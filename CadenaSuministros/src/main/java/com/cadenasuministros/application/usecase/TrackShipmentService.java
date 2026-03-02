package com.cadenasuministros.application.usecase;

import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.port.in.TrackShipmentUseCase;
import com.cadenasuministros.domain.port.out.ShipmentRepository;


import java.util.UUID;

public class TrackShipmentService implements TrackShipmentUseCase {

    private final ShipmentRepository shipmentRepository;

    public TrackShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public Shipment getById(UUID shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));
    }
}
