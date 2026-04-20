package com.cadenasuministros.domain.port.in;

import com.cadenasuministros.domain.model.Shipment;

import java.util.UUID;

public interface TrackShipmentUseCase {
    Shipment getById(UUID shipmentId);
    Shipment create(Shipment shipment);
}