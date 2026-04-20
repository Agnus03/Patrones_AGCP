package com.cadenasuministros.domain.model.composite;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ShipmentItemTest {

    @Test
    void test_item_getTotalValue_calculatesCorrectly() {
        ShipmentItem sensor = new ShipmentItem(
            UUID.randomUUID(),
            "SensorTemp-DHT22",
            "SENSOR-TEMP-001",
            10,
            new BigDecimal("15.50")
        );

        assertEquals(new BigDecimal("155.00"), sensor.getTotalValue());
    }

    @Test
    void test_item_getProductCount_returnsQuantity() {
        ShipmentItem producto = new ShipmentItem(
            UUID.randomUUID(),
            "ProductoElectronico",
            "PROD-ELEC-001",
            25,
            new BigDecimal("100.00")
        );

        assertEquals(25, producto.getProductCount());
    }

    @Test
    void test_item_getProductCount_withSingleUnit() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "SENSOR-001",
            1,
            new BigDecimal("50.00")
        );

        assertEquals(1, item.getProductCount());
    }

    @Test
    void test_item_getAllProductIds_returnsSingleId() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "SensorTemperatura",
            "SENSOR-TEMP-001",
            1,
            new BigDecimal("20.00")
        );

        List<String> ids = item.getAllProductIds();
        assertEquals(1, ids.size());
        assertTrue(ids.contains("SENSOR-TEMP-001"));
    }

    @Test
    void test_item_getAllProductIds_multipleQuantity() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "SensorGPS",
            "SENSOR-GPS-001",
            5,
            new BigDecimal("100.00")
        );

        List<String> ids = item.getAllProductIds();
        assertEquals(1, ids.size());
    }

    @Test
    void test_item_calculateStatistics_singleItem() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Controller",
            "CTRL-001",
            10,
            new BigDecimal("50.00")
        );

        ShipmentStatistics stats = item.calculateStatistics();

        assertEquals(1, stats.totalItems());
        assertEquals(10, stats.totalProducts());
        assertEquals(new BigDecimal("500.00"), stats.totalValue());
    }

    @Test
    void test_item_calculateStatistics_withZeroValue() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sample",
            "SAMPLE-001",
            0,
            BigDecimal.ZERO
        );

        ShipmentStatistics stats = item.calculateStatistics();

        assertEquals(0, stats.totalProducts());
        assertEquals(BigDecimal.ZERO, stats.totalValue());
    }

    @Test
    void test_item_addChild_throwsUnsupportedOperationException() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "SENSOR-001",
            1,
            new BigDecimal("10.00")
        );

        ShipmentItem anotherItem = new ShipmentItem(
            UUID.randomUUID(),
            "OtroSensor",
            "SENSOR-002",
            1,
            BigDecimal.ONE
        );

        assertThrows(UnsupportedOperationException.class, () ->
            item.addChild(anotherItem)
        );
    }

    @Test
    void test_item_removeChild_throwsUnsupportedOperationException() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "SENSOR-001",
            1,
            BigDecimal.TEN
        );

        assertThrows(UnsupportedOperationException.class, () ->
            item.removeChild(UUID.randomUUID())
        );
    }

    @Test
    void test_item_getChildren_returnsEmptyList() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "SENSOR-001",
            1,
            BigDecimal.TEN
        );

        assertTrue(item.getChildren().isEmpty());
    }

    @Test
    void test_item_getId_returnsCorrectId() {
        UUID id = UUID.randomUUID();
        ShipmentItem item = new ShipmentItem(
            id,
            "Sensor",
            "SENSOR-001",
            1,
            BigDecimal.TEN
        );

        assertEquals(id, item.getId());
    }

    @Test
    void test_item_getName_returnsCorrectName() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "SensorTemperaturaDHT22",
            "SENSOR-001",
            1,
            BigDecimal.TEN
        );

        assertEquals("SensorTemperaturaDHT22", item.getName());
    }

    @Test
    void test_item_getProductId_returnsCorrectId() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "PROD-TEMP-001",
            1,
            BigDecimal.TEN
        );

        assertEquals("PROD-TEMP-001", item.getProductId());
    }

    @Test
    void test_item_getQuantity_returnsCorrectQuantity() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "SENSOR-001",
            100,
            BigDecimal.ONE
        );

        assertEquals(100, item.getQuantity());
    }

    @Test
    void test_item_getUnitPrice_returnsCorrectPrice() {
        BigDecimal price = new BigDecimal("99.99");
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "SENSOR-001",
            1,
            price
        );

        assertEquals(price, item.getUnitPrice());
    }

    @Test
    void test_item_getStatus_returnsDefaultStatus() {
        ShipmentItem item = new ShipmentItem(
            UUID.randomUUID(),
            "Sensor",
            "SENSOR-001",
            1,
            BigDecimal.TEN
        );

        assertEquals("PENDING", item.getStatus());
    }

    @Test
    void test_item_withStatus_createsNewInstance() {
        UUID id = UUID.randomUUID();
        ShipmentItem original = new ShipmentItem(
            id,
            "Sensor",
            "SENSOR-001",
            1,
            BigDecimal.TEN
        );

        ShipmentItem updated = original.withStatus("IN_TRANSIT");

        assertNotSame(original, updated);
        assertEquals(id, updated.getId());
    }
}
