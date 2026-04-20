package com.cadenasuministros.application.facade;

import java.time.Instant;
import java.util.UUID;

public record ShipmentInfo(
        UUID id,
        UUID productId,
        String productName,
        String status,
        String currentLocation,
        Integer quantity,
        Instant createdAt,
        Instant updatedAt
) {}