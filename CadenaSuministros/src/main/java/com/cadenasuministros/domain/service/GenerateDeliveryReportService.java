package com.cadenasuministros.domain.service;

import com.cadenasuministros.domain.model.DeliveryReport;
import com.cadenasuministros.domain.model.SensorReading;
import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.port.in.GenerateDeliveryReportUseCase;
import com.cadenasuministros.domain.port.out.DeliveryReportRepository;
import com.cadenasuministros.domain.port.out.SensorReadingRepository;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

import java.time.Instant;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.UUID;

public class GenerateDeliveryReportService implements GenerateDeliveryReportUseCase {

    private final ShipmentRepository shipmentRepository;
    private final SensorReadingRepository sensorReadingRepository;
    private final DeliveryReportRepository deliveryReportRepository;

    public GenerateDeliveryReportService(ShipmentRepository shipmentRepository,
                                         SensorReadingRepository sensorReadingRepository,
                                         DeliveryReportRepository deliveryReportRepository) {
        this.shipmentRepository = shipmentRepository;
        this.sensorReadingRepository = sensorReadingRepository;
        this.deliveryReportRepository = deliveryReportRepository;
    }

    @Override
    public DeliveryReport generate(UUID shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment no encontrado: " + shipmentId));

        List<SensorReading> readings = sensorReadingRepository.findByShipmentId(shipmentId);

        DoubleSummaryStatistics tempStats = readings.stream()
                .filter(r -> r.temperatureC() != null)
                .mapToDouble(SensorReading::temperatureC)
                .summaryStatistics();

        DoubleSummaryStatistics humStats = readings.stream()
                .filter(r -> r.humidityPct() != null)
                .mapToDouble(SensorReading::humidityPct)
                .summaryStatistics();

        Double avgTemp = tempStats.getCount() > 0 ? tempStats.getAverage() : null;
        Double avgHum  = humStats.getCount()  > 0 ? humStats.getAverage()  : null;

        boolean tempAlert = avgTemp != null && (avgTemp < 2 || avgTemp > 8);
        boolean humAlert  = avgHum  != null && (avgHum  < 40 || avgHum  > 80);

        // Aquí usas TU Builder
        DeliveryReport report = new DeliveryReport.Builder()
                .reportId(UUID.randomUUID())
                .shipmentId(shipment.id())
                .productId(shipment.productId())
                .origin("Bodega origen")              // puedes mapear desde shipment si tienes
                .destination(shipment.currentLocation())
                .dispatchTime(shipment.updatedAt())   // o un campo específico
                .deliveryTime(Instant.now())
                .averageTemperature(avgTemp)
                .averageHumidity(avgHum)
                .temperatureAlert(tempAlert)
                .humidityAlert(humAlert)
                .deliveryStatus(shipment.status())
                .observations(buildObservations(tempAlert, humAlert))
                .build();

        return deliveryReportRepository.save(report);
    }

    private String buildObservations(boolean tempAlert, boolean humAlert) {
        if (tempAlert && humAlert) return "Alerta de temperatura y humedad";
        if (tempAlert) return "Alerta de temperatura";
        if (humAlert) return "Alerta de humedad";
        return "Entrega dentro de parámetros";
    }

	@Override
	public List<SensorReading> listAll() {
		// TODO Auto-generated method stub
		return null;
	}
}
