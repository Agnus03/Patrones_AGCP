package com.cadenasuministros.domain.model.composite;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class ShipmentPackage implements ShipmentComponent {

    private final UUID id;
    private final String name;
    private final String packageType;
    private final List<ShipmentComponent> children;

    public ShipmentPackage(UUID id, String name, String packageType) {
        this.id = id;
        this.name = name;
        this.packageType = packageType;
        this.children = new ArrayList<>();
    }

    public ShipmentPackage(UUID id, String name, String packageType, List<ShipmentComponent> children) {
        this.id = id;
        this.name = name;
        this.packageType = packageType;
        this.children = new ArrayList<>(children);
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
        return children.stream()
            .map(ShipmentComponent::getTotalValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public int getProductCount() {
        return children.stream()
            .mapToInt(ShipmentComponent::getProductCount)
            .sum();
    }

    @Override
    public List<String> getAllProductIds() {
        return children.stream()
            .flatMap(c -> c.getAllProductIds().stream())
            .toList();
    }

    @Override
    public ShipmentStatistics calculateStatistics() {
        int itemCount = 0;
        int productCount = 0;
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal maxValue = BigDecimal.ZERO;
        BigDecimal minValue = BigDecimal.ZERO;
        int deliveredCount = 0;
        int pendingCount = 0;

        for (ShipmentComponent child : children) {
            ShipmentStatistics stats = child.calculateStatistics();
            itemCount += stats.totalItems();
            productCount += stats.totalProducts();
            totalValue = totalValue.add(stats.totalValue());
            
            if (stats.maxValue().compareTo(maxValue) > 0) {
                maxValue = stats.maxValue();
            }
            if (minValue.equals(BigDecimal.ZERO) || stats.minValue().compareTo(minValue) < 0) {
                minValue = stats.minValue();
            }
            deliveredCount += stats.deliveredCount();
            pendingCount += stats.pendingCount();
        }

        return new ShipmentStatistics(
            itemCount,
            productCount,
            totalValue,
            maxValue,
            minValue,
            deliveredCount,
            pendingCount
        );
    }

    @Override
    public void addChild(ShipmentComponent component) {
        children.add(component);
    }

    @Override
    public void removeChild(UUID componentId) {
        children.removeIf(c -> c.getId().equals(componentId));
    }

    @Override
    public List<ShipmentComponent> getChildren() {
        return List.copyOf(children);
    }

    public String getPackageType() {
        return packageType;
    }

    public ShipmentPackage withAddedChild(ShipmentComponent component) {
        List<ShipmentComponent> newChildren = new ArrayList<>(children);
        newChildren.add(component);
        return new ShipmentPackage(id, name, packageType, newChildren);
    }
}