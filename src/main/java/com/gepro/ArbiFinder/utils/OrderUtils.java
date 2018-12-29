package com.gepro.ArbiFinder.utils;

import org.knowm.xchange.dto.trade.LimitOrder;

public class OrderUtils {
    private OrderUtils(){}

    public static LimitOrder combineByVolume(LimitOrder o1, LimitOrder o2){
        LimitOrder result = new LimitOrder.Builder(
                o1.getType(),
                o1.getCurrencyPair())
                .limitPrice(o1.getLimitPrice())
                .originalAmount(o1.getOriginalAmount().add(o2.getOriginalAmount()))
                .build();
        return result;
    }
}
