package com.cadenasuministros.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cadenasuministros.application.factory.*;
import com.cadenasuministros.domain.port.in.*;
import com.cadenasuministros.domain.port.out.*;

@Configuration
public class FactoryConfig {

    @Bean
    TrackShipmentUseCase trackShipmentUseCase(
            TrackShipmentUseCaseFactory factory) {
        return factory.createUseCase();
    }

    @Bean
    TrackShipmentUseCaseFactory trackShipmentUseCaseFactory(
            ShipmentRepository shipmentRepository) {
        return new TrackShipmentServiceFactory(shipmentRepository);
    }

    @Bean
    RegisterSensorReadingUseCase registerSensorReadingUseCase(
            RegisterSensorReadingUseCaseFactory factory) {
        return factory.createUseCase();
    }

    @Bean
    RegisterSensorReadingUseCaseFactory registerSensorReadingUseCaseFactory(
            SensorReadingRepository sensorReadingRepository) {
        return new RegisterSensorReadingServiceFactory(sensorReadingRepository);
    }
}
