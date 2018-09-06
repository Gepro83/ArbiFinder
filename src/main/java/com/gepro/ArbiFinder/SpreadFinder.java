package com.gepro.ArbiFinder;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SpreadFinder {

    private List<Exchange> mExchanges;
    private Map<Ticker, Exchange> mTickersToExchange;
    private List<CurrencyPair> mPairs;

    public SpreadFinder(List<CurrencyPair> pairs, List<Exchange> exchanges) {
        mPairs = pairs;
        mExchanges = exchanges;
        mTickersToExchange = new HashMap<>();
        renewTickers();
    }

    public void checkForSpread() throws InterruptedException {

        while (true) {
            long before = System.currentTimeMillis();
            renewTickers();
            for (CurrencyPair pair : mPairs) {
                System.out.flush();
                System.out.println("====================");
                System.out.println("Max spread for " + pair.toString() + ": " + getMaxSpread(pair));
                System.out.println("====================");
                Thread.sleep(3000);
            }
            long after = System.currentTimeMillis();
            System.out.println(after - before);
        }
    }

    private void renewTickers() {
        mTickersToExchange.clear();

        // add tickers for every exchange and pair
        for (Exchange exc : mExchanges) {
            MarketDataService ds = exc.getMarketDataService();
            for (CurrencyPair pair : mPairs)
                try {
                    Ticker ticker = ds.getTicker(pair);
                    mTickersToExchange.put(ticker, exc);
                } catch (Exception e) {
                    System.out.println("Couldnt get ticker from: " + exc.toString());
                    break;
                }
        }
    }

    public class SpreadExchanges {
        Exchange buy;
        Exchange sell;
        BigDecimal bid;
        BigDecimal ask;

        public BigDecimal spread() { return bid.subtract(ask); }
    }

    public SpreadExchanges getMaxSpread(CurrencyPair pair) {
        SpreadExchanges spreadExchanges = new SpreadExchanges();

        spreadExchanges.bid = new BigDecimal(-1);
        spreadExchanges.ask= new BigDecimal(Integer.MAX_VALUE);

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

        return spreadExchanges;
    }
}
