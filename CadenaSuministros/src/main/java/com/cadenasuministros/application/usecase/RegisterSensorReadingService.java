package com.cadenasuministros.application.usecase;

import java.util.List;

import com.cadenasuministros.domain.model.SensorReading;
import com.cadenasuministros.domain.port.in.RegisterSensorReadingUseCase;
import com.cadenasuministros.domain.port.out.SensorReadingRepository;

public class RegisterSensorReadingService implements RegisterSensorReadingUseCase {

    private final SensorReadingRepository sensorReadingRepository;

    public RegisterSensorReadingService(SensorReadingRepository sensorReadingRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
    }

    @Override
    public SensorReading register(SensorReading reading) {
        return sensorReadingRepository.save(reading);
    }

	@Override
	public List<SensorReading> listAll() {
		// TODO Auto-generated method stub
		return sensorReadingRepository.listAll();
	}
	
}
