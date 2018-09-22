package com.gepro.ArbiFinder;

import org.jetbrains.annotations.Nullable;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.abucoins.dto.account.AbucoinsPaymentMethod;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.BiFunction;

public class SpreadFinder {

    private List<Exchange> mExchanges;
    private Map<Ticker, Exchange> mTickersToExchange;
    private Map<OrderBook, Exchange> mOrderbooksToExchange;

    private List<CurrencyPair> mPairs;

    public SpreadFinder(List<CurrencyPair> pairs, List<Exchange> exchanges) {
        mPairs = pairs;
        mExchanges = exchanges;
        mTickersToExchange = new HashMap<>();
        mOrderbooksToExchange = new HashMap<>();
    }

//    public void checkForSpread() throws InterruptedException {
//
//        while (true) {
//            long before = System.currentTimeMillis();
//            renewTickers();
//            for (CurrencyPair pair : mPairs) {
//                System.out.flush();
//                System.out.println("====================");
//                System.out.println("Max spread for " + pair.toString() + ": " + getMaxSpread(pair));
//                System.out.println("====================");
//                Thread.sleep(3000);
//            }
//            long after = System.currentTimeMillis();
//            System.out.println(after - before);
//        }
//    }

    public void renewTickers() {
        renewData(mTickersToExchange,
                LambdaExceptionWrapper.handlingBiFunctionWrapper(
                        (marketDataService, currencyPair) -> marketDataService.getTicker(currencyPair),
                        IOException.class)
        );
    }

    public void renewOrderbooks() {
        renewData(mOrderbooksToExchange,
                LambdaExceptionWrapper.handlingBiFunctionWrapper(
                        (marketDataService, currencyPair) -> marketDataService.getOrderBook(currencyPair),
                        IOException.class)
        );
    }

    //TODO only renew data for specific pair
    //TODO dont call getMarketDataService every time since it makes a http request
    private <T> void renewData(Map<T, Exchange> dataMap, BiFunction<MarketDataService, CurrencyPair, T> getData) {
        synchronized (dataMap) {
            dataMap.clear();

            // add data for every exchange and pair
            for (Exchange exc : mExchanges) {
                MarketDataService ds = exc.getMarketDataService();
                for (CurrencyPair pair : mPairs)
                    try {
                        dataMap.put(getData.apply(ds, pair), exc);
                    } catch (Exception e) {
                        System.out.println("Couldnt get data from: " + exc.toString());
                        break;
                    }
            }
        }
    }

    public Map<LimitOrder, Exchange> findArbitrageOrders(
            CurrencyPair pair,
            Exchange exchange1,
            Exchange exchange2) {

        OrderBook orderBookExc1 = getOrderbook(exchange1, pair);
        OrderBook orderBookExc2 = getOrderbook(exchange2, pair);

        Map<LimitOrder, Exchange> arbiOrdersToExchange = new HashMap<>();
        if(orderBookExc1 == null || orderBookExc2 == null) return arbiOrdersToExchange;

        List<LimitOrder> arbiOrders = findArbitrageOrders(
                orderBookExc1.getAsks(), orderBookExc2.getBids(), pair);

        for(LimitOrder order : arbiOrders)

            if(order.getType() == Order.OrderType.ASK) {
                arbiOrdersToExchange.put(order, exchange1);
            }
            else {
                arbiOrdersToExchange.put(order, exchange2);
            }

        if(arbiOrdersToExchange.size() == 0){

            arbiOrders = findArbitrageOrders(
                    orderBookExc2.getAsks(), orderBookExc1.getBids(), pair
            );

            for(LimitOrder order : arbiOrders)

                if(order.getType() == Order.OrderType.ASK) {
                    arbiOrdersToExchange.put(order, exchange2);
                }
                else {
                    arbiOrdersToExchange.put(order, exchange1);
                }
        }

        return arbiOrdersToExchange;
    }

    private List<LimitOrder> findArbitrageOrders(List<LimitOrder> asks, List<LimitOrder> bids, CurrencyPair pair){
        //copy since the lists are going to be manipulated
        List<LimitOrder> _asks = new ArrayList<>(asks);
        List<LimitOrder> _bids = new ArrayList<>(bids);
        // sort ascending
        Collections.sort(_asks, (a, b) -> a.getLimitPrice().compareTo(b.getLimitPrice()));
        // sort descending
        Collections.sort(_bids, (a, b) -> b.getLimitPrice().compareTo(a.getLimitPrice()));

        List<LimitOrder> arbiOrders = new ArrayList<>();
        Map<LimitOrder, BigDecimal> remainingBidAdmount = new HashMap<>();

        for(LimitOrder ask : _asks) {

            BigDecimal askAmount = ask.getOriginalAmount();

            bidsloop:
            for (LimitOrder bid : _bids) {

                BigDecimal askPrice = ask.getLimitPrice();
                BigDecimal bidPrice = bid.getLimitPrice();

                if (askPrice.compareTo(bidPrice) < 0) {

                    BigDecimal bidAmount = remainingBidAdmount.containsKey(bid) ?
                            remainingBidAdmount.get(bid) : bid.getOriginalAmount();
                    BigDecimal arbiOrderAmount;
                    boolean askCleared = false;

                    if(askAmount.compareTo(bidAmount) > 0) {

                        arbiOrderAmount = bidAmount;

                        askAmount.subtract(arbiOrderAmount,
                                new MathContext(128, RoundingMode.DOWN));

                        if(askAmount.compareTo(BigDecimal.ZERO) <= 0) askCleared = true;

                        _bids.remove(bid);

                    } else {

                        arbiOrderAmount = askAmount;

                        remainingBidAdmount.put(bid, bidAmount.subtract(
                                arbiOrderAmount,
                                new MathContext(128, RoundingMode.DOWN)));

                        askCleared = true;
                    }

                    arbiOrders.add(new LimitOrder.Builder(Order.OrderType.BID, pair)
                            .limitPrice(askPrice)
                            .originalAmount(arbiOrderAmount)
                            .build());

                    arbiOrders.add(new LimitOrder.Builder(Order.OrderType.ASK, pair)
                            .limitPrice(bidPrice)
                            .originalAmount(arbiOrderAmount)
                            .build());

                    if(askCleared) break bidsloop;
                }
            }
        }

        return arbiOrders;
    }

    public class SpreadExchanges {
        Exchange buy;
        Exchange sell;
        BigDecimal bid;
        BigDecimal ask;

        public BigDecimal spread() { return bid.subtract(ask); }
        public BigDecimal spreadPercent() {
            return spread().divide(ask, 6, RoundingMode.CEILING.HALF_DOWN);
        }
    }

    public SpreadExchanges getMaxSpread(CurrencyPair pair) {
        SpreadExchanges spreadExchanges = new SpreadExchanges();

        spreadExchanges.bid = new BigDecimal(-1);
        spreadExchanges.ask= new BigDecimal(Integer.MAX_VALUE);

        synchronized (mTickersToExchange) {
            for (Map.Entry<Ticker, Exchange> entry : mTickersToExchange.entrySet()) {
                Ticker t = entry.getKey();
                if (!t.getCurrencyPair().equals(pair))
                    continue;
                if (t.getAsk().compareTo(spreadExchanges.ask) == -1) {
                    spreadExchanges.ask = t.getAsk();
                    spreadExchanges.buy = entry.getValue();
                }
                if (t.getBid().compareTo(spreadExchanges.ask) == 1) {
                    spreadExchanges.bid = t.getBid();
                    spreadExchanges.sell = entry.getValue();
                }
            }
        }
        return spreadExchanges;
    }

    public Map<Ticker, Exchange> getTickers(){
        return mTickersToExchange;
    }
    public Map<OrderBook, Exchange> getmOrderbooksToExchange() { return mOrderbooksToExchange; }

    @Nullable
    private OrderBook getOrderbook(Exchange exchange, CurrencyPair pair){
        for(Map.Entry<OrderBook, Exchange> entry : mOrderbooksToExchange.entrySet()){
            if(entry.getValue() != exchange) continue;
            OrderBook ob = entry.getKey();
            if(ob.getAsks() == null) continue;
            if(ob.getAsks().get(0) == null) continue;
            if(ob.getAsks().get(0).getCurrencyPair() == pair) return ob;
        }
        return null;
    }
}
