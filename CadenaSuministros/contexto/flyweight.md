# Patrón Flyweight - Análisis de Viabilidad

## 1. Introducción al Patrón Flyweight

El **Patrón Flyweight** es un patrón estructural que usa el compartir objeto para soportar grandes cantidades de objetos de grano fino de manera eficiente.

### Conceptos Clave

- **Estado intrínseco**: Compartido, almacenado en el flyweight (inmutable)
- **Estado extrínseco**: Variable, calculado o almacenado por el cliente
- **FlyweightFactory**: Crea y gestiona los objetos flyweight

---

## 2. Por qué NO se implementará en este proyecto

### 2.1 Análisis del Dominio

Los modelos del proyecto tienen datos únicos que **no se repiten**:

| Modelo | Estado Intrínseco | Estado Extrínseco | ¿Flyweight viable? |
|--------|------------------|-------------------|-------------------|
| `SensorReading` | UUID, shipmentId | Temp, humidity, GPS, timestamp | ❌ No - cada lectura es única |
| `Shipment` | UUID, productId | Status, location, timestamp | ❌ No - cada envío es único |
| `DeliveryReport` | UUID | Reporte completo | ❌ No - cada reporte es único |

**Problema**: Cada lectura de sensor contiene valores únicos (temperatura, humedad, GPS) que no pueden compartirse.

### 2.2 Ejemplo del Problema

```java
// Flyweight NO funciona así:
new SensorReading(id1, shipmentId, temp=25.0, humidity=60.0);
new SensorReading(id2, shipmentId, temp=26.0, humidity=61.0);
new SensorReading(id3, shipmentId, temp=27.0, humidity=62.0);

// Cada lectura tiene valores DIFERENTES - no hay nada que compartir
```

---

## 3. Cuándo SÍ sería viable

El patrón sería útil solo si el proyecto evoluciona hacia escenarios con **objetos compartibles**:

### 3.1 Catálogo de Tipos de Sensores

```
Flyweight: SensorType
    ├── "TEMP-DHT22" → nombre: "Sensor Temperatura DHT22"
    ├── "HUM-SI7021" → nombre: "Sensor Humedad SI7021"  
    └── "GPS-NEO6M"  → nombre: "GPS NEO-6M"
```

Esto implicaría agregar una nueva entidad `SensorType`.

### 3.2 Estados Predefinidos

```
enum ShipmentStatus {
    PENDING, IN_TRANSIT, DELIVERED, DELAYED
}
// Pero esto ya existe como Enum, no necesita Flyweight
```

### 3.3 Ubicaciones Comunes

```
Flyweight: Location
    ├── "WAREHOUSE-A" → dirección, ciudad
    ├── "WAREHOUSE-B" → dirección, ciudad
    └── "DESTINATION-1" → dirección, ciudad
```

---

## 4. Conclusión

### Razones para NO implementar

| Razón | Descripción |
|-------|-------------|
| **Datos únicos** | Cada lectura de sensor tiene valores diferentes |
| **Sin objetos repetidos** | No hay elementos que pueda compartir entre instancias |
| **Agregaría complejidad** | Sin beneficio real - el Proxy ya optimiza consultas |
| **BDD innecesaria** | El problema de rendimiento ya está resuelto con el Proxy |

### Comparación con el Patrón Proxy

| Patrón | Problema que resuelve | Implementado |
|--------|---------------------|--------------|
| **Proxy** | Consultas repetitivas a DB | ✅ Sí |
| **Flyweight** |-many objetos idénticos | ❌ No necesario |

---

## 5. Recomendación

El proyecto **no necesita Flyweight** actualmente porque:

1. ✅ El **Proxy** ya implementado resuelve el problema de rendimiento de consultas
2. ✅ Los modelos contienen datos únicos que no se repetition
3. ✅ No hay un catálogo de elementos compartibles

Si en el futuro se requiere gestionar un catálogo de tipos de sensores o ubicaciones predefinidas, entonces flyweight sería una opción a considerar.