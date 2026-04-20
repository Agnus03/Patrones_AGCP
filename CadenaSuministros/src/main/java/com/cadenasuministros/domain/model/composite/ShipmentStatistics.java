package com.cadenasuministros.domain.model.composite;

import java.math.BigDecimal;

public record ShipmentStatistics(
    int totalItems,
    int totalProducts,
    BigDecimal totalValue,
    BigDecimal maxValue,
    BigDecimal minValue,
    int deliveredCount,
    int pendingCount
) {
    public ShipmentStatistics {
        if (totalValue == null) {
            totalValue = BigDecimal.ZERO;
        }
        if (maxValue == null) {
            maxValue = BigDecimal.ZERO;
        }
        if (minValue == null) {
            minValue = BigDecimal.ZERO;
        }
    }

    public double averageValue() {
        if (totalItems == 0) {
            return 0.0;
        }
        return totalValue.doubleValue() / totalItems;
    }

    public double deliveryRate() {
        int total = deliveredCount + pendingCount;
        if (total == 0) {
            return 0.0;
        }
        return (double) deliveredCount / total * 100;
    }
}