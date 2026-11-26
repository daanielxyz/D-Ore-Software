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
- `GET /api/products/{id}/lots`, `GET /api/products/expiring-lots?days=3`
- Pendiente (asistente): `POST /api/assistant/messages`,
  `POST /api/assistant/audio`, `POST /api/assistant/actions/{id}/confirm`

## Fase 1 — En paralelo
**daanielxyz: base y migración**
- [x] Inventario (backend): crear, editar, desactivar, movimientos, stock bajo.
- [x] Pantalla de inventario con búsqueda, filtros, lotes e historial.
- [x] Login simple.
- [x] Layout y estilo, reutilizando la paleta del demo.

**Asistente (lo hace daanielxyz desde el 2026-11-26; Nicolás no pudo continuar)**
- [x] Herramientas, ciclo con Gemini y propuesta → confirmación → ejecución.
- [x] Texto y audio, mostrando lo que entendió.
- [x] Chat con texto, grabación y archivo de audio.
- [x] 28 órdenes de ejemplo por texto: resultado esperado.
- [ ] Probar con voces reales, ruido de taller y celular.

## Fase 2 — Integración
- [x] Asistente conectado al servicio real.
- [x] Los cambios del asistente quedan en la tabla y el historial.
- [x] Resumen con datos reales del inventario (inventario, pedidos y ventas).
- [x] Pedidos que descuentan stock, clientes y ventas (daanielxyz).

## Fase 2b — Resto de pantallas (daanielxyz, después del inventario)
- [ ] Finanzas (gastos y balance).
- [ ] Analítica.
- [ ] Contenido.
- [ ] Social listening (menciones registradas a mano, sin rastreo de redes).

## Fase 3 — Demo
- [x] Datos de ejemplo (`APP_DEMO_DATA=true`).
- [x] Guion (`docs/GUION_DEMO.md`).
- [ ] Ensayo, incluido el micrófono en celular (necesita HTTPS).
- [ ] Despliegue.
- [ ] Retirar `app/` y el CI de Higgsfield.

## Pendiente de definir
- Fecha de la demo.
- Nombre del producto y de la floristería (el demo usa "FLORE"; el repo, "D'Ore").
- ¿Landing animada del demo: se conserva?
- Dónde se despliega.
- Quién paga y administra las claves de IA.

## Estado actual
### daanielxyz
2026-11-26: Fase 0 y backend del inventario listos en la rama
`feat/base-proyecto` (incluye lotes y vencimientos). El frontend solo lista productos.
Pantalla de inventario recorrida en navegador (sin probar fechas de
vencimiento desde la pantalla). Login y resumen listos. Pedidos, clientes y ventas listos (17 pruebas pasan). Venta de mostrador con productos, datos de ejemplo y guion listos. Asistente funcionando con texto y voz.
Siguiente: ensayo con voz real, despliegue, y finanzas y analítica.

### Nicolás
Prototipo en `prototipos/asistente/`; sus órdenes de ejemplo se usan para probar.
Su tarea pasó a daanielxyz el 2026-11-26.
