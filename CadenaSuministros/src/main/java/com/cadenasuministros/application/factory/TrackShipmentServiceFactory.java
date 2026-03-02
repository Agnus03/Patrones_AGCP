package com.cadenasuministros.application.factory;

import com.cadenasuministros.application.usecase.TrackShipmentService;
import com.cadenasuministros.domain.port.in.TrackShipmentUseCase;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

public class TrackShipmentServiceFactory extends TrackShipmentUseCaseFactory {

    private final ShipmentRepository shipmentRepository;

    public TrackShipmentServiceFactory(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    protected TrackShipmentUseCase buildUseCase() {
        return new TrackShipmentService(shipmentRepository);
    }
}
