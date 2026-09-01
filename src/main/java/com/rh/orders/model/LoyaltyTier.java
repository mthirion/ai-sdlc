package com.rh.orders.model;

import java.math.BigDecimal;

public enum LoyaltyTier {

    NONE(BigDecimal.ZERO),
    SILVER(new BigDecimal("0.05")),
    GOLD(new BigDecimal("0.10")),
    PLATINUM(new BigDecimal("0.15"));

    private final BigDecimal discountRate;

    LoyaltyTier(BigDecimal discountRate) {
        this.discountRate = discountRate;
    }

    public BigDecimal getDiscountRate() {
        return discountRate;
    }
}
