package com.cadenasuministros.adapters.outbound.persistence.jpa;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Entity(name = "shipments")
public class ShipmentJpaEntity {
    @Id
    public UUID id;
    public UUID productId;
    public String status;
    public String currentLocation;
    public Instant updatedAt;
}