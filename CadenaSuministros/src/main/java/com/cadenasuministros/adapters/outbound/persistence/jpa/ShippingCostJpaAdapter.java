package com.cadenasuministros.adapters.outbound.persistence.jpa;

import com.cadenasuministros.domain.model.ShippingCost;
import com.cadenasuministros.domain.port.out.ShippingCostRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ShippingCostJpaAdapter extends AbstractJpaAdapter<ShippingCost, ShippingCostJpaEntity>
        implements ShippingCostRepository {

    private final SpringDataShippingCostRepository costRepo;

    public ShippingCostJpaAdapter(SpringDataShippingCostRepository repo) {
        super(repo);
        this.costRepo = repo;
    }

    @Override
    protected ShippingCostJpaEntity toEntity(ShippingCost d) {
        return new ShippingCostJpaEntity(d.id(), d.shipmentId(), d.baseRate(),
                d.distanceKm(), d.distanceCost(), d.extraCharges(),
                d.totalCost(), d.currency(), d.calculatedAt(), d.strategyName());
    }

    @Override
    protected ShippingCost toDomain(ShippingCostJpaEntity e) {
        String sn = e.getStrategyName();
        if (sn == null || sn.isBlank()) sn = "Standard";
        return new ShippingCost(e.getId(), e.getShipmentId(), e.getBaseRate(),
                e.getDistanceKm(), e.getDistanceCost(), e.getExtraCharges(),
                e.getTotalCost(), e.getCurrency(), e.getCalculatedAt(),
                sn);
    }

    @Override
    public Optional<ShippingCost> findByShipmentId(UUID shipmentId) {
        return costRepo.findByShipmentId(shipmentId).map(this::toDomain);
    }

    @Override
    public List<ShippingCost> listAll() {
        return findAll();
    }

}
