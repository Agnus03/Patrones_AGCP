# UML del Patrón Proxy - CadenaSuministros

```plantuml
@startuml
skinparam componentStyle uml2

' ============================================
' PATRON PROXY - CADENA SUMINISTROS
' ============================================

package "domain.port.out" {

    interface "SensorReadingRepository" <<interface>> {
        + save(reading: SensorReading): SensorReading
        + listAll(): List<SensorReading>
        + findByShipmentId(shipmentId: UUID): List<SensorReading>
    }

    interface "ShipmentRepository" <<interface>> {
        + findById(id: UUID): Optional<Shipment>
        + save(shipment: Shipment): Shipment
        + listAll(): List<SensorReading>
    }
}

package "domain.port.out.proxy" {

    class "SensorReadingCacheProxy" {
        - realRepository: SensorReadingRepository
        - listAllCache: Map<String, CachedEntry<List<SensorReading>>>
        - byShipmentIdCache: Map<UUID, CachedEntry<List<SensorReading>>>
        - cacheTtlMillis: long
        + SensorReadingCacheProxy(realRepository, ttlMillis)
        + save(reading: SensorReading): SensorReading
        + listAll(): List<SensorReading>
        + findByShipmentId(shipmentId: UUID): List<SensorReading>
        + clearCache(): void
        + getCacheSize(): int
    }

    class "ShipmentCacheProxy" {
        - realRepository: ShipmentRepository
        - findByIdCache: Map<UUID, CachedEntry<Shipment>>>
        - listAllCache: Map<UUID, CachedEntry<List<SensorReading>>>
        - cacheTtlMillis: long
        + ShipmentCacheProxy(realRepository, ttlMillis)
        + findById(id: UUID): Optional<Shipment>
        + save(shipment: Shipment): Shipment
        + listAll(): List<SensorReading>
        + clearCache(): void
        + getCacheSize(): int
    }

    class "CachedEntry" <<static>> {
        - value: T
        - expiresAt: Instant
        + CachedEntry(value: T, ttlMillis: long)
        + getValue(): T
        + isExpired(): boolean
    }
}

' Relaciones
SensorReadingRepository <|.. SensorReadingCacheProxy
SensorReadingCacheProxy o-- "uses" SensorReadingRepository
SensorReadingCacheProxy --> CachedEntry : creates

ShipmentRepository <|.. ShipmentCacheProxy
ShipmentCacheProxy o-- "uses" ShipmentRepository
ShipmentCacheProxy --> CachedEntry : creates

@enduml
```

---

## Diagrama de Flujo de Datos

```plantuml
@startuml
skinparam componentStyle uml2

' ============================================
' FLUJO DE DATOS - CACHE PROXY
' ============================================

actor "Cliente" as client

database "Base de Datos" as db

collections "SensorReadingCacheProxy" as proxy {
    collections "listAllCache" as cache1 {
        [key: "global"]
        [value: CachedEntry]
    }
    collections "byShipmentIdCache" as cache2 {
        [key: UUID1]
        [value: CachedEntry]
        [key: UUID2]
        [value: CachedEntry]
    }
}

' Flujo clientes -> proxy
client -> proxy: findByShipmentId(id)

alt Primera llamada (cache miss)
    proxy -> db: findByShipmentId()
    db --> proxy: List<SensorReading>
    proxy -> proxy: guardando en cache
    proxy --> client: List<SensorReading>
else Llamada siguiente (cache hit)
    proxy -> proxy: buscando en cache
    proxy --> client: List<SensorReading> (desde cache)
end

@enduml
```

---

## Diagrama de Secuencia - Primera vs Segunda Llamada

```plantuml
@startuml
skinparam componentStyle uml2

' ============================================
' SECUENCIA - CACHE HIT VS MISS
' ============================================

actor "Cliente" as client
participant "Proxy" as proxy
participant "Cache" as cache
database "DB" as db

== Primera Llamada (Cache Miss) ==

client -> proxy: findByShipmentId(id)
proxy -> cache: get(id)
cache --> proxy: null
proxy -> db: SELECT * FROM readings WHERE shipment_id = ?
db --> proxy: List<SensorReading>

note right: Primera vez
proxy -> proxy: guardarEnCache(id, result)
proxy --> client: result

== Segunda Llamada (Cache Hit) ==

client -> proxy: findByShipmentId(id)
proxy -> cache: get(id)
cache --> proxy: CachedEntry (valida)

note right: No va a DB
proxy --> client: result (desde cache)

@enduml
```

---

## Diagrama de Invalidez del Cache

```plantuml
@startuml
skinparam componentStyle uml2

' ============================================
' INVALIDACIÓN DEL CACHE
' ============================================

actor "Cliente" as client
participant "Proxy" as proxy
participant "Cache" as cache
database "DB" as db

note over proxy
  save() invalida el cache
end note

== Guardar nueva lectura ==

client -> proxy: save(nuevaLectura)

alt Sin invalidación (sin cache)
    proxy -> db: INSERT INTO readings
    db --> proxy: saved
    proxy --> client: saved
else Con invalidación (proxy con cache)
    proxy -> cache: clear()
    note right: Limpia todo el cache
    cache --> proxy: ok
    proxy -> db: INSERT INTO readings
    db --> proxy: saved
    proxy --> client: saved
end

@enduml
```

---

## Diagrama Comparativo - Sin vs Con Proxy

```plantuml
@startuml
skinparam componentStyle uml2

' ============================================
' COMPARACIÓN - SIN PROXY VS CON PROXY
' ============================================

' --- Sin Proxy ---
container "Sin Proxy" {
    node "Cliente1" as c1
    node "Cliente2" as c2
    node "Cliente3" as c3
    database "Repository (JPA)" as repo1
    
    c1 -> repo1: query
    c2 -> repo1: query
    c3 -> repo1: query
}

note right of c1
  Cada cliente
  consulta DB
end note

' --- Con Proxy ---
container "Con Proxy" {
    node "Cliente1" as c1p
    node "Cliente2" as c2p
    node "Cliente3" as c3p
    collections "CacheProxy" as proxy
    database "Repository (JPA)" as repo2
}

c1p -> proxy: query
proxy -> proxy: Check cache
proxy --> c1p: result (cached)

c2p -> proxy: query
proxy --> c2p: result (cached)

c3p -> proxy: query
proxy --> c3p: result (cached)

repo2 ..> proxy: "never called"

@enduml
```

---

## Ejecutar los Diagramas

Para visualizar los diagramas:
1. Copia el código entre los bloques \`\`\`plantuml
2. Pégalo en [PlantUML Online Editor](https://www.planttext.com)
3. O usa la extensión **PlantUML** en VS Code

---

## Elementos UML Principales

| Elemento | Descripción |
|----------|-------------|
| **SensorReadingRepository** | Interfaz original (Subject) |
| **SensorReadingCacheProxy** | Proxy con cache (Proxy) |
| **ShipmentRepository** | Interfaz original (Subject) |
| **ShipmentCacheProxy** | Proxy con cache (Proxy) |
| **CachedEntry** | Clase interna con TTL |

### Relaciones UML

- `<|..` : Implementación de interfaz
- `o--` : Agregación/composición
- `-->` : Dependencia de creación
- `->` : Flujo de datos