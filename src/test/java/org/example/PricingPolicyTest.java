package org.example;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingPolicyTest {

    private StandardPricingPolicy policy = new StandardPricingPolicy();

    @ParameterizedTest(name = "Base: {0}, Expected: {1}")
    @CsvSource({
            "300.00, 500.00",
            "500.00, 500.00",
            "5000.00, 5000.00",
            "10000.00, 10000.00",
            "10000.01, 9000.01"
    })
    void testCalculateCommercialPrice(String baseCostStr, String expectedCostStr) {
        RepairOrder mockOrder = new RepairOrder(new Car("VIN111")) {
            @Override
            public BigDecimal calculateBaseCost() {
                return new BigDecimal(baseCostStr);
            }
        };

        BigDecimal actualCost = policy.calculateCommercialPrice(mockOrder);

        BigDecimal expectedCost = new BigDecimal(expectedCostStr);
        assertEquals(0, expectedCost.compareTo(actualCost));
    }
}