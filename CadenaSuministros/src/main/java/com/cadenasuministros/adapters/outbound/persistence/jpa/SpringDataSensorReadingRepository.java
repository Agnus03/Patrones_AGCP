package com.cadenasuministros.adapters.outbound.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataSensorReadingRepository extends JpaRepository<SensorReadingJpaEntity, UUID> {}
