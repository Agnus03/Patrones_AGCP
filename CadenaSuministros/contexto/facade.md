# Patrón Facade - Implementación en CadenaSuministros

## 1. Introducción al Patrón Facade

El **Patrón Facade** es un patrón estructural que proporciona una interfaz unificada a un conjunto de interfaces en un subsistema. El Facade define una interfaz de alto nivel que facilita el uso del subsistema completo.

### Propósito
- Simplificar el acceso a sistemas complejos
- Reducir acoplamiento entre clientes y subsistemas
- Proporcionar una interfaz única para operaciones comunes
- Organizar múltiples dependencias en un solo punto de entrada

---

## 2. Por qué se implementó en este proyecto

### Problema Identificado

En la aplicación CadenaSuministros, las operaciones de la cadena de suministro requieren interactuar con múltiples componentes:

| Componente | Responsabilidad |
|------------|------------------|
| `ShipmentController` | Crear y obtener envíos |
| `SensorController` | Registrar lecturas de sensores |
| `DeliveryReportController` | Generar reportes |

Para obtener una vista completa de un envío, el cliente debía:

```java
// Sin Facade - Múltiples llamadas
Shipment shipment = shipmentController.getById(shipmentId);
List<SensorReading> readings = sensorController.listByShipment(shipmentId);
DeliveryReport report = reportController.generate(shipmentId);

// Además, el cliente debe:
// - Conocer los diferentes controllers
// - Manejar múltiples tipos de respuesta
// - Coordinar llamadas independientes
```

### Solución

Crear un **SupplyChainFacade** que unifique todas estas operaciones en una sola interfaz:

```java
// Con Facade - 1 llamada
Dashboard dashboard = facade.getShipmentDashboard(shipmentId);
```

---

## 3. Estructura Implementada

### Ubicación
`src/main/java/com/cadenasuministros/application/facade/`

### Archivos

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `SupplyChainFacade.java` | Interface | Contrato del Facade |
| `SupplyChainFacadeImpl.java` | Class | Implementación |
| `ShipmentInfo.java` | Record | DTO información de envío |
| `ShipmentStatus.java` | Record | DTO estado de tracking |
| `SensorReadingResult.java` | Record | DTO lectura de sensor |
| `Dashboard.java` | Record | DTO panel consolidado |
| `DeliveryReportInfo.java` | Record | DTO reporte de entrega |

### Interfaz del Facade

```java
public interface SupplyChainFacade {
    ShipmentInfo createShipment(UUID productId, String productName, Integer quantity);
    ShipmentInfo getShipmentInfo(UUID shipmentId);
    ShipmentStatus trackShipment(UUID shipmentId);
    SensorReadingResult registerSensorReading(UUID shipmentId, Double temperatureC, Double humidityPct, Double latitude, Double longitude);
    Dashboard getShipmentDashboard(UUID shipmentId);
    DeliveryReportInfo generateDeliveryReport(UUID shipmentId);
}
```

### Diagrama de Arquitectura

```
                    ┌─────────────────────┐
                    │   Cliente (REST)     │
                    └─────────┬───────────┘
                              │
                              ▼
                    ┌─────────────────────┐
                    │ SupplyChainFacade   │ ←── Simplifica interfaz
                    │  (interface)     │
                    └─────────┬───────────┘
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
    ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
    │   Shipment     │ │   Sensor       │ │    Report       │
    │   UseCase     │ │   UseCase     │ │    UseCase      │
    └────────┬──────┘ └────────┬──────┘ └────────┬──────┘
             │                 │                 │
             ▼                 ▼                 ▼
    ┌─────────────────┐ ┌─────────────────┐ ┌─────────────────┐
    │ Repositories    │ │ Repositories    │ │  Domain Models  │
    │ (JPA)          │ │ (JPA)          │ │                 │
    └─────────────────┘ └─────────────────┘ └─────────────────┘
```

---

## 4. Cómo funciona

### Ejemplo de Uso

#### Crear un envío

```java
@Autowired
private SupplyChainFacade facade;

ShipmentInfo shipment = facade.createShipment(
    UUID.randomUUID(),
    "Leche Entera 1L",
    100
);
System.out.println("Shipment ID: " + shipment.id());
System.out.println("Status: " + shipment.status());
```

#### Registrar lectura de sensor

```java
SensorReadingResult reading = facade.registerSensorReading(
    shipmentId,
    25.0,      // temperature
    60.0,      // humidity
    40.412,    // latitude
    -3.7       // longitude
);
System.out.println("Alert: " + reading.alert());
```

#### Obtener dashboard completo

```java
Dashboard dashboard = facade.getShipmentDashboard(shipmentId);

// Toda la información consolidada:
dashboard.shipmentId();           // UUID del shipment
dashboard.shipmentStatus();     // "PENDING", "IN_TRANSIT", etc.
dashboard.currentLocation();    // Ubicación actual
dashboard.summary();          // Resumen del shipment
dashboard.sensorStats();      // Estadísticas de sensores
dashboard.recentReadings();   // Últimas 10 lecturas
dashboard.activeAlerts();     // Alertas activas
```

#### Comparación: Sin vs Con Facade

| Operación | Sin Facade | Con Facade |
|-----------|------------|------------|
| Obtener info de shipment | 1 llamada | 1 llamada |
| Registrar sensor | 1 llamada | 1 llamada |
| **Obtener dashboard** | **3+ llamadas** | **1 llamada** |
| **Generar reporte** | **1 controller** | **1 facade** |

---

## 5. Beneficios

### Beneficios del Patrón

| Beneficio | Descripción |
|----------|-------------|
| **Simplificación de API** | El cliente solo conoce una interfaz, no múltiples controllers |
| **Bajo acoplamiento** | Cambios en subsistemas no afectan al cliente |
| **Consistencia de datos** |El Facade coordina y normaliza respuestas |
| **Operaciones centralizadas** | Toda lógica de coordinación en un solo lugar |
| **Facilidad de mantenimiento** | Cambios localizados en el Facade |
| **Testing simplificado** | Se puede mockear el Facade fácilmente |

### Beneficios de la Implementación

1. **Inmutabilidad**: DTOs usando Java records
2. **Tipado seguro**: Interfaz sellada que define contrato claro
3. **Organización**: DTOs anidados para estructuras complejas (Dashboard)
4. **Extensibilidad**: Fácil agregar nuevas operaciones

### Beneficios Técnicos

- **Facade como bean**: Puede ser inyectado con Spring
- **Compatible con arquitectura hexagonal**: Vive en la capa de aplicación
- **Separación de responsabilidades**: Cada método del Facade delega a Use Cases correspondientes

---

## 6. Consideraciones Técnicas

### Java Features Utilizadas

- **Records**: Para DTOs concisos e immutables
- **Sealed Interface**: Definición de contrato
- **Stream API**: Procesamiento de colecciones
- **Lambda Expressions**: Filtrado y mapeo

### Integración con la Arquitectura Hexagonal

El patrón Facade está ubicado en la **capa de aplicación** (`application/facade/`):

```
application/
├── facade/               ← Facade pattern
│   ├── SupplyChainFacade.java
│   ├── SupplyChainFacadeImpl.java
│   └── DTOs...
├── factory/              ← Abstract Factory pattern
├── usecase/             ← Use Cases
└── reporting/           ← Bridge + Decorator patterns
```

### Extendibilidad

Para agregar nuevas operaciones:
1. Agregar método a `SupplyChainFacade`
2. Implementar en `SupplyChainFacadeImpl`
3. El cliente automáticamente tiene acceso a la nueva operación

---

## 7. Conclusión

El patrón Facade proporciona una solución elegante para simplificar el acceso a las operaciones complejas de la cadena de suministro. Permite:

- Unificar múltiples llamadas en una sola operación
- Reducir la complejidad percibida por el cliente
- Centralizar la lógica de coordinación
- Mantener bajo acoplamiento entre clientes y subsistemas

Esta implementación sigue los principios de la arquitectura hexagonal, residiendo en la capa de aplicación y utilizando los Use Cases existentes como bloque de construcción.