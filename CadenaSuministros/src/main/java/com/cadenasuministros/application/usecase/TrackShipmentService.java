package com.cadenasuministros.application.usecase;

import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.port.in.TrackShipmentUseCase;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

import java.util.List;
import java.util.UUID;

public class TrackShipmentService implements TrackShipmentUseCase {

    private final ShipmentRepository shipmentRepository;

    public TrackShipmentService(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public Shipment getById(UUID shipmentId) {
        return shipmentRepository.findShipmentById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));
    }

    @Override
    public List<Shipment> listAll() {
        return shipmentRepository.listAllShipments();
    }

    @Override
    public Shipment create(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }
}
