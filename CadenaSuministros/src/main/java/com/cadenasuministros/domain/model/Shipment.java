package com.cadenasuministros.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Shipment(
        UUID id,
        UUID productId,
        String status,
        String currentLocation,
        Instant updatedAt
) {}
