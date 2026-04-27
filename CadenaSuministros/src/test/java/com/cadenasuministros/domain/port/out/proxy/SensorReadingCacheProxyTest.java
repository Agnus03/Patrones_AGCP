package com.cadenasuministros.domain.port.out.proxy;

import com.cadenasuministros.domain.model.SensorReading;
import com.cadenasuministros.domain.port.out.SensorReadingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SensorReadingCacheProxyTest {

    @Mock
    private SensorReadingRepository realRepository;

    private SensorReadingCacheProxy proxy;

    @BeforeEach
    void setUp() {
        proxy = new SensorReadingCacheProxy(realRepository, 5000L);
    }

    @Test
    void test_save_invalidatesCache() {
        SensorReading reading = createSensorReading(UUID.randomUUID());
        when(realRepository.save(any())).thenReturn(reading);

        proxy.save(reading);

        verify(realRepository).save(reading);
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

        proxy.listAll();
        List<SensorReading> result = proxy.listAll();

        assertEquals(expected, result);
        verify(realRepository, times(1)).listAll();
    }

    @Test
    void test_findByShipmentId_firstCall_delegatesToRealRepository() {
        UUID shipmentId = UUID.randomUUID();
        List<SensorReading> expected = List.of(
            createSensorReading(shipmentId),
            createSensorReading(shipmentId)
        );
        when(realRepository.findByShipmentId(shipmentId)).thenReturn(expected);

        List<SensorReading> result = proxy.findByShipmentId(shipmentId);

        assertEquals(expected, result);
        verify(realRepository).findByShipmentId(shipmentId);
    }

    @Test
    void test_findByShipmentId_secondCall_returnsFromCache() {
        UUID shipmentId = UUID.randomUUID();
        List<SensorReading> expected = List.of(createSensorReading(shipmentId));
        when(realRepository.findByShipmentId(shipmentId)).thenReturn(expected);

        proxy.findByShipmentId(shipmentId);
        List<SensorReading> result = proxy.findByShipmentId(shipmentId);

        assertEquals(expected, result);
        verify(realRepository, times(1)).findByShipmentId(shipmentId);
    }

    @Test
    void test_findByShipmentId_differentShipmentIds_delegateSeparately() {
        UUID shipmentId1 = UUID.randomUUID();
        UUID shipmentId2 = UUID.randomUUID();
        List<SensorReading> expected1 = List.of(createSensorReading(shipmentId1));
        List<SensorReading> expected2 = List.of(createSensorReading(shipmentId2));

        when(realRepository.findByShipmentId(shipmentId1)).thenReturn(expected1);
        when(realRepository.findByShipmentId(shipmentId2)).thenReturn(expected2);

        List<SensorReading> result1 = proxy.findByShipmentId(shipmentId1);
        List<SensorReading> result2 = proxy.findByShipmentId(shipmentId2);

        assertEquals(expected1, result1);
        assertEquals(expected2, result2);
        verify(realRepository, times(1)).findByShipmentId(shipmentId1);
        verify(realRepository, times(1)).findByShipmentId(shipmentId2);
    }

    @Test
    void test_clearCache_removesAllCacheEntries() {
        UUID shipmentId = UUID.randomUUID();
        List<SensorReading> expected = List.of(createSensorReading(shipmentId));
        when(realRepository.listAll()).thenReturn(expected);
        when(realRepository.findByShipmentId(shipmentId)).thenReturn(expected);

        proxy.listAll();
        proxy.findByShipmentId(shipmentId);
        proxy.clearCache();

        proxy.listAll();
        proxy.findByShipmentId(shipmentId);

        verify(realRepository, times(2)).listAll();
        verify(realRepository, times(2)).findByShipmentId(shipmentId);
    }

    @Test
    void test_getCacheSize_returnsCorrectSize() {
        UUID shipmentId1 = UUID.randomUUID();
        UUID shipmentId2 = UUID.randomUUID();

        when(realRepository.listAll()).thenReturn(List.of());
        when(realRepository.findByShipmentId(shipmentId1)).thenReturn(List.of());
        when(realRepository.findByShipmentId(shipmentId2)).thenReturn(List.of());

        proxy.listAll();
        proxy.findByShipmentId(shipmentId1);
        proxy.findByShipmentId(shipmentId2);

        int cacheSize = proxy.getCacheSize();

        assertEquals(3, cacheSize);
    }

    @Test
    void test_save_invalidatesAllCaches() {
        SensorReading reading = createSensorReading(UUID.randomUUID());
        when(realRepository.listAll()).thenReturn(List.of());
        when(realRepository.findByShipmentId(any())).thenReturn(List.of());
        when(realRepository.save(any())).thenReturn(reading);

        proxy.listAll();
        proxy.findByShipmentId(UUID.randomUUID());

        proxy.save(reading);

        verify(realRepository).save(reading);
    }

    @Test
    void test_constructor_withDefaultTTL() {
        SensorReadingCacheProxy proxyWithDefault = new SensorReadingCacheProxy(realRepository);

        UUID shipmentId = UUID.randomUUID();
        List<SensorReading> expected = List.of(createSensorReading(shipmentId));
        when(realRepository.findByShipmentId(shipmentId)).thenReturn(expected);

        List<SensorReading> result = proxyWithDefault.findByShipmentId(shipmentId);

        assertEquals(expected, result);
    }

    @Test
    void test_constructor_withCustomTTL() {
        long customTtl = 10000L;
        SensorReadingCacheProxy proxyWithCustom = new SensorReadingCacheProxy(realRepository, customTtl);

        UUID shipmentId = UUID.randomUUID();
        List<SensorReading> expected = List.of(createSensorReading(shipmentId));
        when(realRepository.findByShipmentId(shipmentId)).thenReturn(expected);

        List<SensorReading> result = proxyWithCustom.findByShipmentId(shipmentId);

        assertEquals(expected, result);
    }

    @Test
    void test_listAll_withEmptyResult() {
        when(realRepository.listAll()).thenReturn(List.of());

        List<SensorReading> result = proxy.listAll();

        assertTrue(result.isEmpty());
    }

    @Test
    void test_findByShipmentId_withEmptyResult() {
        UUID shipmentId = UUID.randomUUID();
        when(realRepository.findByShipmentId(shipmentId)).thenReturn(List.of());

        List<SensorReading> result = proxy.findByShipmentId(shipmentId);

        assertTrue(result.isEmpty());
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