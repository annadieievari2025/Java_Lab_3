package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class StandardPricingPolicy implements PricingPolicy {
    private static BigDecimal MIN_PRICE = new BigDecimal("500.00");
    private static BigDecimal DISCOUNT_THRESHOLD = new BigDecimal("10000.00");
    private static BigDecimal DISCOUNT_RATE = new BigDecimal("0.90");

    @Override
    public BigDecimal calculateCommercialPrice(RepairOrder order) {
        BigDecimal baseCost = order.calculateBaseCost();

        if (baseCost.compareTo(MIN_PRICE) < 0) {
            return MIN_PRICE;
        }
        if (baseCost.compareTo(DISCOUNT_THRESHOLD) > 0) {
            return baseCost.multiply(DISCOUNT_RATE).setScale(2, RoundingMode.HALF_UP);
        }
        return baseCost;
    }
}