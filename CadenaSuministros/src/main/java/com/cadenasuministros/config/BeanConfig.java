package com.cadenasuministros.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.cadenasuministros.application.usecase.RegisterSensorReadingService;
import com.cadenasuministros.application.usecase.TrackShipmentService;
import com.cadenasuministros.domain.port.in.RegisterSensorReadingUseCase;
import com.cadenasuministros.domain.port.in.TrackShipmentUseCase;
import com.cadenasuministros.domain.port.out.SensorReadingRepository;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

@Configuration
public class BeanConfig {

	@Bean
	TrackShipmentUseCase trackShipmentUseCase(ShipmentRepository shipmentRepository) {
		return new TrackShipmentService(shipmentRepository);
	}

	@Bean
	RegisterSensorReadingUseCase registerSensorReadingUseCase(SensorReadingRepository sensorReadingRepository) {
		return new RegisterSensorReadingService(sensorReadingRepository);
	}
}