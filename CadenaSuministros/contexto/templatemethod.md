# Patrón Template Method - Implementación en CadenaSuministros

## 1. Introducción al Patrón Template Method

El **Patrón Template Method** es un patrón de comportamiento que define el esqueleto de un algoritmo en un método, difiriendo algunos pasos a las subclases. Permite que las subclases redefinan ciertos pasos de un algoritmo sin cambiar su estructura.

### Propósito
- Definir la estructura fija de un algoritmo en la clase base
- Permitir que subclases implementen partes específicas sin cambiar el flujo general
- Eliminar duplicación de código moviendo el comportamiento común a la clase base
- Garantizar que el orden de ejecución de los pasos sea consistente
- Aplicar el principio Hollywood: "No nos llames, nosotros te llamaremos"

---

## 2. Por qué se implementó en este proyecto

### Problema Identificado

En la aplicación CadenaSuministros, se identificaron múltiples casos donde **el mismo algoritmo se repetía con variaciones mínimas**:

#### Caso 1: Comandos de actualización de envíos

`UpdateStatusCommand` y `UpdateLocationCommand` compartían el mismo flujo de ejecución:

```java
// Sin Template Method — código duplicado en cada comando
// UpdateStatusCommand.execute()
Shipment current = findShipment(id);
if (current.status().equals(newStatus)) {
    eventPublisher.publishEvent(...);  // no-op
    return current;
}
Shipment updated = current.withStatus(newStatus);
Shipment saved = shipmentRepository.save(updated);
eventRepository.save(new ShipmentEvent(...));
eventPublisher.publishEvent(new ShipmentStatusChangedEvent(...));
return saved;

// UpdateLocationCommand.execute() — MISMO flujo, diferente mutación y eventos
Shipment current = findShipment(id);
if (current.currentLocation().equals(newLocation)) {
    eventPublisher.publishEvent(...);  // no-op
    return current;
}
Shipment updated = current.withLocation(newLocation);
Shipment saved = shipmentRepository.save(updated);
eventRepository.save(new ShipmentEvent(...));
eventPublisher.publishEvent(new ShipmentLocationChangedEvent(...));
return saved;
```

**Problemas:**
- El flujo `fetch → validar no-op → mutar → guardar → registrar evento → publicar evento` estaba duplicado
- Agregar un nuevo comando de actualización requería copiar todo el flujo
- El orden de los pasos podía divergir accidentalmente entre comandos
- La lógica de undo también estaba duplicada

#### Caso 2: Adaptadores JPA

`ShipmentEventJpaAdapter`, `QualityCheckpointJpaAdapter` y `ShippingCostJpaAdapter` compartían el mismo patrón de persistencia:

```java
// Sin Template Method — cada adapter repetía save/findById/findAll
public ShipmentEvent save(ShipmentEvent domain) {
    ShipmentEventJpaEntity entity = new ShipmentEventJpaEntity();
    entity.id = domain.id();
    entity.shipmentId = domain.shipmentId();
    // ... mapeo manual ...
    ShipmentEventJpaEntity saved = repo.save(entity);
    return new ShipmentEvent(saved.id, saved.shipmentId, ...);
}
```

**Problemas:**
- `save()`, `findById()` y `findAll()` se repetían en cada adapter
- Solo variaba el mapeo `domain → entity` y `entity → domain`
- Agregar un nuevo adapter requería copiar 20+ líneas de código boilerplate

#### Caso 3: Ciclo de fetch y renderizado en frontend

Cada página repetía el mismo patrón:

```tsx
// Sin Template Method — mismo patrón en cada página
const [data, setData] = useState(null);
const [loading, setLoading] = useState(true);
const [error, setError] = useState(null);

useEffect(() => {
    setLoading(true);
    fetchData()
        .then(setData)
        .catch(err => setError(err.message))
        .finally(() => setLoading(false));
}, []);

if (loading) return <Skeleton />;
if (error) return <Error error={error} />;
if (!data || data.length === 0) return <Empty />;
return <Tabla data={data} />;
```

**Problemas:**
- ~15 líneas de boilerplate por página para el ciclo loading/error/data
- La lógica de seguridad por unmounted component se implementaba inconsistentemente
- Cada página tenía su propia implementación del estado vacío

#### Caso 4: Llamadas a API

Cada servicio repetía `listAll()` y `getById()`:

```ts
// Sin Template Method — cada servicio repetía
class ProductService {
    async listAll(): Promise<Product[]> {
        return api.get<Product[]>('/products');
    }
    async getById(id: string): Promise<Product> {
        return api.get<Product>(`/products/${id}`);
    }
}

class SensorService {
    async listAll(): Promise<SensorReading[]> {
        return api.get<SensorReading[]>('/sensors');
    }
    async getById(id: string): Promise<SensorReading> {
        return api.get<SensorReading>(`/sensors/${id}`);
    }
}

class ShipmentService {
    async listAll(): Promise<Shipment[]> {
        return api.get<Shipment[]>('/shipments');
    }
    async getById(id: string): Promise<Shipment> {
        return api.get<Shipment>(`/shipments/${id}`);
    }
}
```

**Problemas:**
- Cada servicio nuevo debía implementar `listAll()` y `getById()` con la misma lógica
- Solo variaba la ruta base (`/products`, `/sensors`, `/shipments`)
- Cualquier cambio en la lógica de fetch (headers, error handling) requería modificar N servicios

### Solución

Implementar el patrón **Template Method** en 4 contextos distintos:

| Contexto | Clase Base (Template) | Subclases (Hooks) |
|----------|----------------------|--------------------|
| **Comandos de actualización** | `AbstractShipmentCommand` | `UpdateStatusCommand`, `UpdateLocationCommand` |
| **Adaptadores JPA** | `AbstractJpaAdapter<D,E>` | `ShipmentEventJpaAdapter`, `QualityCheckpointJpaAdapter`, `ShippingCostJpaAdapter` |
| **Fetch de datos** | `useDataFetch<T>` + `DataView<T>` | Consumido por páginas (`QualityControlPage`, `ShippingCostPage`, `AuditPanelPage`, `InventoryPage`) |
| **Servicios API** | `CrudService<T>` | `ProductService`, `SensorService`, `ShipmentService` |

---

## 3. Estructura Implementada

### 3.1 Backend — AbstractShipmentCommand

**Ubicación:** `src/main/java/.../domain/command/`

| Archivo | Rol |
|---------|-----|
| `AbstractShipmentCommand.java` | **Template Method**: define `execute()` y `undo()` con hooks abstractos y concretos |
| `UpdateStatusCommand.java` | Subclase: solo hooks de status (`isNoOp`, `doExecute`, `buildEvent`, `publishEvent`) |
| `UpdateLocationCommand.java` | Subclase: solo hooks de ubicación (`isNoOp`, `doExecute`, `buildEvent`, `publishEvent`) |

#### Template: `execute()`

```java
public Shipment execute() {
    Shipment current = fetchCurrent();                    // hook concreto (fijo)
    if (isNoOp(current)) {                                 // hook (default: false)
        handleNoOp(current);                                // hook (default: vacío)
        return current;
    }
    this.previousState = current;
    Shipment updated = doExecute(current);                 // hook abstracto
    Shipment saved = save(updated);                        // hook concreto (fijo)
    recordEvent(current, saved);                           // usa buildEvent (abstracto)
    publishEvent(current, saved);                          // hook (default: vacío)
    return saved;
}
```

#### Hooks de `AbstractShipmentCommand`

| Hook | Tipo | Default | Lo que varía entre subclases |
|------|------|---------|------------------------------|
| `fetchCurrent()` | Concreto | `repo.findShipmentById(id)` | Fijo para todos |
| `isNoOp(Shipment)` | Concreto | `return false` | UpdateStatus: misma status; UpdateLocation: misma ubicación |
| `handleNoOp(Shipment)` | Concreto | vacío | UpdateStatus publica evento no-op |
| `doExecute(Shipment)` | **Abstracto** | — | UpdateStatus: `withStatus()`; UpdateLocation: `withLocation()` |
| `save(Shipment)` | Concreto | `repo.save(shipment)` | Fijo para todos |
| `recordEvent(Shipment, Shipment)` | Concreto | `repo.save(buildEvent())` | Fijo para todos |
| `publishEvent(Shipment, Shipment)` | Concreto | vacío | UpdateStatus: `ShipmentStatusChangedEvent`; UpdateLocation: `ShipmentLocationChangedEvent` |
| `buildEvent(Shipment, Shipment)` | **Abstracto** | — | Cada comando construye su evento de dominio |
| `buildUndoEvent(Shipment)` | **Abstracto** | — | Cada comando construye su evento de reversión |
| `publishUndoEvent(Shipment, Shipment)` | **Abstracto** | — | Cada comando publica su evento de reversión |

### 3.2 Backend — AbstractJpaAdapter

**Ubicación:** `src/main/java/.../adapters/outbound/persistence/jpa/`

| Archivo | Rol |
|---------|-----|
| `AbstractJpaAdapter.java` | **Template Method**: define `save()`, `findById()`, `findAll()` con hooks `toEntity()` y `toDomain()` |
| `ShipmentEventJpaAdapter.java` | Subclase: solo hooks de mapeo ShipmentEvent ↔ ShipmentEventJpaEntity |
| `QualityCheckpointJpaAdapter.java` | Subclase: solo hooks de mapeo QualityCheckpoint ↔ QualityCheckpointJpaEntity |
| `ShippingCostJpaAdapter.java` | Subclase: solo hooks de mapeo ShippingCost ↔ ShippingCostJpaEntity |

#### Template: `save()`

```java
public D save(D domain) {
    E entity = toEntity(domain);     // hook abstracto
    E saved = repo.save(entity);      // JpaRepository.save() (fijo)
    return toDomain(saved);           // hook abstracto
}
```

#### Hooks de `AbstractJpaAdapter<D, E>`

| Hook | Tipo | Lo que varía entre subclases |
|------|------|------------------------------|
| `toEntity(D)` | **Abstracto** | Mapeo específico de cada tipo de dominio a su entidad JPA |
| `toDomain(E)` | **Abstracto** | Mapeo específico de cada entidad JPA a su tipo de dominio |

### 3.3 Frontend — useDataFetch + DataView

**Ubicación:** `frontend/src/hooks/` y `frontend/src/components/`

| Archivo | Rol |
|---------|-----|
| `useDataFetch.ts` | **Template Method hook**: define el ciclo fetch → loading → data/error con cleanup |
| `DataView.tsx` | **Template Method componente**: define el ciclo render loading → error → empty → content |
| `SortableTable.tsx` | **Template Method componente**: define el render de tabla ordenable |

#### Template: `useDataFetch<T>`

```typescript
function useDataFetch<T>(fetcher: () => Promise<T>, deps = []) {
  const [data, setData] = useState<T | null>(null);    // estado: data
  const [loading, setLoading] = useState(true);          // estado: loading
  const [error, setError] = useState<string | null>(null); // estado: error
  const mountedRef = useRef(true);                        // cleanup hook

  const execute = useCallback(async () => {
    setLoading(true);                                    // paso 1: loading
    setError(null);                                      // paso 2: limpiar error
    try {
      const result = await fetcher();                    // paso 3: ejecutar fetcher (hook)
      if (mountedRef.current) setData(result);           // paso 4: setear data
    } catch (err) {
      if (mountedRef.current) setError(err.message);     // paso 5: setear error
    } finally {
      if (mountedRef.current) setLoading(false);          // paso 6: fin loading
    }
  }, deps);

  useEffect(() => {
    mountedRef.current = true;
    execute();                                            // ejecutar al montar
    return () => { mountedRef.current = false; };         // cleanup al desmontar
  }, [execute]);

  return { data, loading, error, refresh: execute };
}
```

#### Template: `DataView<T>`

```typescript
function DataView<T>({ loading, error, data, skeleton, errorRender, empty, isEmpty, children }) {
  if (loading && !data) return skeleton ?? <DefaultSkeleton />;       // paso 1: loading
  if (error && !data) return errorRender ?? <DefaultError retry />;   // paso 2: error
  if (!data) return null;                                              // paso 3: sin datos
  if (isEmpty(data) || (Array.isArray(data) && data.length === 0))    // paso 4: vacío
    return empty ?? <DefaultEmpty />;
  return children(data);                                               // paso 5: contenido
}
```

#### Uso en páginas

```tsx
// QualityControlPage.tsx — el fetcher y el render varían
const { data: checkpoints, loading, error, refresh } = useDataFetch(async () => {
  const [qc, sh] = await Promise.all([qualityService.listAll(), shipmentService.listAll()]);
  setShipments(sh);
  return qc;
});

return (
  <DataView loading={loading} error={error} data={checkpoints} onRetry={refresh}>
    {() => <SortableTable<QualityCheckpoint> data={filtered} columns={columns} />}
  </DataView>
);
```

### 3.4 Frontend — CrudService

**Ubicación:** `frontend/src/api/`

| Archivo | Rol |
|---------|-----|
| `CrudService.ts` | **Template Method**: define `listAll()` y `getById()` con hook `basePath()` |
| `productService.ts` | Subclase: solo hook `basePath() = '/products'` |
| `sensorService.ts` | Subclase: solo hook `basePath() = '/sensors'` |
| `shipmentService.ts` | Subclase: solo hook `basePath() = '/shipments'` |

#### Template

```typescript
export abstract class CrudService<T> {
  protected abstract basePath(): string;    // hook abstracto

  async listAll(): Promise<T[]> {           // template
    return api.get<T[]>(this.basePath());   // usa hook
  }

  async getById(id: string): Promise<T> {   // template
    return api.get<T>(`${this.basePath()}/${id}`);
  }
}
```

#### Subclase

```typescript
class ProductService extends CrudService<Product> {
  protected basePath() { return '/products'; }    // solo implementa el hook
  async create(sku: string, name: string) { ... } // métodos propios
}
```

---

## 4. Cómo funciona

### Ejemplo 1: UpdateStatusCommand.execute()

```
llamada → AbstractShipmentCommand.execute()
  │
  ├─ 1. fetchCurrent() → ShipmentRepository.findShipmentById(id)
  │      (hook concreto: igual para todos los comandos)
  │
  ├─ 2. isNoOp(current) → current.status().equals(newStatus)?
  │      (hook: UpdateStatusCommand compara status, UpdateLocationCommand compara ubicación)
  │
  ├─ 3. [si isNoOp] handleNoOp(current) → publica evento sin persistir
  │      (hook: UpdateStatusCommand publica ShipmentStatusChangedEvent)
  │
  ├─ 4. doExecute(current) → current.withStatus(newStatus)
  │      (hook abstracto: cada comando implementa su mutación)
  │
  ├─ 5. save(updated) → ShipmentRepository.save(updated)
  │      (hook concreto: igual para todos)
  │
  ├─ 6. recordEvent(current, saved) → eventRepository.save(buildEvent(...))
  │      (hook concreto: usa buildEvent abstracto internamente)
  │
  ├─ 7. buildEvent(current, saved) → new ShipmentEvent(...)
  │      (hook abstracto: UpdateStatusCommand registra cambio de status)
  │
  └─ 8. publishEvent(current, saved) → ApplicationEventPublisher.publish(...)
         (hook: UpdateStatusCommand publica ShipmentStatusChangedEvent)
```

### Ejemplo 2: QualityCheckpointJpaAdapter.save()

```
llamada → AbstractJpaAdapter.save(checkpoint)
  │
  ├─ 1. toEntity(checkpoint) → new QualityCheckpointJpaEntity(...)
  │      (hook: mapea QualityCheckpoint → entidad JPA)
  │
  ├─ 2. repo.save(entity) → JpaRepository.save()
  │      (fijo: Spring Data JPA)
  │
  └─ 3. toDomain(savedEntity) → new QualityCheckpoint(...)
         (hook: mapea entidad JPA → QualityCheckpoint)
```

### Ejemplo 3: DataView render en página

```
Página carga → useDataFetch()
  │
  ├─ loading = true → DataView muestra <DefaultSkeleton />
  │
  ├─ fetch exitoso → data = [...], loading = false
  │
  ├─ data vacío (isEmpty) → DataView muestra <DefaultEmpty />
  │
  └─ data con contenido → DataView ejecuta children(data)
       └─ SortableTable<QualityCheckpoint> con filtered, columns
```

---

## 5. Beneficios

### Beneficios del Patrón

| Beneficio | Descripción |
|-----------|-------------|
| **Eliminación de duplicación** | El flujo común vive en la clase base; las subclases solo implementan lo que varía |
| **Consistencia** | El orden de los pasos está garantizado por el template; no puede divergir |
| **Open/Closed Principle** | Nuevos comandos, adaptadores o servicios se agregan creando nuevas subclases sin modificar las existentes |
| **Hollywood Principle** | La clase base controla el flujo; las subclases solo implementan hooks |
| **Reducción de código** | UpdateStatusCommand pasó de ~60 líneas a ~40 (solo hooks). QualityCheckpointJpaAdapter de ~40 a ~20 |
| **Testing más fácil** | El template se prueba una vez; cada subclase se prueba solo con sus hooks |

### Comparación: Sin vs Con Template Method

| Aspecto | Sin Template Method | Con Template Method |
|---------|---------------------|---------------------|
| **UpdateStatusCommand** | ~60 líneas (flujo + lógica específica mezclados) | ~40 líneas (solo hooks) |
| **UpdateLocationCommand** | ~60 líneas (flujo duplicado) | ~35 líneas (solo hooks diferentes) |
| **ShipmentEventJpaAdapter** | ~50 líneas (save + findById + findAll + mapeo) | ~30 líneas (solo toEntity + toDomain + query custom) |
| **QualityCheckpointJpaAdapter** | ~55 líneas | ~35 líneas |
| **Nuevo comando** | Copiar y pegar todo el flujo | Extender AbstractShipmentCommand, implementar 5 hooks |
| **Nuevo adapter** | Copiar save/findById/findAll | Extender AbstractJpaAdapter, implementar 2 hooks |
| **Nuevo servicio API** | Copiar listAll/getById | Extender CrudService, implementar 1 hook |

### Archivos del Patrón

#### Backend

| Archivo | Rol | Tipo |
|---------|-----|------|
| `domain/command/AbstractShipmentCommand.java` | Template Method: execute() y undo() | AbstractClass |
| `domain/command/UpdateStatusCommand.java` | ConcreteClass: hooks de status | ConcreteClass |
| `domain/command/UpdateLocationCommand.java` | ConcreteClass: hooks de ubicación | ConcreteClass |
| `persistence/jpa/AbstractJpaAdapter.java` | Template Method: save/findById/findAll | AbstractClass |
| `persistence/jpa/ShipmentEventJpaAdapter.java` | ConcreteClass: toEntity/toDomain de ShipmentEvent | ConcreteClass |
| `persistence/jpa/QualityCheckpointJpaAdapter.java` | ConcreteClass: toEntity/toDomain de QualityCheckpoint | ConcreteClass |
| `persistence/jpa/ShippingCostJpaAdapter.java` | ConcreteClass: toEntity/toDomain de ShippingCost | ConcreteClass |

#### Frontend

| Archivo | Rol | Tipo |
|---------|-----|------|
| `hooks/useDataFetch.ts` | Template Method: ciclo fetch | AbstractClass (hook genérico) |
| `components/DataView.tsx` | Template Method: render loading→error→empty→content | AbstractClass (componente genérico) |
| `components/SortableTable.tsx` | Template Method: tabla ordenable con sort | AbstractClass (componente genérico) |
| `api/CrudService.ts` | Template Method: listAll/getById | AbstractClass |
| `api/productService.ts` | ConcreteClass: basePath='/products' | ConcreteClass |
| `api/sensorService.ts` | ConcreteClass: basePath='/sensors' | ConcreteClass |
| `api/shipmentService.ts` | ConcreteClass: basePath='/shipments' | ConcreteClass |
| `pages/QualityControlPage.tsx` | Consumidor de DataView + useDataFetch | Client |
| `pages/ShippingCostPage.tsx` | Consumidor de DataView + useDataFetch | Client |
| `pages/InventoryPage.tsx` | Consumidor de DataView + useDataFetch | Client |
| `pages/AuditPanelPage.tsx` | Consumidor de DataView + useDataFetch | Client |

### Lo que NO se cambió

| Caso | Razón |
|------|-------|
| `CreateShipmentCommand` | No sigue el mismo algoritmo que los comandos de actualización (crea vs actualiza). Template Method no aporta. |
| `InventoryJpaAdapter` | Maneja 2 entidades (InventoryItem + StockMovement) con 2 repositorios. No encaja en AbstractJpaAdapter de entidad única. |
| `JpaAdapters` | Monolito que adapta 4 entidades. Dividirlo requeriría refactor mayor fuera del alcance. |
| `DashboardPage`, `SensorsPage`, `ShipmentsPage`, `ProductsPage`, `ReportsPage`, `ProductDashboardPage` | Tienen lógica de fetching más compleja (múltiples fuentes, WebSockets, polling) que no se beneficia del `useDataFetch` simple. |

---

## 6. Consideraciones Técnicas

### Hook Methods: Tipos

| Tipo de Hook | Comportamiento | Ejemplo |
|-------------|----------------|---------|
| **Abstracto (obligatorio)** | La subclase DEBE implementarlo | `doExecute()`, `buildEvent()`, `toEntity()`, `toDomain()`, `basePath()` |
| **Concreto (override opcional)** | Tiene implementación default que la subclase PUEDE sobrescribir | `isNoOp()`, `publishEvent()`, `handleNoOp()` |
| **Concreto (fijo)** | Tiene implementación que NO debe sobrescribirse | `fetchCurrent()`, `save()` en AbstractShipmentCommand |

### Template Method vs Strategy

| Aspecto | Template Method | Strategy |
|---------|----------------|----------|
| **Nivel** | Herencia (clase base → subclase) | Composición (contexto → strategy) |
| **Control** | La clase base controla el algoritmo | El contexto delega en la strategy |
| **Variación** | Pasos específicos del algoritmo | Algoritmo completo |
| **Cuándo usarlo** | Cuando el algoritmo es fijo y solo varían pasos | Cuando hay múltiples algoritmos completos intercambiables |

En el proyecto, **Template Method** se usó para pipelines fijos (comandos, persistencia, fetch) mientras que **Strategy** se usó para algoritmos intercambiables (cálculo de costos).

### Template Method y el Command Pattern

`AbstractShipmentCommand` extiende el patrón Command (ya implementado) con Template Method:
- **Command**: `ShipmentCommand` define la interfaz (`execute()`, `undo()`)
- **Template Method**: `AbstractShipmentCommand` implementa el esqueleto de `execute()` y `undo()`
- Ambas subclases (`UpdateStatusCommand`, `UpdateLocationCommand`) solo implementan hooks

### Implementación en Dos Capas (Backend + Frontend)

| Capa | Template | Lo que estandariza |
|------|----------|-------------------|
| **Backend** | `AbstractShipmentCommand` | Pipeline de ejecución de comandos |
| **Backend** | `AbstractJpaAdapter<D,E>` | Pipeline de persistencia JPA |
| **Frontend** | `useDataFetch<T>` + `DataView<T>` | Ciclo de vida de fetch y render |
| **Frontend** | `CrudService<T>` | Operaciones CRUD vía API |
| **Frontend** | `SortableTable<T>` | Renderizado de tablas ordenables |

### Para agregar un nuevo elemento

#### Nuevo comando de actualización
1. Extender `AbstractShipmentCommand`
2. Implementar hooks: `doExecute()`, `buildEvent()`, `publishEvent()`, `buildUndoEvent()`, `publishUndoEvent()`
3. Opcional: override `isNoOp()` y `handleNoOp()`

#### Nuevo adaptador JPA
1. Extender `AbstractJpaAdapter<Dominio, EntidadJpa>`
2. Implementar hooks: `toEntity()`, `toDomain()`
3. Agregar métodos custom (query por ID, etc.)

#### Nuevo servicio API
1. Extender `CrudService<Tipo>`
2. Implementar hook: `basePath()`
3. Agregar métodos custom

---

## 7. Conclusión

El patrón Template Method se implementó en 4 contextos distintos dentro de CadenaSuministros, eliminando código duplicado y estableciendo pipelines consistentes:

| Contexto | Antes | Después |
|----------|-------|---------|
| **Comandos de actualización** | Flujo execute/undo duplicado en cada comando | `AbstractShipmentCommand` define el template; subclases solo implementan hooks |
| **Adaptadores JPA** | save/findById/findAll repetidos en cada adapter | `AbstractJpaAdapter<D,E>` define el template; subclases solo mapean |
| **Fetch + render** | ~15 líneas de boilerplate por página | `useDataFetch` + `DataView` manejan el ciclo completo |
| **Servicios API** | listAll/getById duplicados en cada servicio | `CrudService<T>` define el template; servicios solo dan la ruta |

La implementación sigue los principios SOLID, especialmente el **Open/Closed Principle**: nuevas funcionalidades se agregan extendiendo clases base sin modificar el código existente.

### Estado de los tests

```
Backend:  mvn compile ✓ (0 errores)
Frontend: tsc --noEmit ✓ (0 errores)
```
