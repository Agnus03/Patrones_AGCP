# Patrón Memento — Por qué NO se implementó en CadenaSuministros

## 1. Introducción al Patrón Memento

El **Patrón Memento** es un patrón de comportamiento que captura y externaliza el estado interno de un objeto sin violar su encapsulamiento, permitiendo restaurarlo a ese estado posteriormente.

### Propósito
- Capturar el estado interno de un objeto en un momento dado
- Almacenar el snapshot sin exponer la estructura interna del objeto
- Permitir restauración futura a estados anteriores
- Separar la responsabilidad de almacenar estado (Caretaker) del objeto cuyo estado se guarda (Originator)

### Participantes
- **Originator**: El objeto cuyo estado se quiere capturar. Crea mementos con su estado actual y puede restaurarse desde ellos.
- **Memento**: Objeto opaco que almacena el estado interno del Originator. Solo el Originator puede leerlo (wide interface); el Caretaker no puede modificarlo (narrow interface).
- **Caretaker**: Responsable de almacenar y gestionar los mementos. Nunca examina ni modifica el contenido del memento.

### Cuándo se usa
- Se necesita implementar undo/redo con snapshots de estado completo
- Se requiere checkpointing y rollback en operaciones largas
- El estado interno del objeto debe preservarse sin exponer su implementación
- Ejemplos clásicos: editores de texto con deshacer/ rehacer, juegos con guardado de partida, transacciones con rollback

### Cuándo NO se usa
- El estado del objeto es grande (problemas de memoria/rendimiento)
- Los cambios de estado son poco frecuentes
- El encapsulamiento no es una preocupación (el estado ya es accesible)
- Existen alternativas más simples para undo/redo

---

## 2. Análisis de Candidatos en CadenaSuministros

Se evaluaron 4 candidatos potenciales para implementar Memento. A continuación, el análisis detallado de cada uno.

### Candidato 1: Formalizar `previousState` como ShipmentMemento

**Propuesta:** En lugar de guardar `previousState: Shipment` directamente en los comandos, crear una clase `ShipmentMemento` y métodos `createMemento()` / `restore()` en `Shipment`.

```
// Actual (sin Memento)
abstract class AbstractShipmentCommand {
    protected Shipment previousState;   // ← estado capturado como referencia directa

    public Shipment execute() {
        this.previousState = current;          // captura
        Shipment updated = doExecute(current);
        Shipment saved = save(updated);
        return saved;
    }

    public Optional<Shipment> undo() {
        return Optional.of(save(previousState));  // restaura
    }
}

// Con Memento (propuesta)
class ShipmentMemento {
    // estado opaco — solo Shipment puede leerlo
    private final UUID id;
    private final String status;
    private final String currentLocation;
    // ...
}

class Shipment {
    public ShipmentMemento createMemento() {
        return new ShipmentMemento(this);   // captura en objeto aparte
    }

    public Shipment restore(ShipmentMemento m) {
        return new Shipment(m.getId(), m.getStatus(), ...);  // restaura desde memento
    }
}

abstract class AbstractShipmentCommand {
    protected ShipmentMemento memento;

    public Shipment execute() {
        this.memento = current.createMemento();   // captura via memento
        Shipment updated = doExecute(current);
        Shipment saved = save(updated);
        return saved;
    }

    public Optional<Shipment> undo() {
        return Optional.of(save(current.restore(memento)));  // restaura via memento
    }
}
```

**Por qué se descartó:**

| Criterio | Evaluación |
|----------|-----------|
| **¿Shipment es mutable?** | No. `Shipment` es un `record` Java (inmutable). Cada "mutación" (`withStatus()`, `withLocation()`) devuelve una nueva instancia. |
| **¿Hay encapsulamiento que proteger?** | No. `record Shipment(UUID id, String status, ...)` expone todos sus campos públicamente via accessors. No hay estado interno oculto. |
| **¿Memento aporta algo nuevo?** | No. Una referencia al record anterior ES idéntica a un memento: captura completa, inmutable, restaurable. |
| **¿La ceremonia se justifica?** | No. Crear `ShipmentMemento`, `createMemento()`, `restore()` triplica las líneas de código para exactamente el mismo comportamiento. |

**Decisión:** ❌ No se implementa. La referencia `previousState: Shipment` ya es un memento sin el nombre formal. Los records inmutables de Java hacen que Memento sea redundante:

```java
// current es un record inmutable
Shipment current = repo.findShipmentById(id);

// "Capturar estado" = guardar referencia
this.previousState = current;

// "Restaurar estado" = usar la referencia
repo.save(previousState);
```

No hay encapsulamiento que violar (el record es público). No hay estado interno mutable que proteger (el record es inmutable). La referencia al record anterior ya es un snapshot completo y seguro. Agregar `ShipmentMemento` como wrapper sería pura ceremonia.

---

### Candidato 2: Rollback atómico en CompositeShipmentCommand

**Propuesta:** Antes de ejecutar un macro-comando (`CompositeShipmentCommand`), tomar un memento del Shipment. Si algún subcomando falla, restaurar el estado original desde el memento.

```java
// Actual — no hay rollback en Composite
public Shipment execute() {
    Shipment result = null;
    for (ShipmentCommand cmd : commands) {
        result = cmd.execute();   // si cmd #2 falla, cmd #1 ya persisitió en DB
    }
    return result;
}

// Con Memento (propuesta)
public Shipment execute() {
    ShipmentMemento snapshot = fetchCurrentShipment().createMemento();
    try {
        Shipment result = null;
        for (ShipmentCommand cmd : commands) {
            result = cmd.execute();
        }
        return result;
    } catch (Exception e) {
        restoreFromMemento(snapshot);   // ¿esto deshace cmd #1 en DB?
        throw e;
    }
}
```

**Por qué se descartó:**

| Criterio | Evaluación |
|----------|-----------|
| **¿Los comandos persisten inmediatamente?** | Sí. Cada `cmd.execute()` llama a `repo.save()`, que ejecuta un `UPDATE` en la base de datos. |
| **¿Un memento en memoria puede deshacer un UPDATE commiteado?** | No. Una vez que `repo.save()` retorna, el cambio está en la base de datos. Un memento en Java no puede revertir una transacción de base de datos ya commiteada. |
| **¿Cuál es la solución correcta para rollback?** | `@Transactional`. Si el Composite se ejecuta dentro de una transacción Spring, cualquier fallo revierte automáticamente TODAS las operaciones de base de datos. |

**Decisión:** ❌ No se implementa. Memento opera en memoria y no puede deshacer operaciones de base de datos ya persistidas. El rollback de operaciones de base de datos se resuelve con transacciones Spring (`@Transactional`), no con patrones de diseño en memoria.

```java
// Solución correcta para rollback: transacciones
@Transactional
public Shipment executeMacro(UUID shipmentId, String newStatus, String newLocation) {
    Shipment s = repo.findById(shipmentId);
    s = repo.save(s.withStatus(newStatus));    // si la línea siguiente falla,
    s = repo.save(s.withLocation(newLocation)); // Spring revierte el primer save
    return s;
}
```

Incluso si los comandos no usaran base de datos, el Memento en `CompositeShipmentCommand` tendría que capturar el estado ANTES de cada subcomando individual, no solo del conjunto completo, porque cada comando transforma el estado secuencialmente. Esto multiplica la complejidad.

---

### Candidato 3: ShipmentEvent como Memento persistente

**Propuesta:** Usar la tabla `shipment_events` (que ya almacena `fromStatus`/`toStatus` y `fromLocation`/`toLocation`) como un repositorio de mementos serializados para undo persistente — permitiendo deshacer operaciones incluso después de un reinicio del servidor.

```java
// Actual: ShipmentEvent almacena solo el cambio, no el estado completo
public record ShipmentEvent(
    UUID id, UUID shipmentId,
    String fromStatus, String toStatus,
    String fromLocation, String toLocation,
    Instant timestamp
) {}

// Con Memento (propuesta): almacenar snapshot completo
public record ShipmentMemento(
    UUID id, UUID shipmentId,
    String status, String currentLocation,
    // ... todos los campos de Shipment
) {}

// Cada comando guardaría el memento ANTES de mutar
// ShipmentEventRepository almacenaría ShipmentMemento como JSON
```

**Por qué se descartó:**

| Criterio | Evaluación |
|----------|-----------|
| **¿ShipmentEvent captura el estado completo?** | No. Solo captura los campos que cambiaron (fromStatus, toStatus). No almacena el snapshot completo del Shipment. |
| **¿Convertirlo a memento completo es viable?** | Técnicamente sí, pero requeriría duplicar todos los campos de Shipment en cada evento. Para N cambios, almacenarías N copias completas del mismo Shipment. |
| **¿El undo actual necesita persistencia?** | No. `undo()` restaura desde `previousState` (en memoria) llamando `repo.save(previousState)`. La base de datos contiene el estado actual, no el previo. El undo no necesita un memento en disco. |
| **¿Serviría para undo post-caída?** | Solo si además se persistiera el historial de comandos y se implementara replay de eventos. Un memento aislado no basta. |

**Decisión:** ❌ No se implementa. El escenario que justificaría esta implementación (undo después de reinicio del servidor) requeriría una infraestructura mucho más compleja que un simple memento:

1. Serializar el estado completo del Shipment en cada evento → duplicación masiva de datos
2. Persistir el historial completo de comandos con sus `previousState` → duplicación del dominio
3. Implementar replay de eventos para reconstruir `previousState` post-caída → es Event Sourcing, no Memento

Para el alcance actual del proyecto, donde el servidor no necesita undo post-reinicio, el `previousState` en memoria de `AbstractShipmentCommand` es suficiente y mucho más simple.

---

### Candidato 4: Frontend ShipmentSnapshots para cancelación de edición

**Propuesta:** Antes de que un usuario abra un formulario de edición de shipment en el frontend, capturar un snapshot completo del objeto para permitir cancelación sin pérdida de datos o restauración ante conflictos WebSocket.

```typescript
// Actual: se captura solo el status anterior en UpdateStatusCommand
class UpdateStatusCommand implements Command<Shipment> {
    private previousStatus: string | null = null;

    async execute(): Promise<Shipment> {
        const current = await shipmentService.getById(this.shipmentId);
        this.previousStatus = current.status;   // solo un campo
        return shipmentService.updateStatus(this.shipmentId, this.status);
    }
}

// Con Memento (propuesta):
class ShipmentMemento {
    private state: Record<string, unknown>;

    constructor(shipment: Shipment) {
        this.state = { ...shipment };  // snapshot completo
    }

    restore(): Shipment {
        return { ...this.state } as Shipment;
    }
}
```

**Por qué se descartó:**

| Criterio | Evaluación |
|----------|-----------|
| **¿Los formularios de edición son complejos?** | No. Los formularios tienen 1-2 campos (status en un `<select>`, location en otro `<select>`). No hay formularios multi-paso ni editores complejos. |
| **¿Cancelar edición requiere snapshot?** | No. Cancelar simplemente descarta el estado del formulario React. No se necesita restaurar nada porque el estado original no se modificó. |
| **¿Conflicto WebSocket?** | Potencialmente, pero ocurre solo si otro usuario cambia el mismo shipment simultáneamente. El snapshot ayudaría, pero el caso es marginal y no justifica la complejidad. |
| **¿Command + previousStatus ya funciona?** | Sí. Para undo de status, capturar `previousStatus` es suficiente. No se necesita capturar el Shipment completo. |

**Decisión:** ❌ No se implementa. El frontend actual captura solo el campo relevante (`previousStatus`) porque es todo lo que necesita para undo. Un snapshot completo requeriría:

1. Fetch del Shipment completo ANTES de cada edición → +1 llamada API innecesaria
2. Almacenar todos los campos en un Memento → duplicación de datos en memoria
3. Comparar campos para detectar conflictos → lógica adicional sin beneficio real

Para formularios de 1-2 campos, mantener el valor inicial en un `useState` es más simple:

```typescript
// Más simple que un Memento: solo guardar el valor inicial
const [status, setStatus] = useState(shipment.status);
const initialStatus = useRef(shipment.status);

const handleCancel = () => {
    setStatus(initialStatus.current);  // restaurar valor inicial
};
```

---

## 3. Resumen de Evaluación

| # | Candidato | Patrón actual | ¿Aplica Memento? | Decisión |
|---|-----------|--------------|------------------|----------|
| 1 | Formalizar `previousState` como ShipmentMemento | Command + `previousState: Shipment` | Conceptual — pero redundante | ❌ No. Records inmutables hacen que una referencia sea idéntica a un memento. Memento agregaría ceremonia sin beneficio. |
| 2 | Rollback atómico en CompositeShipmentCommand | Ninguno (no hay rollback) | No | ❌ No. Memento no puede deshacer `UPDATE`s commiteados. La solución correcta es `@Transactional`. |
| 3 | ShipmentEvent como Memento persistente | `ShipmentEvent` (audit log) | Parcial | ❌ No. Requeriría Event Sourcing, no solo Memento. Sin necesidad actual de undo post-caída. |
| 4 | Frontend snapshots de edición | `previousStatus: string` en UpdateStatusCommand | Sí, pero sobreingeniería | ❌ No. Formularios simples (1-2 campos). Un `useRef` con el valor inicial es suficiente. |

---

## 4. Conclusión

**Memento no se implementa en CadenaSuministros** porque el proyecto ya resuelve undo/redo mediante el **patrón Command** y los **records inmutables de Java**, haciendo que Memento sea redundante o directamente inaplicable.

### Lo que Memento requiere vs. lo que el proyecto tiene

| Memento requiere... | En CadenaSuministros... |
|--------------------|------------------------|
| Un Originator con estado mutable que encapsular | `Shipment` es un `record` inmutable. Su "estado" es público y capturable por referencia. |
| Un Memento opaco para proteger el encapsulamiento | No hay encapsulamiento que violar. El record expone todos sus campos. |
| Un Caretaker que almacene mementos sin examinarlos | `CommandHistory` ya almacena comandos. Cada comando ya guarda `previousState`. |
| Que el Originator pueda restaurarse desde el Memento | `undo()` restaura llamando `repo.save(previousState)`. No se necesita `restore()` porque el record es inmutable. |

### Lo que ya cubren otros patrones

| Necesidad | Patrón existente | Cómo lo cubre |
|-----------|-----------------|---------------|
| Capturar estado antes de mutar | **Command** + `previousState` | `AbstractShipmentCommand.previousState = current` |
| Restaurar estado anterior | **Command** + `undo()` | `repo.save(previousState)` |
| Historial de cambios | **Command** + `ShipmentEvent` | Eventos de dominio persisten cada cambio |
| Deshacer desde frontend | **Command** + `CommandHistory` | `UpdateStatusCommand` captura `previousStatus` y envía PATCH inverso |
| Rollback de operaciones múltiples | **Spring `@Transactional`** | Si se necesita atomicidad, las transacciones de base de datos son la solución correcta |

### Cuándo tendría sentido implementar Memento

Si en el futuro el proyecto incorporara:

1. **Un objeto de dominio mutable** — Por ejemplo, si `Shipment` dejara de ser un `record` y pasara a tener setters y estado interno complejo. En ese caso, `createMemento()` / `restore()` protegerían el encapsulamiento.

2. **Snapshots múltiples con navegación no-lineal** — Si se quisiera permitir al usuario "viajar" a cualquier punto del historial (ej: restaurar el estado de hace 5 cambios), no solo deshacer el último. `ShipmentCommandInvoker` solo soporta undo lineal (LIFO).

3. **Un editor visual complejo** — Si se agregara un dashboard drag-and-drop o un configurador visual de rutas donde el usuario necesitara snapshots frecuentes con preview de cambios.

En esos escenarios, Memento sería el patrón adecuado. En el estado actual del proyecto, **Command + Template Method + records inmutables** cubren undo/redo de manera más simple y directa que Memento.
