package com.cadenasuministros.application.factory;

import com.cadenasuministros.domain.port.in.TrackShipmentUseCase;
import com.cadenasuministros.domain.port.in.RegisterSensorReadingUseCase;

public interface SupplyChainUseCaseAbstractFactory {

    TrackShipmentUseCase createTrackShipmentUseCase();

    RegisterSensorReadingUseCase createRegisterSensorReadingUseCase();
}