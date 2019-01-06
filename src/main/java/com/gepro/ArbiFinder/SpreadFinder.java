package com.gepro.ArbiFinder;

import com.gepro.ArbiFinder.Arbitrage.Arbitrage;
import com.gepro.ArbiFinder.Arbitrage.ArbitrageTrade;
import com.gepro.ArbiFinder.Log.TwoWayLogger;
import com.gepro.ArbiFinder.utils.OrderUtils;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

public class SpreadFinder {

    private final CurrencyPair mPair;
    private final PayableExchange mExchangeA;
    private final PayableExchange mExchangeB;

    private static TwoWayLogger LOG = TwoWayLogger.getInstance();

    public SpreadFinder(CurrencyPair pair, PayableExchange exchangeA, PayableExchange exchangeB) {
        Objects.requireNonNull(pair);
        Objects.requireNonNull(exchangeA);
        Objects.requireNonNull(exchangeB);

        mPair = pair;
        mExchangeA = exchangeA;
        mExchangeB = exchangeB;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SpreadFinder that = (SpreadFinder) o;

        if (!mPair.equals(that.mPair)) return false;
        if (!mExchangeA.equals(that.mExchangeA)) return false;
        return mExchangeB.equals(that.mExchangeB);
    }

    @Override
    public int hashCode() {
        int result = mPair.hashCode();
        result = 31 * result + mExchangeA.hashCode();
        result = 31 * result + mExchangeB.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "SpreadFinder{" +
                "pair=" + mPair +
                ", exchangeA=" + mExchangeA +
                ", exchangeB=" + mExchangeB +
                '}';
    }

    private OrderBook getOrderbook(PayableExchange exchange) {
        MarketDataService ds = exchange.getExchange().getMarketDataService();
        try {
            return ds.getOrderBook(mPair);
        } catch (IOException e) {
            LOG.write("Couldnt get orderbook for " + exchange);
            return null;
        }
    }

    public Optional<Arbitrage> findArbitrage() {

        OrderBook orderBookExcA = getOrderbook(mExchangeA);
        OrderBook orderBookExcB = getOrderbook(mExchangeB);

        if(orderBookExcA == null || orderBookExcB == null) return Optional.empty();

        List<ArbitrageTrade> arbiTrades = findArbitrage(
                orderBookExcA.getAsks(),
                orderBookExcB.getBids()
        );

        PayableExchange buyHere = mExchangeA;
        PayableExchange sellHere = mExchangeB;

        if (arbiTrades.size() == 0) {
            arbiTrades = findArbitrage(
                    orderBookExcB.getAsks(),
                    orderBookExcA.getBids()
            );

            buyHere = mExchangeB;
            sellHere = mExchangeA;
        }

        if (arbiTrades.size() == 0) return Optional.empty();

        return Optional.of(
                Arbitrage.newInstance(
                    buyHere.getExchange(),
                    sellHere.getExchange(),
                    arbiTrades
                )
        );
    }

    private List<ArbitrageTrade> findArbitrage(List<LimitOrder> asks, List<LimitOrder> bids){
        //copy since the lists are going to be manipulated
        List<LimitOrder> _asks = new ArrayList<>(asks);
        List<LimitOrder> _bids = new ArrayList<>(bids);
        // sort ascending
        Collections.sort(_asks, Comparator.comparing(LimitOrder::getLimitPrice));
        // sort descending
        Collections.sort(_bids, (a, b) -> b.getLimitPrice().compareTo(a.getLimitPrice()));

        List<ArbitrageTrade> arbiOrders = new ArrayList<>();
        Map<LimitOrder, BigDecimal> remainingBidAdmount = new HashMap<>();

        boolean isArbitragePossible = false;
        for(LimitOrder ask : _asks) {

            BigDecimal askAmount = ask.getOriginalAmount();
            List<LimitOrder> filledBids = new ArrayList<>();
            bidsloop:
            for (LimitOrder bid : _bids) {

                BigDecimal askPrice = ask.getLimitPrice();
                BigDecimal bidPrice = bid.getLimitPrice();

                if (isArbitragePossible(askPrice, bidPrice)) {

                    isArbitragePossible = true;

                    BigDecimal bidAmount = remainingBidAdmount.containsKey(bid) ?
                            remainingBidAdmount.get(bid) : bid.getOriginalAmount();
                    BigDecimal arbiOrderAmount;
                    boolean askCleared = false;

                    if(askAmount.compareTo(bidAmount) > 0) {

                        arbiOrderAmount = bidAmount;

                        askAmount = askAmount.subtract(arbiOrderAmount,
                                new MathContext(128, RoundingMode.DOWN));

                        if(askAmount.compareTo(BigDecimal.ZERO) <= 0) askCleared = true;

                        filledBids.add(bid);
                    } else {

                        arbiOrderAmount = askAmount;

                        remainingBidAdmount.put(bid, bidAmount.subtract(
                                arbiOrderAmount,
                                new MathContext(128, RoundingMode.DOWN)));

                        askCleared = true;
                    }

                    arbiOrders.add(ArbitrageTrade.newInstance(
                            arbiOrderAmount,
                            askPrice,
                            bidPrice
                    ));

                    if(askCleared) break bidsloop;

                } else {
                    break bidsloop;
                }
            }
            _bids.removeAll(filledBids);
            if (isArbitragePossible) {
                isArbitragePossible = false;
            } else {
                break;
            }
        }

        return arbiOrders;
    }

    private boolean isArbitragePossible(BigDecimal askPrice, BigDecimal bidPrice) {
        if (askPrice.compareTo(bidPrice) < 0) {
            if (bidPrice.divide(askPrice, 6, RoundingMode.DOWN).doubleValue() > 1.0045) {
                return true;
            }
        }
        return false;
    }
}
