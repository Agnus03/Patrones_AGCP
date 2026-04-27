package com.cadenasuministros.domain.port.out.proxy;

import com.cadenasuministros.domain.model.Shipment;
import com.cadenasuministros.domain.port.out.ShipmentRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ShipmentCacheProxy implements ShipmentRepository {

    private final ShipmentRepository realRepository;
    private final Map<UUID, CachedEntry<Shipment>> findByIdCache = new ConcurrentHashMap<>();
    private final Map<UUID, CachedEntry<List<Shipment>>> listAllShipmentsCache = new ConcurrentHashMap<>();
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
    public Optional<Shipment> findShipmentById(UUID id) {
        CachedEntry<Shipment> entry = findByIdCache.get(id);
        if (entry != null && !entry.isExpired()) {
            return Optional.of(entry.getValue());
        }

        Optional<Shipment> result = realRepository.findShipmentById(id);
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
    public List<Shipment> listAllShipments() {
        CachedEntry<List<Shipment>> entry = listAllShipmentsCache.get(GLOBAL_KEY);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }

        List<Shipment> result = realRepository.listAllShipments();
        listAllShipmentsCache.put(GLOBAL_KEY, new CachedEntry<>(result, cacheTtlMillis));
        return result;
    }

    private void invalidateAllCaches() {
        findByIdCache.clear();
        listAllShipmentsCache.clear();
    }

    public void clearCache() {
        invalidateAllCaches();
    }

    public int getCacheSize() {
        findByIdCache.entrySet().removeIf(e -> e.getValue().isExpired());
        listAllShipmentsCache.entrySet().removeIf(e -> e.getValue().isExpired());
        
        return findByIdCache.size() + listAllShipmentsCache.size();
    }

    private static class CachedEntry<T> {
        private final T value;
        private final long expiresAtMillis;

        CachedEntry(T value, long ttlMillis) {
            this.value = value;
            this.expiresAtMillis = System.currentTimeMillis() + ttlMillis;
        }

        T getValue() {
            return value;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expiresAtMillis;
        }
    }
}
