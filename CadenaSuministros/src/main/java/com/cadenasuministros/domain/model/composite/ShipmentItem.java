package com.cadenasuministros.domain.model.composite;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public final class ShipmentItem implements ShipmentComponent {

    private final UUID id;
    private final String name;
    private final String productId;
    private final int quantity;
    private final BigDecimal unitPrice;
    private final String status;

    public ShipmentItem(UUID id, String name, String productId, int quantity, BigDecimal unitPrice) {
        this.id = id;
        this.name = name;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.status = "PENDING";
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public BigDecimal getTotalValue() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    public int getProductCount() {
        return quantity;
    }

    @Override
    public List<String> getAllProductIds() {
        return List.of(productId);
    }

    @Override
    public ShipmentStatistics calculateStatistics() {
        return new ShipmentStatistics(
            1,
            quantity,
            getTotalValue(),
           BigDecimal.ZERO,
            BigDecimal.ZERO,
            0,
            0
        );
    }

    @Override
    public void addChild(ShipmentComponent component) {
        throw new UnsupportedOperationException("Item no puede tener hijos");
    }

    @Override
    public void removeChild(UUID componentId) {
        throw new UnsupportedOperationException("Item no puede tener hijos");
    }

    @Override
    public List<ShipmentComponent> getChildren() {
        return List.of();
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getStatus() {
        return status;
    }

    public ShipmentItem withStatus(String newStatus) {
        return new ShipmentItem(id, name, productId, quantity, unitPrice);
    }
}