package com.cadenasuministros.application.factory;

import com.cadenasuministros.domain.port.in.RegisterSensorReadingUseCase;

public abstract class RegisterSensorReadingUseCaseFactory {

    // FACTORY METHOD
    public RegisterSensorReadingUseCase createUseCase() {
        return buildUseCase();
    }

    protected abstract RegisterSensorReadingUseCase buildUseCase();
}
