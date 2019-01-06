package com.gepro.ArbiFinder;

import com.gepro.ArbiFinder.Arbitrage.Arbitrage;
import com.gepro.ArbiFinder.Arbitrage.ArbitrageTrade;
import com.google.common.collect.Lists;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.cexio.CexIOExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exmo.ExmoExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.therock.TheRockExchange;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.sound.midi.Soundbank;

import static java.util.Arrays.asList;

public class ArbiFinderMain {

    public static void main(String[] ars) throws InterruptedException, IOException {

        List<CurrencyPair> pairs = new ArrayList<>();

        pairs.add(CurrencyPair.ETH_EUR);
        pairs.add(CurrencyPair.BTC_EUR);
        pairs.add(CurrencyPair.BCH_EUR);
        pairs.add(CurrencyPair.LTC_EUR);
        pairs.add(CurrencyPair.ETH_BTC);
        pairs.add(CurrencyPair.LTC_BTC);
        pairs.add(CurrencyPair.BCH_BTC);

        List<SpreadFinder> spreadFinders = getSpreadfinderCombinations(
                Lists.newArrayList(pairs),
                PayableExchange.of(
                        ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName()),
                        new BigDecimal("0.25"),
                        new BigDecimal("0.25")
                ),
                PayableExchange.of(
                        ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName()),
                        new BigDecimal("0.16"),
                        new BigDecimal("0.26")
                ),
                PayableExchange.of(
                        ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange.class.getName()),
                        new BigDecimal("0.00"),
                        new BigDecimal("0.30")
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
    ) throws IOException {

        for (SpreadFinder finder : spreadFinders) {
            finder.findArbitrage().ifPresent(
                    (arbi) -> logArbitrage(arbi, outputStream));
        }

                   /* arbiOrders = spreadFinder.findArbitrageOrders(
                            pair,
                            exchanges[i],
                            exchanges[j]
                    );

                    if (!arbiOrders.isEmpty()) {
                        outputStream.write((prettyArbiOrders(arbiOrders) + System.lineSeparator())
                                .getBytes("UTF-8"));
                        outputStream.flush();
                    }*/

    }

    private static void logArbitrage(Arbitrage arbitrage, FileOutputStream outputStream){

        String arbiString = "buy at "
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

            double spread = trade.getBidPrice().divide(
                    trade.getAskPrice(),
                    6,
                    RoundingMode.DOWN
            ).doubleValue() - 1.0;

            arbiString += "askprice: " + trade.getAskPrice().toPlainString()
                    + " bidprice: " + trade.getBidPrice().toPlainString()
                    + " amount: " + trade.getAmount().toPlainString()
                    + " spread: " + (spread * 100) + "%"
                    + System.lineSeparator();
        }

        try {
            outputStream.write(arbiString.getBytes("UTF-8"));
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
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
