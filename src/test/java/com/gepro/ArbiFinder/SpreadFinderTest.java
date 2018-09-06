package com.gepro.ArbiFinder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.cexio.CexIOExchange;
import org.knowm.xchange.coinbase.CoinbaseExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.exmo.ExmoExchange;
import org.knowm.xchange.gdax.GDAXExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.therock.TheRockExchange;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class SpreadFinderTest {

    private SpreadFinder mSpreadFinder;

    public SpreadFinderTest(){
        mSpreadFinder = new SpreadFinder(
                Arrays.asList(CurrencyPair.BTC_EUR, CurrencyPair.ETH_EUR),
                Arrays.asList(
                        ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(CexIOExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(CoinbaseExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(GDAXExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(ExmoExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(TheRockExchange.class.getName())
                ));
    }

    @Test
    void getMaxSpread() {

        SpreadFinder.SpreadExchanges spread = mSpreadFinder.getMaxSpread(CurrencyPair.BTC_EUR);
        System.out.println("Max spread: " + spread.spread());
    }
}