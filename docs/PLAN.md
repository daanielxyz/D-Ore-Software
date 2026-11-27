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
- Asistente: `POST /api/assistant/messages` (texto o audio),
  `POST /api/assistant/actions/{id}/confirm`, `DELETE /api/assistant/actions/{id}`
- Resto: `/api/finance`, `/api/analytics?days=`, `/api/content`, `/api/mentions`

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
- [x] Finanzas (gastos y balance).
- [x] Analítica.
- [x] Contenido.
- [x] Social listening (menciones registradas a mano, sin rastreo de redes).

## Fase 3 — Demo
- [x] Datos de ejemplo (`APP_DEMO_DATA=true`).
- [x] Guion (`docs/GUION_DEMO.md`).
- [ ] Ensayo, incluido el micrófono en celular (necesita HTTPS).
- [x] Despliegue: https://dore-0z6o.onrender.com (cada cambio en `main` lo vuelve a publicar).
- [ ] Retirar `app/` y el CI de Higgsfield.

## Fase 4 — Rediseño y entrega (2026-11-27, antes de las 4 p. m.)
Horario propuesto, con margen:
- [x] Hasta 12:45: paleta a partir del logo, mostrada en una pantalla para aprobar.
- [x] Hasta 13:45: aplicarla a todas las pantallas y al login, con logo y favicon;
      revisar en computador y ancho de celular; `npm run build` y `npm run lint`.
- [ ] Hasta 14:15: commit, publicar la rama, unir a `main` y comprobar el enlace.
- [ ] Hasta 15:15: ensayo en local y grabación.
- [ ] Antes de 15:45: enviar video, enlace, usuario y contraseña.

Solo cambios visuales: sin tocar funciones, backend ni base de datos. Si a las
14:15 no está listo, se publica lo que esté estable o se graba con lo ya publicado.

## Pendiente de definir
- ¿Landing animada del demo: se conserva?
- Quién paga y administra las claves de IA.
- Plan de Render: si se cambió a pago en el panel, ajustar `render.yaml`.
- D-020 y D-021 siguen como propuestas.

## Estado actual
### daanielxyz
2026-11-26: Fase 0 y backend del inventario listos en la rama
`feat/base-proyecto` (incluye lotes y vencimientos). El frontend solo lista productos.
Pantalla de inventario recorrida en navegador (sin probar fechas de
vencimiento desde la pantalla). Login y resumen listos. Pedidos, clientes y ventas listos (17 pruebas pasan). Venta de mostrador con productos, datos de ejemplo y guion listos. Asistente funcionando con texto y voz.
El asistente funcionó en el equipo de daanielxyz.
Finanzas, analítica, contenido y social listening listos en `feat/pantallas`
(27 pruebas pasan; recorridos en navegador, ancho de celular simulado).
Publicado el 2026-11-26: la página responde, usa HTTPS y la API pide sesión
(sin verificar por el agente: contenido tras entrar y asistente).
2026-11-27: se decidió rediseñar con los colores del logo antes de grabar; aún
sin empezar. Documentos listos para seguir en un chat nuevo, en la rama
`docs/despliegue` (sin publicar).
Siguiente: Fase 4.
2026-11-27, 13:45: rediseño con el manual de marca (D-024) en `feat/rediseno`.
Build, lint y paso de frontend de Docker correctos. Pantallas y login recorridos
en navegador con datos de ejemplo, en computador y ancho de celular simulado.
Sin publicar y sin probar en un teléfono real.
2026-11-27, 14:20: segundo rediseño (D-026) y videos del login (D-027) en
`feat/rediseno-ui`, aprobado por daanielxyz. Pantallas revisadas a 1280, 1024 y
390 px simulados. Siguiente: publicar, grabar y enviar.

### Nicolás
Prototipo en `prototipos/asistente/`; sus órdenes de ejemplo se usan para probar.
Su tarea pasó a daanielxyz el 2026-11-26.
