package com.gepro.ArbiFinder;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.meta.ExchangeMetaData;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import si.mazi.rescu.SynchronizedValueFactory;

import java.io.IOException;
import java.util.List;

public class TestExchange implements Exchange {

    private TestMarketDataService mmarketService;

    public TestExchange(TestMarketDataService mmarketService) {
        this.mmarketService = mmarketService;
    }

    public TestExchange(OrderBook ob, CurrencyPair pair){
        this.mmarketService = new TestMarketDataService(ob, null, pair);
    }

    public TestMarketDataService getMarketService() {
        return mmarketService;
    }

    public void setMarketService(TestMarketDataService mmarketService) {
        this.mmarketService = mmarketService;
    }

    public void setOrderBook(OrderBook ob){
        mmarketService.setmOrderbook(ob);
    }

    @Override
    public ExchangeSpecification getExchangeSpecification() {
        return null;
    }

    @Override
    public ExchangeMetaData getExchangeMetaData() {
        return null;
    }

    @Override
    public List<CurrencyPair> getExchangeSymbols() {
        return null;
    }

    @Override
    public SynchronizedValueFactory<Long> getNonceFactory() {
        return null;
    }

    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {
        return null;
    }

    @Override
    public void applySpecification(ExchangeSpecification exchangeSpecification) {
    }

    @Override
    public MarketDataService getMarketDataService() {
        return mmarketService;
    }

    @Override
    public TradeService getTradeService() {
        return null;
    }

    @Override
    public AccountService getAccountService() {
        return null;
    }

    @Override
    public void remoteInit() throws IOException, ExchangeException {

    }
}
