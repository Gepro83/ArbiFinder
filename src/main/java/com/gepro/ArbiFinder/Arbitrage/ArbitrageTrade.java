package com.gepro.ArbiFinder.Arbitrage;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class ArbitrageTrade {

    private final BigDecimal mAmount;
    private final BigDecimal mAskPrice;
    private final BigDecimal mBidPrice;
    private final BigDecimal mSpread;

    private ArbitrageTrade(
            BigDecimal amount,
            BigDecimal askPrice,
            BigDecimal bidPrice,
            BigDecimal totalFee) {
        mAmount = amount;
        mAskPrice = askPrice;
        mBidPrice = bidPrice;
        mSpread = bidPrice.divide(
                askPrice,
                6,
                RoundingMode.DOWN)
                .subtract(BigDecimal.ONE)
                .subtract(totalFee);
    }

    public static ArbitrageTrade newInstance(
            BigDecimal amount,
            BigDecimal askPrice,
            BigDecimal bidPrice,
            BigDecimal totalFee) {
        Objects.requireNonNull(amount);
        Objects.requireNonNull(askPrice);
        Objects.requireNonNull(bidPrice);
        Objects.requireNonNull(totalFee);
        return new ArbitrageTrade(amount, askPrice, bidPrice, totalFee);
    }

    public BigDecimal getAmount() { return mAmount; }
    public BigDecimal getAskPrice() { return mAskPrice; }
    public BigDecimal getBidPrice() { return mBidPrice; }
    public BigDecimal getSpread() { return mSpread; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArbitrageTrade that = (ArbitrageTrade) o;

        if (!mAmount.equals(that.mAmount)) return false;
        if (!mAskPrice.equals(that.mAskPrice)) return false;
        if (!mBidPrice.equals(that.mBidPrice)) return false;
        return mSpread.equals(that.mSpread);
    }

    @Override
    public int hashCode() {
        int result = mAmount.hashCode();
        result = 31 * result + mAskPrice.hashCode();
        result = 31 * result + mBidPrice.hashCode();
        result = 31 * result + mSpread.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "amount: " + mAmount.toPlainString()
                + "ask price: " + mAskPrice.toPlainString()
                + "bid price: " + mBidPrice.toPlainString()
                + "spread: " + mSpread;
    }
}
