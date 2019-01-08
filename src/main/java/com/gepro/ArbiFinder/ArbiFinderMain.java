package com.gepro.ArbiFinder;

import com.gepro.ArbiFinder.Arbitrage.Arbitrage;
import com.gepro.ArbiFinder.Arbitrage.ArbitrageTrade;
import com.gepro.ArbiFinder.Log.TwoWayLogger;
import com.gepro.ArbiFinder.core.PayableExchange;
import com.gepro.ArbiFinder.core.SpreadFinder;
import com.google.common.collect.Lists;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.kraken.KrakenExchange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class ArbiFinderMain {

    private static TwoWayLogger LOG = TwoWayLogger.getInstance();

    public static void main(String[] ars) throws InterruptedException, IOException {

        List<CurrencyPair> pairs = new ArrayList<>();

        pairs.add(CurrencyPair.ETH_EUR);
        pairs.add(CurrencyPair.BTC_EUR);
        pairs.add(CurrencyPair.BCH_EUR);
        pairs.add(CurrencyPair.LTC_EUR);
//        pairs.add(CurrencyPair.ETH_BTC);
//        pairs.add(CurrencyPair.LTC_BTC);
//        pairs.add(CurrencyPair.BCH_BTC);

        Exchange kraken = ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName());
        ExchangeSpecification krakenSpecification = new ExchangeSpecification(KrakenExchange.class.getName());
        krakenSpecification.setApiKey("yqLrvwkVC4gPyNly35Ny6FBlejNbeq153noQGkPPSAq0Qr1amNzdkHJK");
        krakenSpecification.setSecretKey("vT8yMiJ1l+P+QvRMx5DzNoWOqDw+P2bETtCDyc7Mjh2SHSQG+CLnQg1/+RuJHTLnTglSyF8dJcbm1GmY4fo2vQ==");
        kraken.applySpecification(krakenSpecification);


        List<SpreadFinder> spreadFinders = getSpreadfinderCombinations(
                Lists.newArrayList(pairs),
                PayableExchange.of(
                        ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName()),
                        new BigDecimal("0.0025"),
                        new BigDecimal("0.0025")
                ),
                PayableExchange.of(
                        kraken,
                        new BigDecimal("0.0016"),
                        new BigDecimal("0.0026")
                ),
                PayableExchange.of(
                        ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange.class.getName()),
                        new BigDecimal("0.0000"),
                        new BigDecimal("0.0030")
                )
        );

        File db = new File("C:\\Users\\Arbeit\\IdeaProjects\\ArbiFinder2\\Output\\abritrage.txt");

        db.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(db, true)) {

            while (true) {
                findAndLogArbitrage(spreadFinders, outputStream);
                Thread.sleep(2000);
            }
        }
    }

    private static void findAndLogArbitrage(
            List<SpreadFinder> spreadFinders,
            FileOutputStream outputStream
    ) {

        for (SpreadFinder finder : spreadFinders) {
            finder.findArbitrage().ifPresent(
                    (arbi) -> logArbitrage(arbi, outputStream, finder.getPair()));
        }
    }

    private static void logArbitrage(
            Arbitrage arbitrage,
            FileOutputStream outputStream,
            CurrencyPair pair){

        String arbiString = new Date().toString()
                + " buy " +  pair + " at "
                + arbitrage
                        .getBuyHere()
                        .getExchangeSpecification()
                        .getExchangeName()
                + " sell at "
                + arbitrage
                        .getSellHere()
                        .getExchangeSpecification()
                        .getExchangeName()

                + " trades: " + System.lineSeparator();

        for (ArbitrageTrade trade : arbitrage.getTrades()) {

            arbiString += " - askprice: " + trade.getAskPrice().toPlainString()
                    + " bidprice: " + trade.getBidPrice().toPlainString()
                    + " amount: " + trade.getAmount().toPlainString()
                    + " spread: " + (trade.getSpread().doubleValue() * 100.0) + "%"
                    + System.lineSeparator();
        }

        try {
            outputStream.write(arbiString.getBytes("UTF-8"));
            outputStream.flush();
        } catch (IOException e) {
            LOG.write("Error writing to arbitrage log");
        }

    }

    public static List<SpreadFinder> getSpreadfinderCombinations(
            List<CurrencyPair> pairs,
            PayableExchange... exchanges) {
        List<SpreadFinder> spreadFinders = new ArrayList<>();

        for (int i = 0; i < exchanges.length; i++) {
            for (int j = i + 1; j < exchanges.length; j++) {
                for (CurrencyPair pair : pairs) {
                    spreadFinders.add(new SpreadFinder(pair, exchanges[i], exchanges[j]));
                }
            }
        }

        return spreadFinders;
    }
}
