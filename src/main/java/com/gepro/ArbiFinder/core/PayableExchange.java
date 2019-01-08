package com.gepro.ArbiFinder.core;

import org.knowm.xchange.Exchange;

import java.math.BigDecimal;
import java.util.Objects;

public class PayableExchange {
    private final Exchange mExchange;
    private final BigDecimal mMakerFee;
    private final BigDecimal mTakerFee;

    private PayableExchange(Exchange exchange, BigDecimal makerFee, BigDecimal takerFee) {
        mExchange = exchange;
        mMakerFee = makerFee;
        mTakerFee = takerFee;
    }

    public static PayableExchange of(Exchange exchange, BigDecimal makerFee, BigDecimal takerFee) {
        Objects.requireNonNull(exchange);
        Objects.requireNonNull(makerFee);
        Objects.requireNonNull(takerFee);

        return new PayableExchange(exchange, makerFee, takerFee);
    }

    public static PayableExchange noFee(Exchange exchange) {
        Objects.requireNonNull(exchange);

        return new PayableExchange(exchange, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    public Exchange getExchange() {
        return mExchange;
    }

    public BigDecimal getMakerFee() {
        return mMakerFee;
    }

    public BigDecimal getTakerFee() {
        return mTakerFee;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PayableExchange that = (PayableExchange) o;

        if (!mExchange.equals(that.mExchange)) return false;
        if (!mMakerFee.equals(that.mMakerFee)) return false;
        return mTakerFee.equals(that.mTakerFee);
    }

    @Override
    public int hashCode() {
        int result = mExchange.hashCode();
        result = 31 * result + mMakerFee.hashCode();
        result = 31 * result + mTakerFee.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PayableExchange{" +
                "exchange=" + mExchange +
                ", makerFee=" + mMakerFee +
                ", takerFee=" + mTakerFee +
                '}';
    }
}
