package com.cadenasuministros.application.facade;

import java.util.List;
import java.util.UUID;

public record DeliveryReportInfo(
        UUID id,
        UUID shipmentId,
        String generatedAt,
        String status,
        EnvironmentalStats environmentalStats,
        List<String> observations,
        List<String> alerts
) {
    public record EnvironmentalStats(
            double avgTemperature,
            double minTemperature,
            double maxTemperature,
            double avgHumidity,
            double minHumidity,
            double maxHumidity,
            double totalReadings
    ) {}
}