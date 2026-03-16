package com.cadenasuministros.domain.port.out;

import java.util.List;
import java.util.UUID;

import com.cadenasuministros.domain.model.SensorReading;

public interface SensorReadingRepository {
    SensorReading save(SensorReading reading);
    List<SensorReading> listAll();
	List<SensorReading> findByShipmentId(UUID shipmentId);
}
