# Contexto del Proyecto CadenaSuministros

## Descripción General

**CadenaSuministros** es una aplicación Java/Spring Boot que implementa un sistema de gestión de cadena de suministro con monitoreo IoT. El proyecto utiliza una arquitectura hexagonal (Ports & Adapters) y aplica múltiples patrones de diseño para demostrar buenas prácticas de desarrollo de software.

## Problema del Negocio

Esta aplicación resuelve la necesidad de:

- **Seguimiento de envíos** en tiempo real a lo largo de la cadena de suministro
- **Monitoreo de condiciones ambientales** (temperatura, humedad, ubicación GPS) mediante sensores IoT
- **Registro de lecturas de sensores** para garantizar la calidad de productos perecederos
- **Generación de reportes de entrega** con estadísticas ambientales y alertas

## Tecnologías Utilizadas

| Tecnología | Versión | Propósito |
|------------|---------|----------|
| Java | 17 | Lenguaje de programación |
| Spring Boot | 4.0.3 | Framework principal |
| PostgreSQL | - | Base de datos relacional |
| Maven | - | Gestión de dependencias |
| Spring Data JPA | - | Acceso a datos |
| Flyway | - | Migraciones de base de datos |

## Arquitectura del Proyecto

```
src/main/java/com/cadenasuministros/
├── CadenaSuministrosApplication.java    # Punto de entrada
├── adapters/                           # Capa de adaptadores
│   ├── inbound/                       # Entrada (REST, IoT)
│   │   ├── rest/                     # Controladores REST
│   │   └── iot/                      # Listeners MQTT
│   └── outbound/                     # Salida (persistencia)
│       └── persistence/
│           ├── jpa/                  # Adaptadores JPA
│           └── ml/                  # Cliente ML
├── application/                       # Capa de aplicación
│   ├── factory/                      # Abstract Factory
│   ├── usecase/                      # Casos de uso
│   └── reporting/                   # Reportes (Bridge + Decorator)
├── config/                           # Configuración
└── domain/                           # Capa de dominio
    ├── model/                        # Entidades del dominio
    ├── port/                         # Puertos (interfaces)
    │   ├── in/                      # Puerto entrada (use cases)
    │   └── out/                     # Puerto salida (repositorios)
    ├── prototype/                   # Prototype pattern
    └── service/                     # Servicios de dominio
```

## Patrones de Diseño Implementados

### 1. Hexagonal Architecture (Ports & Adapters)

**Propósito:** Separar la lógica de dominio del exterior

- **Domain Layer:** `domain/model/`, `domain/port/`, `domain/service/`
- **Application Layer:** `application/usecase/`
- **Adapters Layer:** `adapters/inbound/`, `adapters/outbound/`

### 2. Abstract Factory

**Ubicación:** `application/factory/`

**Propósito:** Crear familias de objetos relacionados (use cases)

**Archivos:**
- `SupplyChainUseCaseAbstractFactory.java` - Interfaz abstracta
- `DefaultSupplyChainUseCaseFactory.java` - Implementación concretos
- `TrackShipmentUseCaseFactory.java` y `RegisterSensorReadingUseCaseFactory.java`

### 3. Builder

**Ubicación:** `domain/model/DeliveryReport.java`

**Propósito:** Construir objetos complejos paso a paso

La clase `DeliveryReport` contiene un `Builder` interno que permite crear instancias con diferentes configuraciones.

### 4. Decorator

**Ubicación:** `application/reporting/decorator/`

**Propósito:** Añadir comportamiento dinámicamente sin modificar clases

**Componentes:**
- `DeliveryReportGeneratorDecorator.java` - Clase base abstracta
- `LoggingReportDecorator.java` - Añade logging
- `ValidationReportDecorator.java` - Añade validación

### 5. Bridge

**Ubicación:** `application/reporting/`

**Propósito:** Separar abstracción de implementación

- **Abstraction:** `DeliveryReportGenerator`, `DetailedDeliveryReportGenerator`
- **Implementor:** `ReportOutput`, `JpaReportOutput`, `ConsoleReportOutput`

### 6. Prototype

**Ubicación:** `domain/model/Shipment.java`, `domain/prototype/`

**Propósito:** Clonar objetos con variaciones

`Shipment` implementa la interfaz `Prototype<Shipment>` y métodos `withStatus()` y `withLocation()` para crear copias modificadas.

## Modelos del Dominio

### Shipment (Envío)

```java
record Shipment(
    UUID id,
    UUID productId,
    String status,
    String currentLocation,
    Instant updatedAt
)
```

Estados típicos: `PENDING`, `IN_TRANSIT`, `DELIVERED`, `DELAYED`

### SensorReading (Lectura de Sensor)

```java
record SensorReading(
    UUID id,
    UUID shipmentId,
    Instant timestamp,
    Double temperatureC,
    Double humidityPct,
    Double latitude,
    Double longitude
)
```

### DeliveryReport (Reporte de Entrega)

Reporte detallado con estadísticas ambientales, alertas y observaciones.

### Product (Producto)

Entidad simple que representa un producto en la cadena de suministro.

## Controladores REST

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/sensors/readings` | POST | Registrar lectura de sensor |
| `/api/sensors` | GET | Listar todas las lecturas |
| `/api/shipments` | POST | Crear seguimiento de envío |
| `/api/shipments/{id}` | GET | Obtener estado de envío |
| `/api/delivery-reports` | GET | Generar reporte de entrega |

## Configuración

### application.yml

Contiene configuración de base de datos PostgreSQL y otros parámetros de la aplicación.

### FactoryConfig.java

Configura todos los beans usando Abstract Factory, Bridge y Decorator para demostrar los patrones.

## Dependencias Maven

- `spring-boot-starter-data-jpa` - Persistence
- `spring-boot-starter-flyway` - Migraciones
- `spring-boot-starter-validation` - Validación
- `spring-boot-starter-webmvc` - REST API
- `postgresql` - Driver PostgreSQL

## Casos de Uso (Use Cases)

1. **RegisterSensorReadingUseCase** - Registrar lecturas de sensores IoT
2. **TrackShipmentUseCase** - Seguimiento de envíos
3. **GenerateDeliveryReportUseCase** - Generar reportes de entrega

## Beneficios de la Arquitectura

1. **Testabilidad** - La lógica de dominio es independiente de frameworks
2. **Mantenibilidad** - Cambios aislados en cada capa
3. **Flexibilidad** - Fácil替换 adaptadores (ej: cambiar JPA por MongoDB)
4. **Reutilización** - Los use cases pueden usarse desde diferentes puertos de entrada
5. **Claridad** - Estructura clara que refleja el dominio del negocio

## Próximos Pasos Sugeridos

- Agregar más entidades del dominio (Warehouse, Route, etc.)
- Implementar autenticación y autorización
- Agregar más adaptadores de entrada (GraphQL, Messaging)
- Mejorar la generación de reportes
- Agregar tests unitarios y de integración