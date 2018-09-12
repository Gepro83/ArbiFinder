package com.gepro.ArbiFinder;

import org.jetbrains.annotations.Nullable;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.abucoins.dto.account.AbucoinsPaymentMethod;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

        Map<LimitOrder, Exchange> arbiOrders = new HashMap<>();
        if(orderBookExc1 == null || orderBookExc2 == null) return arbiOrders;

        List<LimitOrder> asks = orderBookExc1.getAsks();

        return arbiOrders;
    }

    private List<LimitOrder> findArbitrageOrders(List<LimitOrder> asks, List<LimitOrder> bids){
        // sort ascending
        Collections.sort(asks, (a, b) -> a.getLimitPrice().compareTo(b.getLimitPrice()));
        // sort descending
        Collections.sort(bids, (a, b) -> b.getLimitPrice().compareTo(a.getLimitPrice()));

        for(LimitOrder ask : asks) {

            for (LimitOrder bid : bids) {
                bid.getLimitPrice();
            }
        }
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
