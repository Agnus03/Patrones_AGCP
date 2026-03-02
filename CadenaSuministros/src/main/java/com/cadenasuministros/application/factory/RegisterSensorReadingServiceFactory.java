package com.cadenasuministros.application.factory;

import com.cadenasuministros.application.usecase.RegisterSensorReadingService;
import com.cadenasuministros.domain.port.in.RegisterSensorReadingUseCase;
import com.cadenasuministros.domain.port.out.SensorReadingRepository;

public class RegisterSensorReadingServiceFactory
        extends RegisterSensorReadingUseCaseFactory {

    private final SensorReadingRepository sensorReadingRepository;

    public RegisterSensorReadingServiceFactory(
            SensorReadingRepository sensorReadingRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
    }

    @Override
    protected RegisterSensorReadingUseCase buildUseCase() {
        return new RegisterSensorReadingService(sensorReadingRepository);
    }
}
