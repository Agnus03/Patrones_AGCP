package com.cadenasuministros.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cadenasuministros.application.factory.*;
import com.cadenasuministros.domain.port.in.*;
import com.cadenasuministros.domain.port.out.*;

@Configuration
public class FactoryConfig {

    @Bean
    TrackShipmentUseCaseFactory trackShipmentUseCaseFactory(
            ShipmentRepository shipmentRepository) {
        return new TrackShipmentServiceFactory(shipmentRepository);
    }

    @Bean
    RegisterSensorReadingUseCaseFactory registerSensorReadingUseCaseFactory(
            SensorReadingRepository sensorReadingRepository) {
        return new RegisterSensorReadingServiceFactory(sensorReadingRepository);
    }

    @Bean
    SupplyChainUseCaseAbstractFactory supplyChainUseCaseAbstractFactory(
            TrackShipmentUseCaseFactory trackFactory,
            RegisterSensorReadingUseCaseFactory sensorFactory) {

        return new DefaultSupplyChainUseCaseFactory(trackFactory, sensorFactory);
    }

    @Bean
    TrackShipmentUseCase trackShipmentUseCase(
            SupplyChainUseCaseAbstractFactory factory) {
        return factory.createTrackShipmentUseCase();
    }

    @Bean
    RegisterSensorReadingUseCase registerSensorReadingUseCase(
            SupplyChainUseCaseAbstractFactory factory) {
        return factory.createRegisterSensorReadingUseCase();
    }
}