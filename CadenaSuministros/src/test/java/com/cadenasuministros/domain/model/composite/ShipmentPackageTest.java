package com.cadenasuministros.domain.model.composite;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ShipmentPackageTest {

    @Test
    void test_package_getTotalValue_sumsChildren() {
        ShipmentItem sensor1 = new ShipmentItem(
            UUID.randomUUID(),
            "SensorTemperatura",
            "TEMP-01",
            5,
            new BigDecimal("20.00")
        );
        ShipmentItem sensor2 = new ShipmentItem(
            UUID.randomUUID(),
            "SensorHumedad",
            "HUM-01",
            3,
            new BigDecimal("25.00")
        );

        ShipmentPackage contenedor = new ShipmentPackage(
            UUID.randomUUID(),
            "Contenedor 1",
            "CONTAINER"
        );
        contenedor.addChild(sensor1);
        contenedor.addChild(sensor2);

        assertEquals(new BigDecimal("175.00"), contenedor.getTotalValue());
    }

    @Test
    void test_package_getTotalValue_emptyPackage() {
        ShipmentPackage empty = new ShipmentPackage(
            UUID.randomUUID(),
            "EmptyBox",
            "BOX"
        );

        assertEquals(BigDecimal.ZERO, empty.getTotalValue());
    }

    @Test
    void test_package_getProductCount_sumsAllChildren() {
        ShipmentItem item1 = new ShipmentItem(
            UUID.randomUUID(),
            "Producto1",
            "PROD-001",
            5,
            BigDecimal.TEN
        );
        ShipmentItem item2 = new ShipmentItem(
            UUID.randomUUID(),
            "Producto2",
            "PROD-002",
            3,
            BigDecimal.TEN
        );

        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "Paquete",
            "PALLET"
        );
        paq.addChild(item1);
        paq.addChild(item2);

        assertEquals(8, paq.getProductCount());
    }

    @Test
    void test_package_getProductCount_singleChild() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Producto",
            "PROD-001",
            10,
            BigDecimal.TEN
        );

        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "SingleBox",
            "BOX"
        );
        paq.addChild(item);

        assertEquals(10, paq.getProductCount());
    }

    @Test
    void test_package_getAllProductIds_flattensList() {
        ShipmentItem i1 = new ShipmentItem(
            UUID.randomUUID(),
            "Item1",
            "ID-001",
            1,
            BigDecimal.ONE
        );
        ShipmentItem i2 = new ShipmentItem(
            UUID.randomUUID(),
            "Item2",
            "ID-002",
            1,
            BigDecimal.ONE
        );

        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "Caja",
            "BOX"
        );
        paq.addChild(i1);
        paq.addChild(i2);

        List<String> ids = paq.getAllProductIds();
        assertEquals(2, ids.size());
        assertTrue(ids.contains("ID-001"));
        assertTrue(ids.contains("ID-002"));
    }

    @Test
    void test_package_calculateStatistics_aggregates() {
        ShipmentItem item1 = new ShipmentItem(
            UUID.randomUUID(),
            "Producto1",
            "P1",
            10,
            new BigDecimal("10.00")
        );
        ShipmentItem item2 = new ShipmentItem(
            UUID.randomUUID(),
            "Producto2",
            "P2",
            5,
            new BigDecimal("20.00")
        );

        ShipmentPackage pkg = new ShipmentPackage(
            UUID.randomUUID(),
            "TestPackage",
            "TEST"
        );
        pkg.addChild(item1);
        pkg.addChild(item2);

        ShipmentStatistics stats = pkg.calculateStatistics();

        assertEquals(2, stats.totalItems());
        assertEquals(15, stats.totalProducts());
        assertEquals(new BigDecimal("200.00"), stats.totalValue());
    }

    @Test
    void test_package_calculateStatistics_emptyPackage() {
        ShipmentPackage empty = new ShipmentPackage(
            UUID.randomUUID(),
            "EmptyBox",
            "BOX"
        );

        ShipmentStatistics stats = empty.calculateStatistics();

        assertEquals(0, stats.totalItems());
        assertEquals(0, stats.totalProducts());
        assertEquals(BigDecimal.ZERO, stats.totalValue());
    }

    @Test
    void test_package_addChild_insertsCorrectly() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "SENSOR-001",
            1,
            BigDecimal.TEN
        );

        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "Box",
            "BOX"
        );

        paq.addChild(item);

        assertEquals(1, paq.getChildren().size());
    }

    @Test
    void test_package_removeChild_deletesCorrectly() {
        UUID childId = UUID.randomUUID();
        ShipmentItem child = new ShipmentItem(
            childId,
            "Child",
            "CHILD-001",
            1,
            BigDecimal.ONE
        );

        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "Package",
            "PKG"
        );
        paq.addChild(child);

        paq.removeChild(childId);

        assertTrue(paq.getChildren().isEmpty());
    }

    @Test
    void test_package_removeChild_nonExistentChild_doesNotThrow() {
        ShipmentItem child = new ShipmentItem(
            UUID.randomUUID(),
            "Child",
            "CHILD-001",
            1,
            BigDecimal.ONE
        );

        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "Package",
            "PKG"
        );
        paq.addChild(child);

        assertDoesNotThrow(() -> paq.removeChild(UUID.randomUUID()));
    }

    @Test
    void test_package_getChildren_returnsUnmodifiableList() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "SENSOR-001",
            1,
            BigDecimal.TEN
        );

        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "Box",
            "BOX"
        );
        paq.addChild(item);

        List<ShipmentComponent> children = paq.getChildren();

        assertEquals(1, children.size());
        assertThrows(UnsupportedOperationException.class, () ->
            children.add(new ShipmentItem(UUID.randomUUID(), "X", "X", 1, BigDecimal.ONE))
        );
    }

    @Test
    void test_package_getId_returnsCorrectId() {
        UUID id = UUID.randomUUID();
        ShipmentPackage paq = new ShipmentPackage(
            id,
            "Package",
            "PKG"
        );

        assertEquals(id, paq.getId());
    }

    @Test
    void test_package_getName_returnsCorrectName() {
        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "ContenedorPrincipal",
            "CONTAINER"
        );

        assertEquals("ContenedorPrincipal", paq.getName());
    }

    @Test
    void test_package_getPackageType_returnsCorrectType() {
        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "Box",
            "PALLET"
        );

        assertEquals("PALLET", paq.getPackageType());
    }

    @Test
    void test_package_withAddedChild_returnsNewPackage() {
        ShipmentItem item1 = new ShipmentItem(
            UUID.randomUUID(),
            "Item1",
            "ITEM-001",
            1,
            BigDecimal.ONE
        );
        ShipmentItem item2 = new ShipmentItem(
            UUID.randomUUID(),
            "Item2",
            "ITEM-002",
            1,
            BigDecimal.ONE
        );

        ShipmentPackage original = new ShipmentPackage(
            UUID.randomUUID(),
            "Original",
            "BOX"
        );
        original.addChild(item1);

        ShipmentPackage updated = original.withAddedChild(item2);

        assertNotSame(original, updated);
        assertEquals(1, original.getChildren().size());
        assertEquals(2, updated.getChildren().size());
    }

    @Test
    void test_package_withAddedChild_preservesOriginal() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Item",
            "ITEM-001",
            1,
            BigDecimal.ONE
        );

        ShipmentPackage original = new ShipmentPackage(
            UUID.randomUUID(),
            "Original",
            "BOX"
        );
        original.addChild(item);

        ShipmentPackage updated = original.withAddedChild(
            new ShipmentItem(UUID.randomUUID(), "Item2", "ITEM-002", 1, BigDecimal.ONE)
        );

        assertEquals(1, original.getChildren().size());
        assertEquals(2, updated.getChildren().size());
    }

    @Test
    void test_package_constructorWithChildren() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Item",
            "ITEM-001",
            1,
            BigDecimal.ONE
        );

        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "Box",
            "BOX",
            List.of(item)
        );

        assertEquals(1, paq.getChildren().size());
    }

    @Test
    void test_package_nestedPackage_calculatesRecursively() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Item",
            "ITEM-001",
            10,
            new BigDecimal("5.00")
        );

        ShipmentPackage smallBox = new ShipmentPackage(
            UUID.randomUUID(),
            "CajaChica",
            "BOX"
        );
        smallBox.addChild(item);

        ShipmentPackage largeBox = new ShipmentPackage(
            UUID.randomUUID(),
            "CajaGrande",
            "CONTAINER"
        );
        largeBox.addChild(smallBox);
        largeBox.addChild(
            new ShipmentItem(UUID.randomUUID(), "Item2", "ITEM-002", 5, new BigDecimal("10.00"))
        );

        assertEquals(new BigDecimal("100.00"), largeBox.getTotalValue());
        assertEquals(15, largeBox.getProductCount());
    }

    @Test
    void test_package_nestedStatistics_consolidates() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Item",
            "ITEM-001",
            10,
            new BigDecimal("5.00")
        );

        ShipmentPackage inner = new ShipmentPackage(UUID.randomUUID(), "Inner", "BOX");
        inner.addChild(item);

        ShipmentPackage outer = new ShipmentPackage(UUID.randomUUID(), "Outer", "CONTAINER");
        outer.addChild(inner);

        ShipmentStatistics stats = outer.calculateStatistics();

        assertEquals(1, stats.totalItems());
        assertEquals(10, stats.totalProducts());
    }

    @Test
    void test_package_emptyProductCount_isZero() {
        ShipmentPackage empty = new ShipmentPackage(
            UUID.randomUUID(),
            "EmptyBox",
            "BOX"
        );

        assertEquals(0, empty.getProductCount());
    }

    @Test
    void test_package_emptyTotalValue_isZero() {
        ShipmentPackage empty = new ShipmentPackage(
            UUID.randomUUID(),
            "EmptyBox",
            "BOX"
        );

        assertEquals(BigDecimal.ZERO, empty.getTotalValue());
    }

    @Test
    void test_package_emptyAllProductIds_returnsEmptyList() {
        ShipmentPackage empty = new ShipmentPackage(
            UUID.randomUUID(),
            "EmptyBox",
            "BOX"
        );

        assertTrue(empty.getAllProductIds().isEmpty());
    }

    @Test
    void test_package_addChild_acceptsNull() {
        ShipmentPackage paq = new ShipmentPackage(
            UUID.randomUUID(),
            "Box",
            "BOX"
        );

        assertDoesNotThrow(() ->
            paq.addChild(null)
        );
    }
}