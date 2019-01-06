package com.gepro.ArbiFinder;

import com.gepro.ArbiFinder.Arbitrage.Arbitrage;
import com.gepro.ArbiFinder.Arbitrage.ArbitrageTrade;

import org.junit.jupiter.api.Test;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.opentest4j.TestAbortedException;

import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.knowm.xchange.currency.CurrencyPair.ETH_EUR;

class SpreadFinderTest {

    public SpreadFinderTest() {
    }

    @Test
    void findArbitrage_CorrectExchangesFound() {
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
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, ETH_EUR),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, ETH_EUR)
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
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, ETH_EUR),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, ETH_EUR)
        );

        TestExchange exchangeBuy = new TestExchange(orderBookBuy, ETH_EUR);
        TestExchange exchangeSell = new TestExchange(orderBookSell, ETH_EUR);

        SpreadFinder spreadFinder = new SpreadFinder(
                ETH_EUR,
                PayableExchange.noFee(exchangeBuy),
                PayableExchange.noFee(exchangeSell)
        );

        Arbitrage arbitrage = spreadFinder.findArbitrage().get();

        assertThat(arbitrage.getBuyHere(), is(equalTo(exchangeBuy)));
        assertThat(arbitrage.getSellHere(), is(equalTo(exchangeSell)));

    }

    @Test
    void findArbitrage_CorrectTrades() {
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
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, ETH_EUR),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, ETH_EUR)
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
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, ETH_EUR),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, ETH_EUR)
        );

        TestExchange exchangeBuy = new TestExchange(orderBookBuy, ETH_EUR);
        TestExchange exchangeSell = new TestExchange(orderBookSell, ETH_EUR);

        SpreadFinder spreadFinder = new SpreadFinder(
                ETH_EUR,
                PayableExchange.noFee(exchangeBuy),
                PayableExchange.noFee(exchangeSell)
        );

        Arbitrage arbitrage = spreadFinder.findArbitrage().get();

        assertThat(arbitrage.getTrades(), hasItems(
                ArbitrageTrade.newInstance(
                        BigDecimal.ONE,
                        new BigDecimal(100),
                        new BigDecimal(103)
                ),
                ArbitrageTrade.newInstance(
                        BigDecimal.ONE,
                        new BigDecimal(101),
                        new BigDecimal(103)
                ),
                ArbitrageTrade.newInstance(
                        BigDecimal.ONE,
                        new BigDecimal(101),
                        new BigDecimal(102)
                )
           )
        );
    }

    @Test
    void findArbitrageOrders_ReverseExchangesFound() {
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
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, ETH_EUR),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, ETH_EUR)
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
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, ETH_EUR),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, ETH_EUR)
        );

        TestExchange exchangeBuy = new TestExchange(orderBookBuy, ETH_EUR);
        TestExchange exchangeSell = new TestExchange(orderBookSell, ETH_EUR);

        SpreadFinder spreadFinder = new SpreadFinder(
                ETH_EUR,
                PayableExchange.noFee(exchangeSell),
                PayableExchange.noFee(exchangeBuy)
        );

        Arbitrage arbitrage = spreadFinder.findArbitrage().get();

        assertThat(arbitrage.getBuyHere(), is(equalTo(exchangeBuy)));
        assertThat(arbitrage.getSellHere(), is(equalTo(exchangeSell)));
    }

    @Test
    void findArbitrageOrders_NoOrderbook(){
        TestExchange exchangeA = new TestExchange(null, ETH_EUR);
        TestExchange exchangeB = new TestExchange(null, ETH_EUR);

        SpreadFinder spreadFinder = new SpreadFinder(
                ETH_EUR,
                PayableExchange.noFee(exchangeA),
                PayableExchange.noFee(exchangeB)
        );

        assertFalse(spreadFinder.findArbitrage().isPresent());

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
