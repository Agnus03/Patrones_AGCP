package com.cadenasuministros.domain.port.out.proxy;

import com.cadenasuministros.domain.model.SensorReading;
import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.port.out.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentCacheProxyTest {

    @Mock
    private ShipmentRepository realRepository;

    private ShipmentCacheProxy proxy;

    @BeforeEach
    void setUp() {
        proxy = new ShipmentCacheProxy(realRepository, 5000L);
    }

    @Test
    void test_findById_firstCall_delegatesToRealRepository() {
        UUID shipmentId = UUID.randomUUID();
        Shipment expected = createShipment(shipmentId);
        when(realRepository.findById(shipmentId)).thenReturn(Optional.of(expected));

        Optional<Shipment> result = proxy.findById(shipmentId);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
        verify(realRepository).findById(shipmentId);
    }

    @Test
    void test_findById_secondCall_returnsFromCache() {
        UUID shipmentId = UUID.randomUUID();
        Shipment expected = createShipment(shipmentId);
        when(realRepository.findById(shipmentId)).thenReturn(Optional.of(expected));

        Optional<Shipment> result1 = proxy.findById(shipmentId);
        Optional<Shipment> result2 = proxy.findById(shipmentId);

        assertTrue(result1.isPresent());
        assertTrue(result2.isPresent());
        assertEquals(expected, result1.get());
        assertEquals(expected, result2.get());
        verify(realRepository, times(1)).findById(shipmentId);
    }

    @Test
    void test_findById_notFound_delegatesToRealRepository() {
        UUID shipmentId = UUID.randomUUID();
        when(realRepository.findById(shipmentId)).thenReturn(Optional.empty());

        Optional<Shipment> result = proxy.findById(shipmentId);

        assertFalse(result.isPresent());
        verify(realRepository).findById(shipmentId);
    }

    @Test
    void test_findById_differentIds_delegateSeparately() {
        UUID shipmentId1 = UUID.randomUUID();
        UUID shipmentId2 = UUID.randomUUID();
        Shipment expected1 = createShipment(shipmentId1);
        Shipment expected2 = createShipment(shipmentId2);

        when(realRepository.findById(shipmentId1)).thenReturn(Optional.of(expected1));
        when(realRepository.findById(shipmentId2)).thenReturn(Optional.of(expected2));

        Optional<Shipment> result1 = proxy.findById(shipmentId1);
        Optional<Shipment> result2 = proxy.findById(shipmentId2);

        assertEquals(expected1, result1.get());
        assertEquals(expected2, result2.get());
        verify(realRepository, times(1)).findById(shipmentId1);
        verify(realRepository, times(1)).findById(shipmentId2);
    }

    @Test
    void test_save_invalidatesCache() {
        Shipment shipment = createShipment(UUID.randomUUID());
        when(realRepository.save(any())).thenReturn(shipment);

        proxy.save(shipment);

        verify(realRepository).save(shipment);
    }

    @Test
    void test_listAll_firstCall_delegatesToRealRepository() {
        List<SensorReading> expected = List.of(
            createSensorReading(UUID.randomUUID()),
            createSensorReading(UUID.randomUUID())
        );
        when(realRepository.listAll()).thenReturn(expected);

        List<SensorReading> result = proxy.listAll();

        assertEquals(expected, result);
        verify(realRepository).listAll();
    }

    @Test
    void test_listAll_secondCall_returnsFromCache() {
        List<SensorReading> expected = List.of(createSensorReading(UUID.randomUUID()));
        when(realRepository.listAll()).thenReturn(expected);

        List<SensorReading> result1 = proxy.listAll();
        List<SensorReading> result2 = proxy.listAll();

        assertEquals(expected, result1);
        assertEquals(expected, result2);
        verify(realRepository, times(1)).listAll();
    }

    @Test
    void test_clearCache_removesAllCacheEntries() {
        UUID shipmentId = UUID.randomUUID();
        Shipment shipment = createShipment(shipmentId);
        List<SensorReading> readings = List.of(createSensorReading(shipmentId));

        when(realRepository.findById(shipmentId)).thenReturn(Optional.of(shipment));
        when(realRepository.listAll()).thenReturn(readings);

        proxy.findById(shipmentId);
        proxy.listAll();
        proxy.clearCache();

        proxy.findById(shipmentId);
        proxy.listAll();

        verify(realRepository, times(2)).findById(shipmentId);
        verify(realRepository, times(2)).listAll();
    }

    @Test
    void test_getCacheSize_returnsCorrectSize() {
        UUID shipmentId1 = UUID.randomUUID();
        UUID shipmentId2 = UUID.randomUUID();
        Shipment shipment1 = createShipment(shipmentId1);
        Shipment shipment2 = createShipment(shipmentId2);

        when(realRepository.findById(shipmentId1)).thenReturn(Optional.of(shipment1));
        when(realRepository.findById(shipmentId2)).thenReturn(Optional.of(shipment2));
        when(realRepository.listAll()).thenReturn(List.of());

        proxy.findById(shipmentId1);
        proxy.findById(shipmentId2);
        proxy.listAll();

        int cacheSize = proxy.getCacheSize();

        assertEquals(3, cacheSize);
    }

    @Test
    void test_save_invalidatesAllCaches() {
        Shipment shipment = createShipment(UUID.randomUUID());
        List<SensorReading> readings = List.of(createSensorReading(shipment.id()));

        when(realRepository.findById(any())).thenReturn(Optional.of(shipment));
        when(realRepository.listAll()).thenReturn(readings);
        when(realRepository.save(any())).thenReturn(shipment);

        proxy.findById(shipment.id());
        proxy.listAll();

        proxy.save(shipment);

        verify(realRepository).save(shipment);
    }

    @Test
    void test_constructor_withDefaultTTL() {
        ShipmentCacheProxy proxyWithDefault = new ShipmentCacheProxy(realRepository);

        UUID shipmentId = UUID.randomUUID();
        Shipment expected = createShipment(shipmentId);
        when(realRepository.findById(shipmentId)).thenReturn(Optional.of(expected));

        Optional<Shipment> result = proxyWithDefault.findById(shipmentId);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void test_constructor_withCustomTTL() {
        long customTtl = 10000L;
        ShipmentCacheProxy proxyWithCustom = new ShipmentCacheProxy(realRepository, customTtl);

        UUID shipmentId = UUID.randomUUID();
        Shipment expected = createShipment(shipmentId);
        when(realRepository.findById(shipmentId)).thenReturn(Optional.of(expected));

        Optional<Shipment> result = proxyWithCustom.findById(shipmentId);

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
    }

    @Test
    void test_listAll_withEmptyResult() {
        when(realRepository.listAll()).thenReturn(List.of());

        List<SensorReading> result = proxy.listAll();

        assertTrue(result.isEmpty());
    }

    private Shipment createShipment(UUID shipmentId) {
        return new Shipment(
            shipmentId,
            UUID.randomUUID(),
            "IN_TRANSIT",
            "BOGOTA",
            Instant.now()
        );
    }

    private SensorReading createSensorReading(UUID shipmentId) {
        return new SensorReading(
            UUID.randomUUID(),
            shipmentId,
            Instant.now(),
            25.0,
            60.0,
            40.4,
            -3.7
        );
    }
}