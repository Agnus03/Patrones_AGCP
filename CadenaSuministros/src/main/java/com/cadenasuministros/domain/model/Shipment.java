package com.cadenasuministros.domain.model;

import java.time.Instant;
import java.util.UUID;
import com.cadenasuministros.domain.prototype.Prototype;

public record Shipment(
        UUID id,
        UUID productId,
        String status,
        String currentLocation,
        Instant updatedAt
) implements Prototype<Shipment> {

    @Override
    public Shipment copy() {
        return new Shipment(
                this.id,
                this.productId,
                this.status,
                this.currentLocation,
                this.updatedAt
        );
    }

    // Métodos de "clon con variación"
    public Shipment withStatus(String newStatus) {
        Shipment clone = this.copy();
        return new Shipment(
                clone.id(),
                clone.productId(),
                newStatus,
                clone.currentLocation(),
                Instant.now()
        );
    }

    public Shipment withLocation(String newLocation) {
        Shipment clone = this.copy();
        return new Shipment(
                clone.id(),
                clone.productId(),
                clone.status(),
                newLocation,
                Instant.now()
        );
    }
}