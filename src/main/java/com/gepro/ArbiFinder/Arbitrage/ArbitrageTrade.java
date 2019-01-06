package com.gepro.ArbiFinder.Arbitrage;

import java.math.BigDecimal;
import java.util.Objects;

public class ArbitrageTrade {

    private final BigDecimal mAmount;
    private final BigDecimal mAskPrice;
    private final BigDecimal mBidPrice;

    private ArbitrageTrade(BigDecimal amount, BigDecimal askPrice, BigDecimal bidPrice) {
        mAmount = amount;
        mAskPrice = askPrice;
        mBidPrice = bidPrice;
    }

    public static ArbitrageTrade newInstance(
            BigDecimal amount,
            BigDecimal askPrice,
            BigDecimal bidPrice) {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(askPrice);
        Objects.requireNonNull(bidPrice);
        return new ArbitrageTrade(amount, askPrice, bidPrice);
    }

    public BigDecimal getAmount() { return mAmount; }
    public BigDecimal getAskPrice() { return mAskPrice; }
    public BigDecimal getBidPrice() { return mBidPrice; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArbitrageTrade that = (ArbitrageTrade) o;

        if (!mAmount.equals(that.mAmount)) return false;
        if (!mAskPrice.equals(that.mAskPrice)) return false;
        return mBidPrice.equals(that.mBidPrice);
    }

    @Override
    public int hashCode() {
        int result = mAmount.hashCode();
        result = 31 * result + mAskPrice.hashCode();
        result = 31 * result + mBidPrice.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "amount: " + mAmount.toPlainString()
                + "ask price: " + mAskPrice.toPlainString()
                + "bid price: " + mBidPrice.toPlainString();
    }
}
