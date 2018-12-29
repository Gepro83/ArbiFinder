package com.gepro.ArbiFinder;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.bitstamp.BitstampExchange;
import org.knowm.xchange.cexio.CexIOExchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.exmo.ExmoExchange;
import org.knowm.xchange.coinbasepro.CoinbaseProExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.therock.TheRockExchange;
import org.opentest4j.TestAbortedException;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class SpreadFinderTest {

    private SpreadFinder mSpreadFinder;
    private static CurrencyPair testPair = CurrencyPair.ETH_EUR;

    public SpreadFinderTest() {
        mSpreadFinder = new SpreadFinder(
                asList(CurrencyPair.ETH_EUR),
                asList(
                        ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(CexIOExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(CoinbaseProExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(ExmoExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(TheRockExchange.class.getName())
                ));

    }

    @Disabled
    @Test
    void getMaxSpread() {
        SpreadFinder.SpreadExchanges spread = mSpreadFinder.getMaxSpread(CurrencyPair.ETH_EUR);
        System.out.println("Max spread: " + spread.spread() + " = " + spread.spreadPercent());
        System.out.println("Buy from: " + spread.buy.getClass().getName());
        System.out.println("Sell to: " + spread.sell.getClass().getName());
    }

    @Disabled
    @Test
    void renewData() {
        long before = System.currentTimeMillis();
        mSpreadFinder.renewOrderbooks();
        long after = System.currentTimeMillis();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("==================");
        System.out.println("Orderbook renew took " + (after - before) + " millis");
        System.out.println("==================");
        System.out.println();
        System.out.println();
        System.out.println();

        before = System.currentTimeMillis();
        mSpreadFinder.renewTickers();
        after = System.currentTimeMillis();
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("==================");
        System.out.println("Ticker renew took " + (after - before) + " millis");
        System.out.println("==================");
        System.out.println();
        System.out.println();
        System.out.println();

        for(Map.Entry<OrderBook, Exchange> entry : mSpreadFinder.getmOrderbooksToExchange().entrySet()){
            OrderBook ob = entry.getKey();
            Exchange exc = entry.getValue();
            System.out.println("===============");
            System.out.println("Orderbook for " + exc.getClass().getName());
            System.out.println("asks:");
            int maxOrders = 5;
            int numOrders = 0;
            for(LimitOrder ask : ob.getAsks()){
                if(++numOrders > maxOrders) break;
                System.out.println("Price: " + ask.getLimitPrice());
                System.out.println("Amount: " + ask.getOriginalAmount());
            }
            System.out.println("--------");
            System.out.println("bids:");
            numOrders = 0;
            for(LimitOrder bid : ob.getBids()){
                if(++numOrders > maxOrders) break;
                System.out.println("Price: " + bid.getLimitPrice());
                System.out.println("Amount: " + bid.getOriginalAmount());
            }
        }
    }

    @Test
    void findArbitrageOrders() {
        //also test non-sorted orderbooks
        int[] askAmounts = new int[]{5, 3, 1, 30, 50};
        int[] askPrices = new int[]{105, 101, 100, 108, 112};

        int[] bidAmounts = new int[]{2, 1, 1, 3, 5};
        int[] bidPrices = new int[]{99, 98, 95, 94, 92};

        /*
        Sorted:
            ASK
            100 1
            101 3
            105 5
            ...
            BID
            99 2
            98 1
            95 1
         */

        OrderBook orderBookBuy = new OrderBook(
                new Date(),
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, testPair),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, testPair)
        );

        askAmounts = new int[]{2, 2, 3, 1, 5};
        askPrices = new int[]{105, 106, 110, 111, 120};

        bidAmounts = new int[]{4, 1, 2, 40, 5};
        bidPrices = new int[]{101, 102, 103, 99, 98};

        /*
        Sorted:
            ASK
            105 2
            106 2
            110 3
            ...
            BID
            103 2
            102 1
            101 4
            ..
         */

        OrderBook orderBookSell = new OrderBook(
                new Date(),
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, testPair),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, testPair)
        );

        TestExchange exchangeBuy = new TestExchange(orderBookBuy, testPair);
        TestExchange exchangeSell = new TestExchange(orderBookSell, testPair);

        SpreadFinder spreadFinder = new SpreadFinder(
                asList(testPair),
                asList(exchangeBuy, exchangeSell)
                );

        spreadFinder.renewOrderbooks();

        Map<LimitOrder, Exchange> arbiOrders = spreadFinder.findArbitrageOrders(
                testPair,
                exchangeBuy,
                exchangeSell
        );

        assertTrue(arbiOrders.containsValue(exchangeBuy));
        assertTrue(arbiOrders.containsValue(exchangeSell));

        // an order is: amount, price, type (1 ask 2 bid), exchange (3 buy 4 sell)
        HashSet<List<Integer>> simplifiedOrders = new HashSet<>();
        simplifiedOrders.add(asList(1, 100, 2, 3));
        simplifiedOrders.add(asList(2, 101, 2, 3));
        simplifiedOrders.add(asList(2, 103, 1, 4));
        simplifiedOrders.add(asList(1, 102, 1, 4));

        for(Map.Entry<LimitOrder, Exchange> arbiOrder : arbiOrders.entrySet()){
            LimitOrder order = arbiOrder.getKey();
            int amount = order.getOriginalAmount().intValue();
            int price = order.getLimitPrice().intValue();
            int type = 0;
            if(order.getType() == Order.OrderType.ASK) type = 1;
            if(order.getType() == Order.OrderType.BID) type = 2;

            int exchange = 0;
            if(arbiOrder.getValue() == exchangeBuy) exchange = 3;
            if(arbiOrder.getValue() == exchangeSell) exchange = 4;

            List<Integer> simplifiedOrder = asList(amount, price, type, exchange);
            simplifiedOrders.remove(simplifiedOrder);
        }
        System.out.println("Missing Orders:");
        for(List<Integer> simpleOrder : simplifiedOrders)
            System.out.println(simpleOrder.toString());
        assertEquals(0, simplifiedOrders.size());
    }

    @Test
    void findArbitrageOrders_reversExchangeOrder() {
        OrderBook orderBook = new OrderBook(
                new Date(),
                createOrders(
                        Order.OrderType.ASK,
                        new int[]{1, 2},
                        new int[]{100, 102},
                       CurrencyPair.ETH_EUR),
                createOrders(
                        Order.OrderType.BID,
                        new int[]{3, 1},
                        new int[]{99, 97},
                        CurrencyPair.ETH_EUR)
        );

        TestExchange lowAskExchange = new TestExchange(orderBook, CurrencyPair.ETH_EUR);

        OrderBook otherOrderbook = new OrderBook(
                new Date(),
                createOrders(
                        Order.OrderType.ASK,
                        new int[]{1, 2},
                        new int[]{102, 103},
                        CurrencyPair.ETH_EUR),
                createOrders(
                        Order.OrderType.BID,
                        new int[]{3, 1},
                        new int[]{101, 97},
                        CurrencyPair.ETH_EUR)
        );

        TestExchange highBidExchange = new TestExchange(otherOrderbook, CurrencyPair.ETH_EUR);

        SpreadFinder spreadFinder = new SpreadFinder(
                asList(CurrencyPair.ETH_EUR),
                asList(lowAskExchange, highBidExchange)
        );

        spreadFinder.renewOrderbooks();

        Map<LimitOrder, Exchange> arbiOrders = spreadFinder.findArbitrageOrders(
                CurrencyPair.ETH_EUR,
                highBidExchange,
                lowAskExchange
        );

        assertThat(arbiOrders.values(), hasItems(lowAskExchange, highBidExchange));

    }

    private static List<LimitOrder> createOrders(Order.OrderType type, int[] amounts, int[] price, CurrencyPair pair){
        if(amounts.length != price.length) throw new TestAbortedException("amounts and prices need to be of same length");
        List<LimitOrder> orders = new ArrayList<>();

        for(int i = 0; i < amounts.length; i++)
            orders.add(new LimitOrder(
                    Order.OrderType.ASK,
                    new BigDecimal(amounts[i]), //volume
                    pair,
                    "1",
                    new Date(),
                    new BigDecimal(price[i]))
            );

        return orders;
    }

    private static void printOrderbook(OrderBook ob, int numEntries){
        System.out.println();
        System.out.println("===========");
        System.out.println("asks: ");
        System.out.println("-----------");
        int i = 0;
        for(LimitOrder order : ob.getAsks()){
            if(i++ == numEntries) break;
            printOrder(order);
        }
        System.out.println();
        System.out.println("bids: ");
        System.out.println("-----------");
        i = 0;
        for(LimitOrder order : ob.getBids()){
            if(i++ == numEntries) break;
            printOrder(order);
        }
        System.out.println("===========");
    }

    private static void printOrder(LimitOrder order){
        System.out.println("price: " + order.getLimitPrice() + "\t amount: " + order.getOriginalAmount());
    }
}
