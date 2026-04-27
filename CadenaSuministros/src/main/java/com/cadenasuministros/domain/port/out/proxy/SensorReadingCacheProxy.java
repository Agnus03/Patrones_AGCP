package com.cadenasuministros.domain.port.out.proxy;

import com.cadenasuministros.domain.model.SensorReading;
import com.cadenasuministros.domain.port.out.SensorReadingRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SensorReadingCacheProxy implements SensorReadingRepository {

    private final SensorReadingRepository realRepository;
    private final Map<String, CachedEntry<List<SensorReading>>> listAllCache = new ConcurrentHashMap<>();
    private final Map<UUID, CachedEntry<List<SensorReading>>> byShipmentIdCache = new ConcurrentHashMap<>();
    private final long cacheTtlMillis;

    public SensorReadingCacheProxy(SensorReadingRepository realRepository, long cacheTtlMillis) {
        this.realRepository = realRepository;
        this.cacheTtlMillis = cacheTtlMillis;
    }

    public SensorReadingCacheProxy(SensorReadingRepository realRepository) {
        this(realRepository, 60000L);
    }

    @Override
    public SensorReading save(SensorReading reading) {
        invalidateAllCaches();
        return realRepository.save(reading);
    }

    @Override
    public List<SensorReading> listAll() {
        CachedEntry<List<SensorReading>> entry = listAllCache.get("global");
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        List<SensorReading> result = realRepository.listAll();
        listAllCache.put("global", new CachedEntry<>(result, cacheTtlMillis));
        return result;
    }

    @Override
    public List<SensorReading> findByShipmentId(UUID shipmentId) {
        CachedEntry<List<SensorReading>> entry = byShipmentIdCache.get(shipmentId);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        List<SensorReading> result = realRepository.findByShipmentId(shipmentId);
        byShipmentIdCache.put(shipmentId, new CachedEntry<>(result, cacheTtlMillis));
        return result;
    }

    private void invalidateAllCaches() {
        listAllCache.clear();
        byShipmentIdCache.clear();
    }

    public void clearCache() {
        invalidateAllCaches();
    }

    public int getCacheSize() {
        listAllCache.entrySet().removeIf(e -> e.getValue().isExpired());
        byShipmentIdCache.entrySet().removeIf(e -> e.getValue().isExpired());
        
        return listAllCache.size() + byShipmentIdCache.size();
    }

    private static class CachedEntry<T> {
        private final T value;
        private final Instant expiresAt;

        CachedEntry(T value, long ttlMillis) {
            this.value = value;
            this.expiresAt = Instant.now().plusMillis(ttlMillis);
        }

        T getValue() {
            return value;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }
}