# Plan

## Fase 0 — Cimientos (bloquea lo demás, va primero)
**daanielxyz**
- [x] Esqueleto `backend/` (Spring Boot con Maven Wrapper) y `frontend/` (Vite).
- [x] PostgreSQL con Docker Compose y primera migración.
- [x] Contrato del inventario: entidades, interfaz `InventoryService` y endpoints.

**Nicolás (puede empezar ya, en `prototipos/asistente/`)**
- [ ] 20 a 30 órdenes de ejemplo con el resultado esperado (incluir ambiguas y
      erróneas).
- [ ] Proyecto Spring Boot independiente con `InventoryService` en memoria.
- [ ] Probar Gemini con texto y con audio en español usando herramientas.
- [ ] Revisar y aprobar el contrato cuando esté.

### Contrato del inventario
La fuente de verdad es el código en `backend/src/main/java/com/dore/inventory/`:
`InventoryService` y sus records. **Nicolás debe revisarlo.**

- `GET /api/products?q=&includeInactive=`, `POST /api/products`
- `GET`, `PUT`, `DELETE /api/products/{id}` (`DELETE` desactiva)
- `GET`, `POST /api/products/{id}/movements`
- Pendiente (asistente): `POST /api/assistant/messages`,
  `POST /api/assistant/audio`, `POST /api/assistant/actions/{id}/confirm`

## Fase 1 — En paralelo
**daanielxyz: base y migración**
- [x] Inventario (backend): crear, editar, desactivar, movimientos, stock bajo.
- [ ] Pantalla de inventario con búsqueda, filtros e historial.
- [ ] Login simple.
- [ ] Layout y estilo, reutilizando la paleta del demo.

**Nicolás: asistente**
- [ ] Mover el prototipo a `backend/.../assistant`.
- [ ] Herramientas: buscar, crear, modificar, registrar movimiento, desactivar.
      Al inicio, contra una implementación en memoria de `InventoryService`.
- [ ] Ciclo de herramientas con la API de Gemini desde Java.
- [ ] Propuesta → confirmación → ejecución.
- [ ] Envío de audio a Gemini, mostrando lo que entendió.
- [ ] Chat con texto y grabación de audio.
- [ ] Pruebas con las órdenes de ejemplo.

## Fase 2 — Integración
- [ ] Conectar el asistente al servicio real (ambos).
- [ ] Que los cambios del asistente se vean en la tabla y el historial (ambos).
- [ ] Panel de resumen con datos reales (daanielxyz).
- [ ] Pedidos que descuentan stock, clientes y ventas (daanielxyz).

## Fase 3 — Demo
- [ ] Datos de ejemplo creíbles para una floristería de Cali.
- [ ] Guion de la demo y ensayo, incluido el micrófono en celular (necesita HTTPS).
- [ ] Despliegue.
- [ ] Retirar `app/` y el CI de Higgsfield.

## Pendiente de definir
- Fecha de la demo.
- Nombre del producto y de la floristería (el demo usa "FLORE"; el repo, "D'Ore").
- ¿Se manejan lotes y vencimiento de flores?
- ¿Landing animada del demo: se conserva?
- Dónde se despliega.
- Quién paga y administra las claves de IA.

## Estado actual
### daanielxyz
2026-11-26: Fase 0 y backend del inventario listos en la rama
`feat/base-proyecto` (7 pruebas pasan). El frontend solo lista productos.
Siguiente: pantalla de inventario completa.

### Nicolás
Sin iniciar.
