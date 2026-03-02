package com.cadenasuministros.domain.port.out;

import com.cadenasuministros.domain.model.Shipment;

import java.util.Optional;
import java.util.UUID;

public interface ShipmentRepository {
    Optional<Shipment> findById(UUID id);
    Shipment save(Shipment shipment);
}
