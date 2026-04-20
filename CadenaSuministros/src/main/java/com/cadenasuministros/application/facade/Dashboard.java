package com.cadenasuministros.application.facade;

import java.util.List;
import java.util.UUID;

public record Dashboard(
        UUID shipmentId,
        String shipmentStatus,
        String currentLocation,
        ShipmentSummary summary,
        SensorStats sensorStats,
        List<SensorReadingResult> recentReadings,
        List<AlertInfo> activeAlerts
) {
    public record ShipmentSummary(
            String productName,
            Integer quantity,
            String status,
            String createdAt,
            String lastUpdate
    ) {}

    public record SensorStats(
            int totalReadings,
            double avgTemperature,
            double avgHumidity,
            Double lastLatitude,
            Double lastLongitude,
            boolean isWithinRange
    ) {}

    public record AlertInfo(
            String type,
            String message,
            String timestamp,
            boolean acknowledged
    ) {}
}