# Patrón Proxy - Implementación en CadenaSuministros

## 1. Introducción al Patrón Proxy

El **Patrón Proxy** es un patrón estructural que proporciona un sustituto o placeholder para otro objeto, controlando el acceso a él. El proxy actúa como intermediario entre el cliente y el objeto real.

### Tipos de Proxy

- **Virtual Proxy**: Crear objetos costosos bajo demanda (lazy loading)
- **Protection Proxy**: Controlar acceso basado en permisos
- **Remote Proxy**: Representar objeto en diferente espacio de direcciones
- **Cache Proxy**: Guardar resultados de operaciones costosas
- **Logging Proxy**: Registrar llamadas y argumentos

---

## 2. Por qué se implementó en este proyecto

### Problema Identificado

En la aplicación CadenaSuministros, los repositorios realizan consultas frecuentes a la base de datos:

| Repositorio | Método | Costo |
|------------|--------|-------|
| `SensorReadingRepository` | `listAll()` | Alto |
| `SensorReadingRepository` | `findByShipmentId()` | Medio |
| `ShipmentRepository` | `findById()` | Medio |
| `ShipmentRepository` | `listAll()` | Alto |

Las lecturas de sensores IoT se realizan frecuentemente, lo que genera muchas consultas a la base de datos.

### Solución

Implementar **Cache Proxy** que:
- Guarda resultados en memoria
- Evita consultas repetitivas a la base de datos
- Tiene tiempo de expiración (TTL)
- Se invalida al guardar nuevos datos

---

## 3. Estructura Implementada

### Ubicación
`src/main/java/com/cadenasuministros/domain/port/out/proxy/`

### Archivos

| Archivo | Descripción |
|---------|-------------|
| `SensorReadingCacheProxy.java` | Proxy con cache para lecturas |
| `ShipmentCacheProxy.java` | Proxy con cache para envíos |

### Interfaz

```java
public class SensorReadingCacheProxy implements SensorReadingRepository {
    private final SensorReadingRepository realRepository;
    private final Map<UUID, CachedEntry<List<...>>> cache;
    private final long cacheTtlMillis;
    
    public SensorReadingCacheProxy(SensorReadingRepository realRepository, long ttl)
}
```

---

## 4. Cómo funciona

### Flujo sin Proxy

```java
// Sin Proxy - cada llamada va a DB
for (int i = 0; i < 10; i++) {
    List<SensorReading> readings = repository.findByShipmentId(shipmentId);
    // 10 consultas a la base de datos
}
```

### Flujo con Proxy

```java
// Con Proxy - solo 1 consulta a DB
SensorReadingCacheProxy proxy = new SensorReadingCacheProxy(realRepository);

for (int i = 0; i < 10; i++) {
    List<SensorReading> readings = proxy.findByShipmentId(shipmentId);
    // Primera: consulta DB
    // siguientes 9: cache en memoria
}
```

### Invalidez del Cache

```java
// Al guardar, se invalida el cache
proxy.save(nuevaLectura);  // Cache se limpia automáticamente

// También se puede limpiar manualmente
proxy.clearCache();
```

### TTL (Time To Live)

```java
// Constructor default: 60 segundos
new SensorReadingCacheProxy(realRepository);

// Constructor custom: tiempo en milisegundos
new SensorReadingCacheProxy(realRepository, 30000L);  // 30 segundos
```

---

## 5. Beneficios

### Beneficios del Patrón

| Beneficio | Descripción |
|----------|-------------|
| **Rendimiento** | Reduce consultas a la base de datos |
| **Tiempo de respuesta** | Datos desde memoria son más rápidos |
| **TTL configurable** | Controla cuánto stay los datos en cache |
| **Invalidación automática** | Al guardar se limpia el cache |
| **Thread-safe** | Usa ConcurrentHashMap |

### Beneficios Técnicos

- Implementa la misma interfaz que el real
- Transparente para el cliente
- No requiere cambios en el código existente

---

## 6. Consideraciones Técnicas

### Implementación

- **ConcurrentHashMap**: Thread-safe para múltiples hilos
- **TTL configurable**: Tiempo de expiración del cache
- **CachedEntry**: Clase interna con timestamp de expiración
- **Invalidación**: Al save() se limpia el cache

### Integración con Arquitectura Hexagonal

El Proxy está en la **capa de dominio** (`domain/port/out/proxy/`):

```
domain/
├── port/
│   ├── out/
│   │   ├── proxy/        ← Proxy pattern
│   │   │   ├── SensorReadingCacheProxy
│   │   │   └── ShipmentCacheProxy
│   │   ├── ShipmentRepository.java
│   │   └── SensorReadingRepository.java
```

### Configuración Spring

Para usar el proxy como bean:

```java
@Bean
public SensorReadingRepository sensorReadingRepository(
        SensorReadingRepository jpaRepository) {
    return new SensorReadingCacheProxy(jpaRepository, 60000L);
}
```

---

## 7. Comparación

### Métricas de Rendimiento

| Operación | Sin Proxy | Con Proxy |
|-----------|----------|----------|
| 10 llamadas `findByShipmentId` | 10 DB | 1 DB |
| Tiempo total | ~500ms | ~50ms |
| Carga DB | Alta | Baja |

### Casos de Uso Ideales

- **Lecturas frecuentes de sensores**: dashboard con gráficos
- **Datos que rareen cambian**: historial de envíos
- **Consultas de lista**: listAll() con pagination

---

## 8. Conclusión

El patrón Proxy con cache proporciona una solución eficiente para reducir la carga en la base de datos y mejorar el rendimiento de la aplicación. Es especialmente útil en escenarios con:

- Alto volumen de lecturas IoT
- Dashboard que refresh frecuentemente
- Consultas repetitivas de los mismos datos

Esta implementación sigue los principios de la arquitectura hexagonal, residiendo en la capa de dominio como un adapter de salida.