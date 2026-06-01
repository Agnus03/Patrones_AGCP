# Patrón Chain of Responsibility — Por qué NO se implementó en CadenaSuministros

## 1. Introducción al Patrón Chain of Responsibility

El **Chain of Responsibility** es un patrón de comportamiento que desacopla el emisor de una solicitud de sus receptores, dando a múltiples objetos la oportunidad de procesarla. Los receptores se encadenan y la solicitud viaja por la cadena hasta que uno (o varios) la manejan.

### Propósito
- Evitar el acoplamiento entre el emisor de una solicitud y su receptor
- Permitir que más de un objeto pueda manejar una solicitud
- Componer dinámicamente una cadena de handlers en tiempo de ejecución
- Aplicar el principio de responsabilidad única: cada handler hace una cosa

### Cuándo se usa
- La solicitud debe ser procesada por uno de varios handlers, sin saber cuál de antemano
- Los handlers pueden agregarse o quitarse dinámicamente
- Se quiere emitir una solicitud sin especificar explícitamente el receptor
- Ejemplos clásicos: middleware HTTP, filtros de logging, flujos de aprobación

---

## 2. Análisis de Candidatos en CadenaSuministros

Se evaluaron 5 candidatos potenciales para implementar Chain of Responsibility. A continuación, el análisis detallado de cada uno.

### Candidato 1: Quality Checkpoint Pipeline

**Propuesta:** Cadena de validación antes de persistir un checkpoint de calidad.

```
POST /api/quality
  → TemperatureValidator   (auto settea passed=false si temp ∉ [2,30])
  → HumidityValidator      (auto settea passed=false si humidity ∉ [30,80])
  → ShipmentExistsValidator (verifica que el shipment exista)
  → PersistenceHandler     (guarda el checkpoint)
```

**Por qué se descartó:**

| Criterio | Evaluación |
|----------|-----------|
| **¿Múltiples handlers pueden procesar?** | Sí, todos los validadores deben ejecutarse. Es una cadena colaborativa. |
| **¿La cadena varía dinámicamente?** | No. Los validadores son siempre los mismos para todos los checkpoints. |
| **¿El handler se conoce de antemano?** | Sí. Todos los checkpoints pasan por exactamente los mismos pasos. |
| **¿Hay composición dinámica en runtime?** | No. No hay escenario donde un checkpoint salte ciertos validadores según el contexto. |

**Decisión:** ❌ No se implementa. Los validadores son estáticos y conocidos. Una simple secuencia de métodos (o un `Validator` con una lista fija) es más directa y legible que una cadena de responsabilidad completa. CoR añadiría 3+ interfaces (`Handler`, `setNext()`, `handle()`) sin aportar variabilidad dinámica.

**Alternativa más simple:** Si en el futuro se necesitara validación extensible, un `List<CheckpointValidator>` inyectado vía Spring con un bucle `for` lograría el mismo resultado que CoR con menos indirección:

```java
@Component
public class CheckpointValidationPipeline {
    private final List<CheckpointValidator> validators;

    public QualityCheckpoint validate(QualityCheckpoint checkpoint) {
        for (CheckpointValidator v : validators) {
            checkpoint = v.validate(checkpoint);  // cada validador transforma o rechaza
        }
        return checkpoint;
    }
}
```

Esto es más simple que CoR porque:
- No requiere interfaz con `setNext()` — Spring inyecta el orden
- No hay navegación de cadena — el bucle es explícito
- No hay riesgo de que un handler "corte" la cadena prematuramente

---

### Candidato 2: Alert Evaluation Chain

**Propuesta:** En lugar de un `AlertEvaluationListener` monolítico, una cadena de handlers por tipo de alerta.

```
SensorReadingRegisteredEvent
  → TemperatureAlertHandler  (evalúa temp contra [2, 30])
  → HumidityAlertHandler     (evalúa hum contra [30, 80])
  → (futuro) GPSSpeedAlertHandler
  → (futuro) VibrationAlertHandler
```

**Por qué se descartó:**

| Criterio | Evaluación |
|----------|-----------|
| **¿Múltiples handlers pueden procesar?** | Sí, pueden dispararse múltiples alertas simultáneas. |
| **¿La cadena varía dinámicamente?** | Potencialmente, si diferentes tipos de sensor requieren diferentes evaluaciones. |
| **¿Spring ya provee desacoplamiento?** | Sí. `@EventListener` ya permite que cada tipo de alerta tenga su propio listener sin CoR. |

**Decisión:** ❌ No se implementa. Spring `ApplicationEventPublisher` + `@EventListener` ya implementa un bus de eventos que desacopla emisores de receptores. Agregar CoR entre el evento y los listeners sería una capa de indirección innecesaria.

Cada nueva alerta se agrega simplemente creando un nuevo `@EventListener`:

```java
@Component
public class GpsSpeedAlertHandler {
    @EventListener
    public void onSensorReading(SensorReadingRegisteredEvent event) {
        if (event.getReading().speed() > MAX_SPEED) {
            eventPublisher.publishEvent(new AlertTriggeredEvent(...));
        }
    }
}
```

Esto es más simple que CoR porque:
- No requiere modificar ningún handler existente (OCP puro)
- No hay interfaz de handler que implementar — solo un método anotado
- Spring maneja el enrutamiento del evento a todos los listeners interesados

---

### Candidato 3: Command Middleware Pipeline

**Propuesta:** Agregar una cadena de middleware antes de ejecutar comandos (validación, logging, autorización).

```
UpdateStatusCommand.execute()
  → pre-validation chain:
      → ShipmentExistsValidator
      → StatusTransitionValidator
      → LoggingHandler
      → AuthorizationHandler
  → doExecute() (mutación real)
  → post-execution chain:
      → EventPublisher
      → AuditLogger
```

**Por qué se descartó:**

| Criterio | Evaluación |
|----------|-----------|
| **¿El pipeline varía dinámicamente?** | No. Todos los comandos de actualización ejecutan el mismo flujo. |
| **¿Template Method ya cubre esto?** | Sí. `AbstractShipmentCommand.execute()` ya define el pipeline fijo: fetch → isNoOp → doExecute → save → recordEvent → publishEvent. |
| **¿CoR mejoraría la extensibilidad?** | No. Agregar un paso al pipeline requiere modificar el template, pero esto es poco frecuente y preferimos que sea explícito. |

**Decisión:** ❌ No se implementa. El **Template Method** ya implementa el pipeline de ejecución de comandos. Reemplazarlo por CoR:
1. Eliminaría la garantía de orden que TM provee (la cadena podría reordenarse accidentalmente)
2. Haría el flujo menos legible (habría que seguir la cadena para entender el orden)
3. No aportaría variabilidad dinámica (la cadena sería fija en la práctica)

CoR es óptimo cuando el pipeline cambia en producción (como middleware HTTP). En comandos, el pipeline es fijo por diseño.

---

### Candidato 4: Cost Strategy Selection

**Propuesta:** Refactorizar `CostCalculator.selectStrategy()` como una cadena explícita de handlers.

```java
// Actual: bucle for que busca el primer strategy que soporta el producto
private CostCalculationStrategy selectStrategy(String productName) {
    for (CostCalculationStrategy s : strategies) {
        if (s.supports(productName)) return s;
    }
    return standardStrategy;
}
```

**Por qué se descartó:**

| Criterio | Evaluación |
|----------|-----------|
| **¿Es conceptualmente una cadena?** | Sí. Cada strategy decide si maneja o pasa a la siguiente. |
| **¿El patrón correcto es Strategy o CoR?** | **Strategy**. No hay "manejar o pasar" — hay "elegir el algoritmo correcto". |
| **¿CoR aporta algo nuevo?** | No. Strategy captura mejor la intención: algoritmos intercambiables. |

**Decisión:** ❌ No se implementa. El bucle `for` con `supports()` ya implementa el comportamiento de "primer handler que soporta". Refactorizarlo a CoR explícito (con `Handler` interface, `setNext()`, `handle()`) agregaría ceremonia sin cambiar la semántica ni aportar flexibilidad nueva.

La selección actual es correcta y más legible que una cadena de responsabilidad explícita:

```java
// Actual: claro, directo, sin indirección
for (CostCalculationStrategy s : strategies) {
    if (s.supports(productName)) return s;
}
```

---

### Candidato 5: Frontend Form Validation

**Propuesta:** Cadena de validación centralizada para formularios.

```typescript
// En lugar de validaciones inline en cada form:
const validationChain = new ValidationChain()
    .add(new RequiredFieldValidator())
    .add(new NumberRangeValidator('temperatureC', 2, 30))
    .add(new NumberRangeValidator('humidityPct', 30, 80));

validationChain.validate(formData);
```

**Por qué se descartó:**

| Criterio | Evaluación |
|----------|-----------|
| **¿Complejidad de las validaciones actuales?** | Muy baja. Son checks simples: `!sku.trim()`, `!shipmentId`, `required` HTML. |
| **¿Se beneficiaría de composición dinámica?** | No. Cada formulario tiene validaciones fijas y diferentes. |
| **¿CoR mejoraría el mantenimiento?** | No. Las validaciones son tan simples que una cadena sería más código del que reemplaza. |

**Decisión:** ❌ No se implementa. Las validaciones actuales son simples y directas. Agregar CoR para formularios con 2-3 campos requeriría:

1. Crear interfaz `Validator<T>` con `validate()` y `setNext()`
2. Crear clases concretas para cada tipo de validación
3. Instanciar y encadenar en cada formulario
4. Mantener la cadena actualizada

Para formularios con validaciones de 1-3 líneas, esto es sobreingeniería pura. La validación inline es más legible y mantenible:

```typescript
// Directo, sin indirección, más fácil de leer y depurar
if (!shipmentId || !location) return;
if (temperatureC && (temperatureC < 2 || temperatureC > 30)) {
    setError('Temperatura fuera de rango');
    return;
}
```

---

## 3. Resumen de Evaluación

| # | Candidato | Patrón actual | ¿Aplica CoR? | Decisión |
|---|-----------|--------------|--------------|----------|
| 1 | Quality Checkpoint Pipeline | Inline en controller | Conceptual | ❌ No. Validadores estáticos. Un `List<Validator>` con bucle es más simple. |
| 2 | Alert Evaluation Chain | Observer (`@EventListener`) | Potencial | ❌ No. Spring Events ya desacoplan emisores y receptores mejor que CoR. |
| 3 | Command Middleware Pipeline | Template Method (`AbstractShipmentCommand`) | Conceptual | ❌ No. Template Method garantiza orden explícito. CoR haría el flujo menos predecible. |
| 4 | Cost Strategy Selection | Strategy (bucle `for` + `supports()`) | Sí (ya es una cadena implícita) | ❌ No. Strategy captura mejor la intención. El bucle for es más claro que CoR explícito. |
| 5 | Frontend Form Validation | Inline | No | ❌ No. Validaciones simples. CoR sería más código del que reemplaza. |

---

## 4. Conclusión

**Chain of Responsibility no se implementa en CadenaSuministros** porque el proyecto no presenta el problema que el patrón resuelve.

### Lo que CoR requiere vs. lo que el proyecto tiene

| CoR requiere... | En CadenaSuministros... |
|----------------|------------------------|
| Múltiples handlers candidatos para una misma solicitud | Cada solicitud tiene un handler fijo y conocido |
| La cadena debe ser configurable dinámicamente | Los pipelines son estáticos y definidos en compilación |
| El emisor no debe conocer al receptor | El emisor conoce exactamente qué servicio llamar |
| Los handlers pueden agregarse sin modificar código existente | Spring ya lo permite con `@EventListener` e inyección de `List<>` |

### Lo que ya cubren otros patrones

| Necesidad | Patrón existente | Cómo lo cubre |
|-----------|-----------------|---------------|
| Pipeline fijo de ejecución | **Template Method** | `AbstractShipmentCommand.execute()` con hooks |
| Validación extensible | Inyección de `List<Validator>` | Spring `@Component` + `List` injection |
| Desacoplamiento emisor-receptor | **Observer** (Spring Events) | `ApplicationEventPublisher` + `@EventListener` |
| Algoritmos intercambiables | **Strategy** | `CostCalculationStrategy` con `supports()` |
| Validación simple en formularios | Inline | `if (!x) return` — más legible que una cadena |

### Cuándo tendría sentido implementar CoR

Si en el futuro el proyecto incorporara:

1. **Un sistema de reglas de negocio configurables** — donde diferentes clientes o tipos de envío requieran distintas validaciones en tiempo de ejecución
2. **Un middleware HTTP personalizado** — donde las peticiones REST pasen por filtros configurables (autenticación, rate limiting, transformación)
3. **Un flujo de aprobaciones** — donde una solicitud de cambio de estado deba pasar por diferentes niveles de aprobación según el monto/destino

En esos escenarios, Chain of Responsibility sería el patrón adecuado. En el estado actual del proyecto, los patrones existentes (Template Method, Observer, Strategy, inyección de dependencias) cubren todos los casos de manera más simple y directa.
