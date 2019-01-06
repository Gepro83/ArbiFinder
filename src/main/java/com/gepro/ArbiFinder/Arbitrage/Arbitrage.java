package com.gepro.ArbiFinder.Arbitrage;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.dto.marketdata.Trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Arbitrage {

    private final Exchange mBuyHere;
    private final Exchange mSellHere;
    private List<ArbitrageTrade> mTrades;

    private Arbitrage(Exchange buyHere, Exchange sellHere, ArbitrageTrade trade) {
        this.mBuyHere = buyHere;
        this.mSellHere = sellHere;
        mTrades = new ArrayList<>();
        mTrades.add(trade);
    }

    private Arbitrage(Exchange buyHere, Exchange sellHere, List<ArbitrageTrade> trades) {
        this.mBuyHere = buyHere;
        this.mSellHere = sellHere;
        this.mTrades = trades;
    }

    public static Arbitrage newInstance(
            Exchange buyHere,
            Exchange sellHere,
            ArbitrageTrade trade) {
        Objects.requireNonNull(buyHere);
        Objects.requireNonNull(sellHere);
        Objects.requireNonNull(trade);
        return new Arbitrage(buyHere, sellHere, trade);
    }

    public static Arbitrage newInstance(
            Exchange buyHere,
            Exchange sellHere,
            List<ArbitrageTrade> trades) {
        Objects.requireNonNull(buyHere);
        Objects.requireNonNull(sellHere);
        Objects.requireNonNull(trades);

        if (trades.isEmpty())
            throw new IllegalArgumentException("trades must contain at least 1 trade");

        return new Arbitrage(buyHere, sellHere, trades);
    }

    public Exchange getBuyHere() { return mBuyHere; }
    public Exchange getSellHere() { return mSellHere; }
    public List<ArbitrageTrade> getTrades() { return mTrades; }

    @Override
    public String toString() {
        return "Arbitrage{" +
                "buyHere=" + mBuyHere +
                ", sellHere=" + mSellHere +
                ", trades=" + mTrades +
                '}';
    }
}
