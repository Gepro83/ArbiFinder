package com.gepro.ArbiFinder;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.opentest4j.TestAbortedException;


public class TestMarketDataService implements MarketDataService {
    private OrderBook mOrderbook;
    private Ticker mTicker;
    private CurrencyPair mPair;

    public TestMarketDataService(OrderBook mOrderbook, Ticker mTicker, CurrencyPair pair) {
        this.mOrderbook = mOrderbook;
        this.mTicker = mTicker;
        this.mPair = pair;
    }

    public OrderBook getmOrderbook() {
        return mOrderbook;
    }

    public void setmOrderbook(OrderBook mOrderbook) {
        this.mOrderbook = mOrderbook;
    }

    public Ticker getmTicker() {
        return mTicker;
    }

    public void setmTicker(Ticker mTicker) {
        this.mTicker = mTicker;
    }

    public CurrencyPair getmPair() {
        return mPair;
    }

    public void setmPair(CurrencyPair mPair) {
        this.mPair = mPair;
    }

    @Override
    public OrderBook getOrderBook(CurrencyPair pair, Object... args){
        if(!pair.equals(mPair)) throw new TestAbortedException("Wrong currencypair, expected: " + mPair + " but got: " + pair);
        return mOrderbook;
    }

    @Override
    public Ticker getTicker(CurrencyPair pair, Object... args){
        if(!pair.equals(mPair)) throw new TestAbortedException("Wrong currencypair, expected: " + mPair + " but got: " + pair);
        return mTicker;
    }
}
