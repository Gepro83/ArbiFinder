package com.gepro.ArbiFinder;

import com.gepro.ArbiFinder.Arbitrage.Arbitrage;
import com.gepro.ArbiFinder.Arbitrage.ArbitrageTrade;
import com.gepro.ArbiFinder.core.PayableExchange;
import com.gepro.ArbiFinder.core.SpreadFinder;

import org.junit.jupiter.api.Test;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.opentest4j.TestAbortedException;

import java.math.BigDecimal;
import java.util.*;

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
        int[] askAmounts = new int[]{1, 3, 5, 30, 50};
        int[] askPrices = new int[]{100, 101, 105, 108, 112};

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

        bidAmounts = new int[]{2, 1, 4, 40, 5};
        bidPrices = new int[]{103, 102, 101, 99, 98};

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
        int[] askAmounts = new int[]{1, 3, 5, 30, 50};
        int[] askPrices = new int[]{100, 101, 105, 108, 112};

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

        bidAmounts = new int[]{2, 1, 4, 40, 5};
        bidPrices = new int[]{103, 102, 101, 99, 98};

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
                        new BigDecimal(103),
                        BigDecimal.ZERO
                ),
                ArbitrageTrade.newInstance(
                        BigDecimal.ONE,
                        new BigDecimal(101),
                        new BigDecimal(103),
                        BigDecimal.ZERO
                ),
                ArbitrageTrade.newInstance(
                        BigDecimal.ONE,
                        new BigDecimal(101),
                        new BigDecimal(102),
                        BigDecimal.ZERO
                )
           )
        );
    }

    @Test
    void findArbitrage_ReverseExchangesFound() {

        int[] askAmounts = new int[]{1, 3, 5, 30, 50};
        int[] askPrices = new int[]{100, 101, 105, 108, 112};

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

        bidAmounts = new int[]{2, 1, 4, 40, 5};
        bidPrices = new int[]{103, 102, 101, 99, 98};

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
    void findArbitrage_NoOrderbook(){
        TestExchange exchangeA = new TestExchange(null, ETH_EUR);
        TestExchange exchangeB = new TestExchange(null, ETH_EUR);

        SpreadFinder spreadFinder = new SpreadFinder(
                ETH_EUR,
                PayableExchange.noFee(exchangeA),
                PayableExchange.noFee(exchangeB)
        );

        assertFalse(spreadFinder.findArbitrage().isPresent());

    }

    @Test
    void findArbitrage_FeeTooHigh(){
        OrderBook buyHere = newOrderbook(
                new int[]{1, 2},
                new int[]{100, 101},
                new int[]{2, 3},
                new int[]{99, 98},
                CurrencyPair.ETH_EUR
        );

        OrderBook sellHere = newOrderbook(
                new int[]{3, 4},
                new int[]{103, 104},
                new int[]{2, 2},
                new int[]{101, 100},
                CurrencyPair.ETH_EUR
        );

        SpreadFinder spreadFinder = new SpreadFinder(
                CurrencyPair.ETH_EUR,
                PayableExchange.of(
                        new TestExchange(buyHere, CurrencyPair.ETH_EUR),
                        BigDecimal.ZERO,
                        new BigDecimal("0.006")),
                PayableExchange.of(
                        new TestExchange(sellHere, CurrencyPair.ETH_EUR),
                        BigDecimal.ZERO,
                        new BigDecimal("0.005"))
        );

        assertFalse(spreadFinder.findArbitrage().isPresent());
    }

    @Test
    void findArbitrage_ConcurrentGetOrders(){
        OrderBook oderbook = newOrderbook(
                new int[]{1, 2},
                new int[]{100, 101},
                new int[]{1, 2},
                new int[]{99, 98},
                ETH_EUR
        );

        TestExchange slowExchange = new TestExchange(new TestMarketDataService(
                oderbook,
                null,
                ETH_EUR) {

            @Override
            public OrderBook getOrderBook(CurrencyPair pair, Object... args){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return oderbook;
            }
        });

        SpreadFinder spreadFinder = new SpreadFinder(
                ETH_EUR,
                PayableExchange.noFee(slowExchange),
                PayableExchange.noFee(slowExchange)
        );

        long before = System.nanoTime();

        spreadFinder.findArbitrage();

        long duration = System.nanoTime() - before;

        assertTrue(duration < (1950 * 1e+6));
    }

//    @Test
//    void findArbitrageOrders_QuitIfNoArbPossible(){
//        OrderBook shortOrderbook = new OrderBook(
//                new Date(),
//                createOrders(
//                        Order.OrderType.ASK,
//                        new int[]{1, 2, 3},
//                        new int[]{100, 102, 105},
//                        CurrencyPair.ETH_EUR),
//                createOrders(
//                        Order.OrderType.BID,
//                        new int[]{1, 2, 3},
//                        new int[]{99, 98, 95},
//                        CurrencyPair.ETH_EUR
//                        )
//        );
//
//        int[] manyAsks = new int[3000];
//
//
//        for (int i = 0; i < 3000; i++) {
//            manyAsks[i] = i+1;
//        }
//        int[] manyBids = new int[3000];
//
//
//    }

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

    private static OrderBook newOrderbook(
            int[] askAmounts,
            int[] askPrices,
            int[] bidAmounts,
            int[] bidPrices,
            CurrencyPair pair
    ){
        return new OrderBook(
                new Date(),
                createOrders(Order.OrderType.ASK, askAmounts, askPrices, pair),
                createOrders(Order.OrderType.BID, bidAmounts, bidPrices, pair)
        );
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
