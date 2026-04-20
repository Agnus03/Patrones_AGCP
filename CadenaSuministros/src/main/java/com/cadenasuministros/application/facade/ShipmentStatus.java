package com.cadenasuministros.application.facade;

import java.time.Instant;
import java.util.UUID;

public record ShipmentStatus(
        UUID shipmentId,
        String status,
        String currentLocation,
        Instant lastUpdate,
        String estimatedDelivery
) {}