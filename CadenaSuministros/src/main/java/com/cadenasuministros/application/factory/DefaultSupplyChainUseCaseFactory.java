package com.cadenasuministros.application.factory;

import com.cadenasuministros.domain.port.in.TrackShipmentUseCase;
import com.cadenasuministros.domain.port.in.RegisterSensorReadingUseCase;

public class DefaultSupplyChainUseCaseFactory
        implements SupplyChainUseCaseAbstractFactory {

    private final TrackShipmentUseCaseFactory trackFactory;
    private final RegisterSensorReadingUseCaseFactory sensorFactory;

    public DefaultSupplyChainUseCaseFactory(
            TrackShipmentUseCaseFactory trackFactory,
            RegisterSensorReadingUseCaseFactory sensorFactory) {

        this.trackFactory = trackFactory;
        this.sensorFactory = sensorFactory;
    }

    @Override
    public TrackShipmentUseCase createTrackShipmentUseCase() {
        return trackFactory.createUseCase();
    }

    @Override
    public RegisterSensorReadingUseCase createRegisterSensorReadingUseCase() {
        return sensorFactory.createUseCase();
    }
}