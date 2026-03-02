package com.cadenasuministros.domain.port.in;

import java.util.List;

import com.cadenasuministros.domain.model.SensorReading;

public interface RegisterSensorReadingUseCase {
    SensorReading register(SensorReading reading);

	List<SensorReading> listAll();
}