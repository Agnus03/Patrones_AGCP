package com.cadenasuministros.domain.port.in;

import com.cadenasuministros.domain.model.DeliveryReport;
import com.cadenasuministros.domain.model.SensorReading;

import java.util.List;
import java.util.UUID;

public interface GenerateDeliveryReportUseCase {
	List<SensorReading> listAll();
    DeliveryReport generate(UUID shipmentId);
}
