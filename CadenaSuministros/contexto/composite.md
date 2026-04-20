# Patrón Composite - Implementación en CadenaSuministros

## 1. Introducción al Patrón Composite

El **Patrón Composite** es un patrón estructural que permite componer objetos en estructuras de árbol para representar jerarquías parte-todo. Permite tratar objetos individuales (hojas) y composiciones de objetos (grupos) de manera uniforme.

### Propósito
- Representar jerarquías parte-todo (tree structures)
- Permitir que clientes ignoren la diferencia entre objetos individuales y compuestos
- Operar uniformemente sobre objetos simples y complejos

---

## 2. Por qué se implementó en este proyecto

### Problema del Dominio

En una cadena de suministro, los envíos pueden representarse de dos formas:

1. **Envío individual**: Un producto único (ej: 1 caja de leche)
2. **Paquete**: Una agrupación de múltiples envíos (ej: 1 pallet con 10 cajas)

Los clientes necesitan operar sobre ambos tipos de la misma manera:
- Calcular valor total
- Contar productos
- Generar reportes
- Rastrear estado

### Solución

El patrón Composite permite tratar un envío individual y un pallet completo con la misma interfaz, ocultando la complejidad de la estructura interna.

---

## 3. Estructura Implementada

### Ubicación
`src/main/java/com/cadenasuministros/domain/model/composite/`

### Archivos

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `ShipmentComponent.java` | Sealed Interface | Interfaz común para Leaf y Composite |
| `ShipmentItem.java` | Leaf | Representa un envío individual |
| `ShipmentPackage.java` | Composite | Representa un paquete con hijos |
| `ShipmentStatistics.java` | Record | DTO para estadísticas calculadas |

### Diagrama de Clases

```
ShipmentComponent (sealed interface)
    │
    ├── + getId(): UUID
    ├── + getName(): String
    ├── + getTotalValue(): BigDecimal
    ├── + getProductCount(): int
    ├── + getAllProductIds(): List<String>
    ├── + calculateStatistics(): ShipmentStatistics
    ├── + addChild(ShipmentComponent)
    ├── + removeChild(UUID)
    └── + getChildren(): List<ShipmentComponent>
            │
            ├── ShipmentItem (Leaf)
            │       - id, name, productId, quantity, unitPrice
            │       - addChild/removeChild → UnsupportedOperationException
            │
            └── ShipmentPackage (Composite)
                    - id, name, packageType, children
                    - Implementa todas las operaciones
                    - Cálculo recursivo de estadísticas
```

---

## 4. Cómo funciona

### Ejemplo de Uso

```java
import com.cadenasuministros.domain.model.composite.*;
import java.math.BigDecimal;
import java.util.UUID;

// Crear envíos individuales (Leaf)
ShipmentItem leche = new ShipmentItem(
    UUID.randomUUID(), 
    "Leche Entera", 
    "PROD-001", 
    10, 
    new BigDecimal("25.00")
);  // Valor: $250.00

ShipmentItem queso = new ShipmentItem(
    UUID.randomUUID(), 
    "Queso Manchego", 
    "PROD-002", 
    5, 
    new BigDecimal("40.00")
);  // Valor: $200.00

// Crear paquete (Composite)
ShipmentPackage pallet = new ShipmentPackage(
    UUID.randomUUID(), 
    "Pallet 1", 
    "PALLET"
);
pallet.addChild(leche);
pallet.addChild(queso);

// Operar uniformemente sobre el paquete
BigDecimal total = pallet.getTotalValue();  // $450.00
int productos = pallet.getProductCount();   // 15
List<String> productosIds = pallet.getAllProductIds(); // [PROD-001, PROD-002]

// Calcular estadísticas (recursivo)
ShipmentStatistics stats = pallet.calculateStatistics();
System.out.println("Items: " + stats.totalItems());        // 2
System.out.println("Productos: " + stats.totalProducts());  // 15
System.out.println("Valor: " + stats.totalValue());        // $450.00
System.out.println("Promedio: " + stats.averageValue());   // $225.00
```

### Jerarquía Anidada

```java
// Crear sub-paquete
ShipmentPackage caja = new ShipmentPackage(
    UUID.randomUUID(), 
    "Caja 1", 
    "CAJA"
);
caja.addChild(leche);

// Agregar sub-paquete al pallet
pallet.addChild(caja);

// El cálculo es recursivo: pallet incluye todo
BigDecimal total = pallet.getTotalValue();  // $450.00 (leche + queso)
```

### Operaciones de Modificación

```java
// Agregar hijo
pallet.addChild(nuevoItem);

// Remover hijo
pallet.removeChild(idDelItem);

// Obtener hijos
List<ShipmentComponent> hijos = pallet.getChildren();

// Crear nuevo paquete con hijo añadido (inmutable)
ShipmentPackage nuevoPallet = pallet.withAddedChild(nuevoItem);
```

---

## 5. Beneficios en este Proyecto

### Beneficios del Patrón

| Beneficio | Descripción |
|-----------|-------------|
| **Uniformidad** | Cliente trata igual un item y un paquete |
| **Extensibilidad** | Fácil agregar nuevos tipos de componentes |
| **Jerarquías complejas** | Soporta múltiples niveles de anidamiento |
| **Cálculo recursivo** | Estadísticas se calculan automáticamente |
| **Código limpio** | Elimina condicionales del tipo "es compuesto o simple" |

### Beneficios de la Implementación

1. **Sealed Interface**: Garantiza que solo existen ShipmentItem y ShipmentPackage
2. **Inmutabilidad parcial**: ShipmentPackage puede crear copias inmutables con `withAddedChild()`
3. **Cálculo automático**: Las estadísticas se calculan recursivamente sin intervención del cliente
4. **Excepciones claras**: Items individuales lanzan excepción al intentar agregar hijos

### Comparación: Sin vs Con Composite

| Operación | Sin Composite | Con Composite |
|-----------|---------------|---------------|
| Calcular total | `if (isPackage) sumChildren()` | `shipment.getTotalValue()` |
| Contar productos | `if (isPackage) countRecursive()` | `shipment.getProductCount()` |
| Agregar item | Depende del tipo | `shipment.addChild()` |

---

## 6. Consideraciones Técnicas

### Java Features Utilizadas

- **Sealed Classes** (Java 17): Garantiza tipado seguro del Component
- **Records**: Para ShipmentStatistics (inmutable, conciso)
- **Varargs**: Para constructores flexibles
- **Stream API**: Para cálculos agregados

### Integración con la Arquitectura Hexagonal

El patrón Composite está ubicado en la **capa de dominio** (`domain/model/composite/`):

```
domain/
├── model/
│   ├── composite/     ← Composite pattern
│   ├── Shipment.java
│   ├── SensorReading.java
│   └── ...
```

### Extendibilidad

Para agregar nuevos comportamientos:
1. Agregar método a `ShipmentComponent`
2. Implementar en `ShipmentItem`
3. Implementar en `ShipmentPackage`

---

## 7. Conclusión

El patrón Composite proporciona una solución elegante para representar jerarquías de envíos en la cadena de suministro. Permite:

- Tratar envíos individuales y paquetes uniformemente
- Calcular estadísticas de forma automática y recursiva
- Extender fácilmente con nuevos tipos de componentes
- Mantener el código limpio sin condicionales complejos

Esta implementación sigue los principios de la arquitectura hexagonal, residiendo en la capa de dominio y siendo agnóstico a los frameworks de entrada y salida.
