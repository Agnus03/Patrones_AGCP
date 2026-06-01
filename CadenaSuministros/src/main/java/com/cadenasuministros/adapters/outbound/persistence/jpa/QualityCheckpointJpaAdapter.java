package com.cadenasuministros.adapters.outbound.persistence.jpa;

import com.cadenasuministros.domain.model.QualityCheckpoint;
import com.cadenasuministros.domain.port.out.QualityCheckpointRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class QualityCheckpointJpaAdapter extends AbstractJpaAdapter<QualityCheckpoint, QualityCheckpointJpaEntity>
        implements QualityCheckpointRepository {

    private final SpringDataQualityCheckpointRepository checkpointRepo;

    public QualityCheckpointJpaAdapter(SpringDataQualityCheckpointRepository repo) {
        super(repo);
        this.checkpointRepo = repo;
    }

    @Override
    protected QualityCheckpointJpaEntity toEntity(QualityCheckpoint d) {
        return new QualityCheckpointJpaEntity(d.id(), d.shipmentId(), d.location(),
                d.temperatureC(), d.humidityPct(), d.passed(),
                d.notes(), d.inspector(), d.timestamp());
    }

    @Override
    protected QualityCheckpoint toDomain(QualityCheckpointJpaEntity e) {
        return new QualityCheckpoint(e.getId(), e.getShipmentId(), e.getLocation(),
                e.getTemperatureC(), e.getHumidityPct(), e.isPassed(),
                e.getNotes(), e.getInspector(), e.getTimestamp());
    }

    @Override
    public Optional<QualityCheckpoint> findById(UUID id) {
        return super.findById(id);
    }

    @Override
    public List<QualityCheckpoint> findByShipmentId(UUID shipmentId) {
        return checkpointRepo.findByShipmentIdOrderByTimestampDesc(shipmentId).stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public List<QualityCheckpoint> listAll() {
        return findAll();
    }

    @Override
    public List<QualityCheckpoint> findFailedCheckpoints() {
        return checkpointRepo.findByPassedFalse().stream()
                .map(this::toDomain).collect(Collectors.toList());
    }

}
