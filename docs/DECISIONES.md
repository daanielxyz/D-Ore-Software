# Decisiones

Se añaden al final. Estados: Propuesta, Aceptada, Reemplazada.

## D-001 · Reconstruir con un stack nuevo — Propuesta (2026-11-26)
Java 21 + Spring Boot (backend), React + Vite + TypeScript (frontend),
PostgreSQL + Flyway.
**Por qué:** el equipo propuso Java y React; el demo depende de la plataforma de
Higgsfield. **Costo:** no se reutiliza el servidor actual, solo diseño y textos.

## D-002 · Un solo backend modular — Propuesta (2026-11-26)
Una aplicación Spring Boot separada por funcionalidad, sin microservicios.

## D-003 · Alcance de la demo — Reemplazada por D-012
Dentro: inventario, asistente, resumen, pedidos, clientes, ventas básicas, login.
Fuera por ahora: contenido, social listening, analítica avanzada y finanzas.

## D-004 · El asistente confirma antes de ejecutar — Aceptada (2026-11-26)
El asistente interpreta la orden, muestra el cambio y el usuario lo confirma con
un toque. **Por qué:** una transcripción errónea puede dañar el inventario.

## D-005 · "Eliminar" desactiva — Propuesta (2026-11-26)
El producto se oculta y conserva su historial.

## D-006 · Interpretación con la API de Claude — Reemplazada por D-008
SDK oficial de Java, Messages API con herramientas y ciclo propio en el backend.
**Por qué:** las herramientas modifican nuestra base y requieren confirmación.
Alternativa descartada por ahora: Managed Agents (Anthropic aloja el ciclo).
Modelo inicial `claude-opus-5-5`, configurable; comparar con modelos más baratos
usando las órdenes de ejemplo.

Notas de implementación:
- No admite `tool_choice` forzado: usar `auto` y herramientas con `strict: true`.
- Definir `effort` de forma explícita.
- Revisar `stop_reason` (incluido `refusal`) antes de ejecutar herramientas.

## D-007 · La voz se transcribe aparte — Reemplazada por D-009
Los modelos actuales de Claude reciben texto e imágenes, no audio
(https://platform.claude.com/docs/en/models/overview). Flujo: audio →
transcripción → mismo camino que el texto. Proveedor de transcripción por elegir
(lo investiga Nicolás), detrás de la interfaz `SpeechToText`.

## D-008 · La demo usa la API de Gemini — Aceptada (2026-11-26)
Capa gratuita para la demo; el proyecto real pasará a un modelo de pago.
- El proveedor queda detrás de una interfaz propia (`AssistantModel`) para
  poder cambiarlo sin tocar el resto.
- Modelo inicial `gemini-3.8-flash`, configurable. SDK de Java
  `com.google.genai`; la documentación usa la API Interactions.
- En la capa gratuita Google usa el contenido para mejorar sus productos y puede
  haber revisión humana: **solo datos ficticios**.
- Fuentes: https://ai.google.dev/gemini-api/docs/pricing y
  https://ai.google.dev/gemini-api/terms

## D-009 · El audio va directo a Gemini — Aceptada (2026-11-26, probado)
Gemini recibe audio, así que no hace falta otro proveedor de transcripción.
El modelo devuelve también el texto que entendió, para mostrarlo en la
confirmación. Formatos admitidos, entre otros: webm, ogg, opus, mp3, wav y m4a
(https://ai.google.dev/gemini-api/docs/audio). Sin probar todavía.

## D-010 · Contrato del inventario — Propuesta (2026-11-26)
Pendiente de la revisión de Nicolás.
- Unidades: tallo, ramo, rollo, paquete, unidad. Cantidades enteras.
- El stock solo cambia con movimientos: entrada, salida, merma o conteo.
- El stock nunca queda negativo; los cambios simultáneos se serializan.
- SKU opcional al crear (se genera) y único sin distinguir mayúsculas.
- El origen (manual o asistente) lo fija el servidor, no el cliente.
- Sin lotes ni vencimientos por ahora. El usuario de cada movimiento se añade
  con el login.
- Los DTOs son los records del paquete `com.dore.inventory`.

## D-011 · Lotes y vencimientos — Aceptada (2026-11-26)
La demo controla vencimiento por lotes. El cliente no lo pidió expresamente; el
equipo cree que lo necesita. Reemplaza el "sin lotes" de D-010.

## D-012 · La demo incluye todas las pantallas — Aceptada (2026-11-26)
Además de inventario, asistente, resumen, pedidos, clientes, ventas y login,
entran contenido, social listening, analítica y finanzas. El inventario y el
asistente siguen siendo la prioridad.

## D-013 · Reglas de lotes — Propuesta (2026-11-26)
- Cada entrada crea un lote, con vencimiento opcional. Vence al terminar ese día
  (hora de Cali).
- Salidas y mermas toman primero lo que vence antes; se puede indicar un lote.
- Una salida no toma flor vencida: se da de baja como merma.
- Un conteo que baja descuenta primero lo que vence antes; si sube, crea un lote.
- El historial guarda un movimiento por operación, sin detalle por lote.

## D-014 · Unidades pliego y bloque — Aceptada (2026-11-26)
Se añaden `SHEET` y `BLOCK` a `Unit`.

## D-015 · Login de la demo — Propuesta (2026-11-26)
- Un solo usuario definido por `APP_USER` y `APP_PASSWORD`; sin clave fija en el
  código. Sesión por cookie. Sin límite de intentos ni roles.
- Todo `/api/**` exige sesión, incluido el asistente.
- Las peticiones que modifican llevan el token CSRF: en el frontend se usa
  siempre `http()` de `frontend/src/http.ts` (Nicolás también).
- Los movimientos aún no registran el usuario: solo hay uno.

## D-016 · Pedidos y descuento de inventario — Aceptada (2026-11-26)
El stock se descuenta al pasar a "En diseño" (o si se salta a un estado
posterior), en una sola operación: si falta algo, no cambia nada.

## D-017 · Resto de reglas de pedidos y ventas — Aceptada (2026-11-26, ver D-018)
- Estados: pendiente → en diseño → listo → en camino → entregado. No se
  devuelven. Cancelar es posible antes de entregar.
- Cancelar un pedido ya descontado no devuelve stock.
- Un pedido no se edita: se cancela y se crea otro.
- La venta del pedido se registra al entregar (aún no hay abonos ni pagos).
- La venta de mostrador no descuenta inventario.

## D-018 · La venta de mostrador descuenta inventario — Aceptada (2026-11-26)
Reemplaza el último punto de D-017. Se eligen productos y salen del stock en la
misma operación. Una venta sin productos (por ejemplo, un domicilio) sigue siendo
posible.

## D-019 · Cómo funciona el asistente — Aceptada (2026-11-26)
Cambia de D-008 el modelo y el uso del SDK.
- Una sola petición REST a `generateContent` por orden, sin SDK. El catálogo va
  en las instrucciones y el modelo responde con llamadas a funciones.
- El backend valida y guarda la propuesta 10 minutos en memoria. Solo
  `confirm` modifica, una vez y todo o nada.
- Modelos en orden: `gemini-3.5-flash-lite`, `gemini-3.1-flash-lite`,
  `gemini-3.8-flash` (`ASSISTANT_GEMINI_MODELS`). Motivo medido ese día: 3.8
  devolvía 503 por saturación y 3.6 tardó 58 s; los lite, cerca de 1 s.
- Texto y audio entran por `POST /api/assistant/messages`.
- El asistente no puede dictar vencimiento al crear un producto.
