package com.cadenasuministros.adapters.outbound.persistence.jpa;

import org.springframework.stereotype.Component;
import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.model.SensorReading;
import com.cadenasuministros.domain.port.out.SensorReadingRepository;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class JpaAdapters implements ShipmentRepository, SensorReadingRepository {

    private final SpringDataShipmentRepository shipmentRepo;
    private final SpringDataSensorReadingRepository sensorRepo;

    public JpaAdapters(SpringDataShipmentRepository shipmentRepo,
                       SpringDataSensorReadingRepository sensorRepo) {
        this.shipmentRepo = shipmentRepo;
        this.sensorRepo = sensorRepo;
    }

    @Override
    public Optional<Shipment> findById(UUID id) {
        return shipmentRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Shipment save(Shipment shipment) {
        ShipmentJpaEntity e = toEntity(shipment);
        ShipmentJpaEntity saved = shipmentRepo.save(e);
        return toDomain(saved);
    }

    @Override
    public SensorReading save(SensorReading reading) {
        SensorReadingJpaEntity e = toEntity(reading);
        SensorReadingJpaEntity saved = sensorRepo.save(e);
        return toDomain(saved);
    }

    private Shipment toDomain(ShipmentJpaEntity e) {
        return new Shipment(e.id, e.productId, e.status, e.currentLocation, e.updatedAt);
    }

    private ShipmentJpaEntity toEntity(Shipment d) {
        ShipmentJpaEntity e = new ShipmentJpaEntity();
        e.id = d.id();
        e.productId = d.productId();
        e.status = d.status();
        e.currentLocation = d.currentLocation();
        e.updatedAt = d.updatedAt();
        return e;
    }

    private SensorReading toDomain(SensorReadingJpaEntity e) {
        return new SensorReading(e.id, e.shipmentId, e.timestamp, e.temperatureC, e.humidityPct, e.latitude, e.longitude);
    }

    private SensorReadingJpaEntity toEntity(SensorReading d) {
        SensorReadingJpaEntity e = new SensorReadingJpaEntity();
        e.id = d.id();
        e.shipmentId = d.shipmentId();
        e.timestamp = d.timestamp();
        e.temperatureC = d.temperatureC();
        e.humidityPct = d.humidityPct();
        e.latitude = d.latitude();
        e.longitude = d.longitude();
        return e;
    }

    @Override
    public List<SensorReading> listAll() {
        return sensorRepo.findAll().stream()  // JpaEntities
            .map(this::toDomain)              // ← Convertir a Domain
            .collect(Collectors.toList());
    }

}
