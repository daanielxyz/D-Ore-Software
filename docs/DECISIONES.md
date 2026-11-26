# Decisiones

Se añaden al final. Estados: Propuesta, Aceptada, Reemplazada.

## D-001 · Reconstruir con un stack nuevo — Propuesta (2026-11-26)
Java 21 + Spring Boot (backend), React + Vite + TypeScript (frontend),
PostgreSQL + Flyway.
**Por qué:** el equipo propuso Java y React; el demo depende de la plataforma de
Higgsfield. **Costo:** no se reutiliza el servidor actual, solo diseño y textos.

## D-002 · Un solo backend modular — Propuesta (2026-11-26)
Una aplicación Spring Boot separada por funcionalidad, sin microservicios.

## D-003 · Alcance de la demo — Propuesta (2026-11-26)
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

## D-009 · El audio va directo a Gemini — Aceptada (2026-11-26)
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
