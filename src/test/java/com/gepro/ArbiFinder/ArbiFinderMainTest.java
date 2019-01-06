package com.gepro.ArbiFinder;

import org.junit.jupiter.api.Test;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.List;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ArbiFinderMainTest {

    @Test
    void getSpreadfinderCombinations(){
        PayableExchange ex1 = PayableExchange.noFee(new TestExchange(null, null));
        PayableExchange ex2 = PayableExchange.noFee(new TestExchange(null, null));
        PayableExchange ex3 = PayableExchange.noFee(new TestExchange(null, null));

        List<SpreadFinder> spreadFinders = ArbiFinderMain.getSpreadfinderCombinations(
                asList(CurrencyPair.ETH_EUR, CurrencyPair.BTC_EUR),
                ex1,
                ex2,
                ex3
        );

        assertThat(spreadFinders, hasItems(
                new SpreadFinder(CurrencyPair.ETH_EUR, ex1, ex2),
                new SpreadFinder(CurrencyPair.BTC_EUR, ex1, ex2),
                new SpreadFinder(CurrencyPair.ETH_EUR, ex1, ex3),
                new SpreadFinder(CurrencyPair.BTC_EUR, ex1, ex3),
                new SpreadFinder(CurrencyPair.ETH_EUR, ex2, ex3),
                new SpreadFinder(CurrencyPair.BTC_EUR, ex2, ex3)
        ));

    }
}