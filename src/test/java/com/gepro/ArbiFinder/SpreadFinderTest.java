package com.gepro.ArbiFinder;

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
import org.knowm.xchange.gdax.GDAXExchange;
import org.knowm.xchange.kraken.KrakenExchange;
import org.knowm.xchange.therock.TheRockExchange;
import org.opentest4j.TestAbortedException;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpreadFinderTest {

    private SpreadFinder mSpreadFinder;
    private static CurrencyPair testPair = CurrencyPair.ETH_EUR;

    public SpreadFinderTest() {
        mSpreadFinder = new SpreadFinder(
                Arrays.asList(CurrencyPair.ETH_EUR),
                Arrays.asList(
                        ExchangeFactory.INSTANCE.createExchange(BitstampExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(KrakenExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(CexIOExchange.class.getName()),
                        ExchangeFactory.INSTANCE.createExchange(GDAXExchange.class.getName()),
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

        OrderBook orderBookBuy = new OrderBook(
                new Date(),
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, testPair),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, testPair)
        );

        askAmounts = new int[]{2, 2, 3, 1, 5};
        askPrices = new int[]{105, 106, 110, 111, 120};

        bidAmounts = new int[]{4, 1, 2, 40, 5};
        bidPrices = new int[]{101, 102, 103, 99, 98};

        OrderBook orderBookSell = new OrderBook(
                new Date(),
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, testPair),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, testPair)
        );

        TestExchange exchangeBuy = new TestExchange(orderBookBuy, testPair);
        TestExchange exchangeSell = new TestExchange(orderBookSell, testPair);

        SpreadFinder spreadFinder = new SpreadFinder(
                Arrays.asList(testPair),
                Arrays.asList(exchangeBuy, exchangeSell)
                );

        Map<LimitOrder, Exchange> arbiOrders = spreadFinder.findArbitrageOrders(
                testPair,
                exchangeBuy,
                exchangeSell
        );

        assertTrue(arbiOrders.containsValue(exchangeBuy));
        assertTrue(arbiOrders.containsValue(exchangeSell));

        // an order is: amount, price, type (1 ask 2 bid), exchange (3 buy 4 sell)
        HashSet<List<Integer>> simplifiedOrders = new HashSet<>();
        simplifiedOrders.add(Arrays.asList(1, 100, 2, 3));
        simplifiedOrders.add(Arrays.asList(2, 101, 2, 3));
        simplifiedOrders.add(Arrays.asList(2, 103, 1, 4));
        simplifiedOrders.add(Arrays.asList(1, 102, 1, 4));

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

            List<Integer> simplifiedOrder = Arrays.asList(amount, price, type, exchange);
            simplifiedOrder.remove(simplifiedOrder);
        }
        for(List<Integer> simpleOrder : simplifiedOrders)
            System.out.println(simpleOrder.toString());
        assertEquals(0, simplifiedOrders.size());
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
