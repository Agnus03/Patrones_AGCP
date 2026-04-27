package com.cadenasuministros.adapters.outbound.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataSensorReadingRepository extends JpaRepository<SensorReadingJpaEntity, UUID> {
    List<SensorReadingJpaEntity> findByShipmentId(UUID shipmentId);
}
