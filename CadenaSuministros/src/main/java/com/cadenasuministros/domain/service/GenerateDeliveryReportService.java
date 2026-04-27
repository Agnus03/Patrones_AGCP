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
        Shipment shipment = shipmentRepository.findShipmentById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Shipment no encontrado: " + shipmentId));

        List<SensorReading> readings = sensorReadingRepository.findByShipmentId(shipmentId);
        
        // Si no hay lecturas, usar valores por defecto
        if (readings == null || readings.isEmpty()) {
            System.out.println("No se encontraron lecturas para shipment: " + shipmentId);
        }

        DoubleSummaryStatistics tempStats = readings.stream()
                .filter(r -> r.temperatureC() != null)
                .mapToDouble(SensorReading::temperatureC)
                .summaryStatistics();

        DoubleSummaryStatistics humStats = readings.stream()
                .filter(r -> r.humidityPct() != null)
                .mapToDouble(SensorReading::humidityPct)
                .summaryStatistics();

        Double avgTemp = tempStats.getCount() > 0 ? tempStats.getAverage() : 0.0;
        Double avgHum  = humStats.getCount()  > 0 ? humStats.getAverage()  : 0.0;

        boolean tempAlert = avgTemp != 0.0 && (avgTemp < 2 || avgTemp > 30);
        boolean humAlert  = avgHum  != 0.0 && (avgHum  < 30 || avgHum  > 80);

        DeliveryReport report = new DeliveryReport.Builder()
                .reportId(UUID.randomUUID())
                .shipmentId(shipment.id())
                .productId(shipment.productId()) // Obtener productId del shipment
                .origin("Bodega origen")
                .destination(shipment.currentLocation())
                .dispatchTime(shipment.updatedAt())
                .deliveryTime(Instant.now())
                .averageTemperature(avgTemp)
                .averageHumidity(avgHum)
                .temperatureAlert(tempAlert)
                .humidityAlert(humAlert)
                .deliveryStatus(shipment.status())
                .observations(buildObservations(tempAlert, humAlert, readings.isEmpty()))
                .build();

        return deliveryReportRepository.save(report);
    }

    private String buildObservations(boolean tempAlert, boolean humAlert, boolean noReadings) {
        if (noReadings) return "No hay lecturas de sensores registradas";
        if (tempAlert && humAlert) return "Alerta de temperatura y humedad";
        if (tempAlert) return "Alerta de temperatura";
        if (humAlert) return "Alerta de humedad";
        return "Entrega dentro de parámetros";
    }

	@Override
	public List<SensorReading> listAll() {
		return sensorReadingRepository.listAll();
	}
}
