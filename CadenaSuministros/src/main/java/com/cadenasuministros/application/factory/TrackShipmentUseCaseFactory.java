package com.cadenasuministros.application.factory;

import com.cadenasuministros.domain.port.in.TrackShipmentUseCase;

public abstract class TrackShipmentUseCaseFactory {

    // FACTORY METHOD
    public TrackShipmentUseCase createUseCase() {
        return buildUseCase();
    }

    protected abstract TrackShipmentUseCase buildUseCase();
}
