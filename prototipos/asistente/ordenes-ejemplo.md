# Órdenes de ejemplo para el asistente de inventario (Floristería en Cali)

Este documento define **28 órdenes realistas en español colombiano** (contexto de taller floral en Cali, Valle del Cauca) junto con el **catálogo inicial ficticio** sobre el que se evalúan y el **resultado esperado** para cada una.

> **Regla fundamental (D-004):** Ninguna orden modifica el inventario de inmediato. Cuando una orden de escritura es válida e inequívoca, el asistente **solo registra una propuesta pendiente de confirmación** (`PENDING_CONFIRMATION`) mostrando lo que entendió y el cambio proyectado. Si algo es ambiguo, inexistente o imposible, **no crea propuesta ejecutable** y responde pidiendo aclaración o explicando el motivo.

---

## Catálogo base ficticio (estado inicial)

Todos los precios y costos están expresados en **pesos colombianos enteros (COP)**. Todos los nombres de proveedores son ficticios.

| ID | SKU | Producto | Categoría | Unidad | Stock | Mínimo | Costo (COP) | Precio (COP) | Proveedor | Activo |
|---:|---|---|---|---|---:|---:|---:|---:|---|---|
| 1 | `FL-001` | Rosa Freedom roja | Flores | tallo | 84 | 30 | 1800 | 4200 | Cultivos La Montaña | Sí |
| 2 | `FL-002` | Rosa Vendela marfil | Flores | tallo | 48 | 24 | 2000 | 4500 | Cultivos La Montaña | Sí |
| 3 | `FL-003` | Tulipán holandés | Flores | tallo | 42 | 15 | 6200 | 12500 | Importadora Flora | Sí |
| 4 | `FL-004` | Hortensia azul | Flores | tallo | 7 | 10 | 9500 | 18500 | Jardines del Cauca | Sí |
| 5 | `FL-005` | Girasol San Vicente | Flores | tallo | 30 | 12 | 2500 | 5800 | Agroflores Dapa | Sí |
| 6 | `FO-001` | Eucalipto Baby Blue | Follajes | ramo | 18 | 20 | 2400 | 5500 | Verde Vivo | Sí |
| 7 | `EM-001` | Papel coreano marfil | Empaque | pliego | 9 | 12 | 2100 | 4800 | Empaques Nativa | Sí |
| 8 | `EM-002` | Cinta de algodón crudo | Empaque | rollo | 26 | 10 | 900 | 2400 | Empaques Nativa | Sí |
| 9 | `IN-001` | Espuma floral Oasis | Insumos | bloque | 15 | 8 | 3200 | 6500 | Insumos del Valle | Sí |

---

## 1. Agregar stock (entradas)

### ORD-01 · Entrada estándar de flor fresca
- **Orden:** `"Mirá, acaban de llegar 50 tallos de Rosa Freedom roja del proveedor Cultivos La Montaña."`
- **Resultado esperado:** Propuesta pendiente `RECORD_MOVEMENT` (`ENTRY`) sobre `FL-001` (`Rosa Freedom roja`), cantidad `50`, motivo `"Llegada de proveedor Cultivos La Montaña"`, stock proyectado `84 -> 134`. Requiere confirmación antes de tocar el inventario.

### ORD-02 · Reposición de follaje
- **Orden:** `"Métele 15 ramos de eucalipto baby blue que trajo el camión esta mañana."`
- **Resultado esperado:** Propuesta pendiente `RECORD_MOVEMENT` (`ENTRY`) sobre `FO-001` (`Eucalipto Baby Blue`), cantidad `15`, motivo `"Reposición recibida en la mañana"`, stock proyectado `18 -> 33`. Requiere confirmación.

### ORD-03 · Compra rápida en plaza
- **Orden:** `"Sumale 24 girasoles San Vicente que compramos temprano para los ramos del día."`
- **Resultado esperado:** Propuesta pendiente `RECORD_MOVEMENT` (`ENTRY`) sobre `FL-005` (`Girasol San Vicente`), cantidad `24`, stock proyectado `30 -> 54`. Requiere confirmación.

### ORD-04 · Entrada de insumos de empaque
- **Orden:** `"Entraron 20 pliegos de papel coreano marfil por pedido a Empaques Nativa."`
- **Resultado esperado:** Propuesta pendiente `RECORD_MOVEMENT` (`ENTRY`) sobre `EM-001` (`Papel coreano marfil`), cantidad `20`, stock proyectado `9 -> 29`. Requiere confirmación.

---

## 2. Quitar stock (salidas, mermas y ajustes de conteo)

### ORD-05 · Salida por arreglo vendido
- **Orden:** `"Descontame 12 tallos de Rosa Vendela marfil que salieron para un arreglo de grado en Ciudad Jardín."`
- **Resultado esperado:** Propuesta pendiente `RECORD_MOVEMENT` (`EXIT`) sobre `FL-002` (`Rosa Vendela marfil`), cantidad `12`, motivo `"Arreglo de grado en Ciudad Jardín"`, stock proyectado `48 -> 36`. Requiere confirmación.

### ORD-06 · Salida de insumo para evento
- **Orden:** `"Salieron 5 bloques de espuma floral Oasis para el montaje del hotel en Granada."`
- **Resultado esperado:** Propuesta pendiente `RECORD_MOVEMENT` (`EXIT`) sobre `IN-001` (`Espuma floral Oasis`), cantidad `5`, stock proyectado `15 -> 10`. Requiere confirmación.

### ORD-07 · Merma por daño térmico
- **Orden:** `"Tocó botar 6 tulipanes holandeses porque llegaron con el pétalo quemado por el calor."`
- **Resultado esperado:** Propuesta pendiente `RECORD_MOVEMENT` (`WASTE`) sobre `FL-003` (`Tulipán holandés`), cantidad `6`, motivo `"Pétalo quemado por calor"`, stock proyectado `42 -> 36`. Requiere confirmación.

### ORD-08 · Merma en cava
- **Orden:** `"Se marchitaron 2 hortensias azules en la cava, registralas como merma."`
- **Resultado esperado:** Propuesta pendiente `RECORD_MOVEMENT` (`WASTE`) sobre `FL-004` (`Hortensia azul`), cantidad `2`, motivo `"Flores marchitas en cava"`, stock proyectado `7 -> 5` (queda bajo mínimo). Requiere confirmación.

### ORD-09 · Ajuste por conteo físico
- **Orden:** `"Hicimos conteo físico de cinta de algodón crudo y en realidad hay 23 rollos en el estante, ajustá el stock."`
- **Resultado esperado:** Propuesta pendiente `RECORD_MOVEMENT` (`ADJUSTMENT`) sobre `EM-002` (`Cinta de algodón crudo`), nuevo stock objetivo `23` (diferencia `-3` respecto a `26`, cantidad del ajuste `23`), motivo `"Conteo físico en estante"`. Requiere confirmación.

---

## 3. Crear productos nuevos

### ORD-10 · Creación completa con stock inicial
- **Orden:** `"Creame un producto nuevo: Astromelia blanca, categoría Flores, unidad ramo, SKU FL-006, llegaron 20 ramos, stock mínimo 8, costo 3500 pesos, precio de venta 7500 pesos, proveedor Jardines del Cauca."`
- **Resultado esperado:** Propuesta pendiente `CREATE_PRODUCT` (`sku="FL-006"`, `name="Astromelia blanca"`, `category="Flores"`, `unit="ramo"`, `initialStock=20`, `minStock=8`, `cost=3500`, `price=7500`, `supplier="Jardines del Cauca"`). Al confirmarse crea el producto activo y deja registrado el movimiento inicial `ENTRY` de `20`.

### ORD-11 · Creación de empaque sin stock inicial
- **Orden:** `"Agregá al catálogo Caja cilíndrica negra mediana en Empaque, unidad unidad, código EM-003, costo 8000, precio 16000, stock mínimo 5, stock inicial 0, proveedor Empaques Nativa."`
- **Resultado esperado:** Propuesta pendiente `CREATE_PRODUCT` (`sku="EM-003"`, `initialStock=0`, `minStock=5`, `cost=8000`, `price=16000`). Requiere confirmación.

### ORD-12 · Intento de crear producto con SKU duplicado
- **Orden:** `"Registrá un producto nuevo con SKU FL-001 llamado Clavel rojo estándar, categoría Flores, unidad tallo, costo 900, precio 2200, stock 40, mínimo 20, proveedor Cultivos La Montaña."`
- **Resultado esperado:** Rechazo por validación (`VALIDATION_ERROR`, sin acción pendiente): el SKU `FL-001` ya pertenece a `Rosa Freedom roja`. Explica el conflicto y sugiere usar otro SKU.

---

## 4. Modificar productos existentes

### ORD-13 · Actualización de precio y costo
- **Orden:** `"Subile el precio de venta a la Hortensia azul a 21000 pesos y el costo a 10500."`
- **Resultado esperado:** Propuesta pendiente `UPDATE_PRODUCT` sobre `FL-004` (`Hortensia azul`), cambiando `cost: 9500 -> 10500` y `price: 18500 -> 21000` sin alterar su stock. Requiere confirmación.

### ORD-14 · Cambio de stock mínimo
- **Orden:** `"Cambiále el stock mínimo a la Rosa Freedom roja a 40 tallos por temporada de grados."`
- **Resultado esperado:** Propuesta pendiente `UPDATE_PRODUCT` sobre `FL-001` (`Rosa Freedom roja`), cambiando `minStock: 30 -> 40`. Requiere confirmación.

### ORD-15 · Cambio de proveedor
- **Orden:** `"Actualizá el proveedor del Eucalipto Baby Blue a Follajes de la Cumbre."`
- **Resultado esperado:** Propuesta pendiente `UPDATE_PRODUCT` sobre `FO-001` (`Eucalipto Baby Blue`), cambiando `supplier: "Verde Vivo" -> "Follajes de la Cumbre"`. Requiere confirmación.

---

## 5. Eliminar (desactivar) productos

### ORD-16 · Eliminar flor de temporada (desactivación lógica según D-005)
- **Orden:** `"Eliminá del catálogo el Tulipán holandés que por temporada ya no vamos a traer más."`
- **Resultado esperado:** Propuesta pendiente `DEACTIVATE_PRODUCT` sobre `FL-003` (`Tulipán holandés`). Aclara que el producto se **desactiva** (`active: true -> false`) conservando su historial y requiere confirmación.

### ORD-17 · Desactivar empaque por código SKU
- **Orden:** `"Desactivá el papel coreano marfil, código EM-001, porque el proveedor lo descontinuó."`
- **Resultado esperado:** Propuesta pendiente `DEACTIVATE_PRODUCT` sobre `EM-001` (`Papel coreano marfil`), `active: true -> false`. Requiere confirmación.

---

## 6. Casos ambiguos (debe preguntar, nunca adivinar)

### ORD-18 · Producto ambiguo (múltiples variedades de rosa)
- **Orden:** `"Descontame 10 rosas que se vendieron ahorita en mostrador."`
- **Resultado esperado:** `CLARIFICATION_REQUIRED` (sin propuesta pendiente). Existen dos rosas activas (`Rosa Freedom roja` y `Rosa Vendela marfil`); el asistente pregunta cuál de las dos variedades se vendió.

### ORD-19 · Falta la cantidad en una entrada
- **Orden:** `"Llegó pedido nuevo de eucalipto baby blue, metelo al inventario."`
- **Resultado esperado:** `CLARIFICATION_REQUIRED` (sin propuesta pendiente). El producto es claro (`FO-001`), pero falta cuántos ramos llegaron.

### ORD-20 · Producto y cantidad ambiguos
- **Orden:** `"Quitá unas flores que salieron malas hoy en la mañana."`
- **Resultado esperado:** `CLARIFICATION_REQUIRED` (sin propuesta pendiente). Pregunta qué flor exacta y cuántos tallos/ramos se deben registrar como merma.

### ORD-21 · Datos incompletos al crear producto
- **Orden:** `"Creame Lirio oriental rosado en el catálogo para empezar a manejarlo."`
- **Resultado esperado:** `CLARIFICATION_REQUIRED` (sin propuesta pendiente). Pide los campos obligatorios faltantes (SKU, unidad de medida, costo, precio de venta y stock mínimo/inicial).

---

## 7. Productos inexistentes

### ORD-22 · Salida de flor que no existe en catálogo
- **Orden:** `"Descontame 8 varas de orquídea phalaenopsis blanca para un arreglo de aniversario."`
- **Resultado esperado:** `PRODUCT_NOT_FOUND` (sin propuesta pendiente). Informa que `Orquídea phalaenopsis blanca` no figura en el inventario y pregunta si desea crearla o quiso decir otro producto.

### ORD-23 · Modificación de follaje no registrado
- **Orden:** `"Cambiále el precio al ruscus italiano a 6500 pesos."`
- **Resultado esperado:** `PRODUCT_NOT_FOUND` (sin propuesta pendiente). Informa que no hay ningún producto llamado `ruscus italiano` en el inventario.

### ORD-24 · Eliminación de empaque inexistente
- **Orden:** `"Eliminá las cajas acrílicas transparentes del sistema."`
- **Resultado esperado:** `PRODUCT_NOT_FOUND` (sin propuesta pendiente). Informa que no existen `cajas acrílicas transparentes` activas en el catálogo.

---

## 8. Cantidades imposibles

### ORD-25 · Salida mayor al stock disponible
- **Orden:** `"Sacame 15 hortensias azules para el matrimonio en Pance."`
- **Resultado esperado:** `IMPOSSIBLE_QUANTITY` (sin propuesta pendiente ejecutable). `Hortensia azul` (`FL-004`) solo tiene `7` tallos en stock; descontar `15` dejaría el inventario en `-8`. Informa el stock disponible (`7`).

### ORD-26 · Cantidad negativa en movimiento
- **Orden:** `"Registrá una entrada de -10 girasoles San Vicente."`
- **Resultado esperado:** `IMPOSSIBLE_QUANTITY` (sin propuesta pendiente). Las entradas y salidas requieren cantidades positivas mayores a cero; pregunta si quiso registrar una salida o merma de `10` tallos.

### ORD-27 · Movimiento con cantidad cero
- **Orden:** `"Descontame 0 rollos de cinta de algodón crudo."`
- **Resultado esperado:** `IMPOSSIBLE_QUANTITY` (sin propuesta pendiente). No se permiten movimientos de `0` unidades; solicita una cantidad entera mayor a cero.

### ORD-28 · Cantidad fraccionaria para unidad entera
- **Orden:** `"Vendimos dos tallos y medio (2.5) de Rosa Freedom roja."`
- **Resultado esperado:** `IMPOSSIBLE_QUANTITY` (sin propuesta pendiente). El stock se gestiona en unidades enteras (`tallo`), no en decimales (`2.5`); pide indicar un número entero de tallos.
