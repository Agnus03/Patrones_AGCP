package com.cadenasuministros.domain.port.out.proxy;

import com.cadenasuministros.domain.model.SensorReading;
import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShipmentCacheProxy implements ShipmentRepository {

    private final ShipmentRepository realRepository;
    private final Map<UUID, CachedEntry<Shipment>> findByIdCache = new ConcurrentHashMap<>();
    private final Map<UUID, CachedEntry<List<SensorReading>>> listAllCache = new ConcurrentHashMap<>();
    private final long cacheTtlMillis;

    public ShipmentCacheProxy(ShipmentRepository realRepository, long cacheTtlMillis) {
        this.realRepository = realRepository;
        this.cacheTtlMillis = cacheTtlMillis;
    }

    public ShipmentCacheProxy(ShipmentRepository realRepository) {
        this(realRepository, 60000L);
    }

    private static final UUID GLOBAL_KEY = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Override
    public Optional<Shipment> findById(UUID id) {
        CachedEntry<Shipment> entry = findByIdCache.get(id);
        if (entry != null && !entry.isExpired()) {
            return Optional.of(entry.getValue());
        }

        Optional<Shipment> result = realRepository.findById(id);
        result.ifPresent(shipment -> 
            findByIdCache.put(id, new CachedEntry<>(shipment, cacheTtlMillis))
        );
        return result;
    }

    @Override
    public Shipment save(Shipment shipment) {
        invalidateAllCaches();
        return realRepository.save(shipment);
    }

    @Override
    public List<SensorReading> listAll() {
        CachedEntry<List<SensorReading>> entry = listAllCache.get(GLOBAL_KEY);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        List<SensorReading> result = realRepository.listAll();
        listAllCache.put(GLOBAL_KEY, new CachedEntry<>(result, cacheTtlMillis));
        return result;
    }

    private void invalidateAllCaches() {
        findByIdCache.clear();
        listAllCache.clear();
    }

    public void clearCache() {
        invalidateAllCaches();
    }

    public int getCacheSize() {
        findByIdCache.entrySet().removeIf(e -> e.getValue().isExpired());
        listAllCache.entrySet().removeIf(e -> e.getValue().isExpired());
        
        return findByIdCache.size() + listAllCache.size();
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