package com.cadenasuministros.domain.model.composite;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public sealed interface ShipmentComponent permits ShipmentItem, ShipmentPackage {

    UUID getId();

    String getName();

    BigDecimal getTotalValue();

    int getProductCount();

    List<String> getAllProductIds();

    ShipmentStatistics calculateStatistics();

    void addChild(ShipmentComponent component);

    void removeChild(UUID componentId);

    List<ShipmentComponent> getChildren();
}