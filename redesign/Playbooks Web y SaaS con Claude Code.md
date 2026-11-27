# Playbooks de proyectos: Web y SaaS con Claude Code

Actualizado: 28 de octubre de 2026

## Cómo usar este documento

Cada proyecto arranca igual: un kit pequeño de skills dentro del proyecto, un prompt inicial que solo hace descubrimiento y un `docs/PLAN.md` que manda en todas las sesiones siguientes. El problema de "tengo demasiadas skills" se resuelve cargando solo el kit de cada proyecto, no las 94.

**Por qué importa:** el agente lee el nombre y la descripción de todas las skills instaladas al inicio de cada conversación. Con 94 activas gasta contexto y activa skills que no tocan, por ejemplo las de auditoría de seguridad que dicen "auto-invoked".

### Configuración única

1. Mueve tus 94 skills a una carpeta biblioteca fuera de las rutas que el agente lee, por ejemplo `~/skills-biblioteca/`.
2. Deja como globales (`~/.claude/skills/`) solo las de flujo de código que sirven en cualquier proyecto: `lean-build`, `verify-and-stop`, `investigate-first`, `surgical-patch`, `safe-refactor`.
3. En cada proyecto nuevo, copia el kit del playbook a `.claude/skills/` (Claude Code) y a `.agents/skills/` (Antigravity). Cada playbook trae su comando.

```bash
# Plantilla: reemplaza la lista KIT por la del playbook
KIT="skill-1 skill-2 skill-3"
mkdir -p .claude/skills .agents/skills
for s in $KIT; do
  cp -r ~/skills-biblioteca/$s .claude/skills/
  cp -r ~/skills-biblioteca/$s .agents/skills/
done
```

### Flujo de cada proyecto

1. Crea la carpeta del proyecto, inicia git y copia el kit de skills.
2. Pon los insumos en el proyecto: `assets/` (logo, fotos, menú o catálogo) y, en el playbook 1.2, `referencia/` con las capturas.
3. Abre Claude Code en modo plan y pega el prompt inicial del playbook, llenando los `[CORCHETES]`.
4. Responde la ronda de preguntas. El agente propone stack y escribe `docs/PLAN.md`.
5. Revisa el plan y corrige hasta escribir "aprobado". Solo entonces el agente te dice cuál es la Fase 1.
6. Ejecuta fase por fase con el prompt de continuación de abajo. Haz commit al cerrar cada fase.

### Prompt de continuación (todas las fases, todos los proyectos)

Este prompt es igual en Claude Code y en Antigravity, porque todo el estado vive en `docs/PLAN.md`. Así puedes cambiar de herramienta a mitad de proyecto cuando se te acaben los tokens.

```text
Lee docs/PLAN.md completo. Identifica la siguiente fase sin completar.

Antes de escribir código, dime en máximo 6 líneas:
- qué fase vas a ejecutar y qué vas a construir
- qué skills vas a usar (solo las que el plan asigna a esta fase)
- el criterio de aceptación con el que vas a demostrar que terminó
Espera mi OK.

Durante la fase: no salgas del alcance de la fase. Si descubres algo que cambia el plan, detente y pregúntame antes de seguir.

Al terminar:
1. Verifica el criterio de aceptación con la skill verify-and-stop.
2. Marca los checkboxes cumplidos en docs/PLAN.md.
3. Agrega una entrada al Registro de progreso: fecha, qué se hizo, archivos clave tocados, pendientes y decisiones nuevas.
4. Detente y dime cuál es la siguiente fase. No la empieces.
```

## Estructura de carpetas

La idea de separar contexto, orquestación y especificaciones sirve para los dos tipos de proyecto. Lo que cambia es el tamaño: una web de restaurante necesita tres archivos y un SaaS necesita carpetas. Las carpetas de agentes y roles solo valen la pena en SaaS, y como revisores, no como un archivo por cada rol.

**Cómo queda cada idea del TikTok**

| Idea original | Dónde queda aquí | Por qué |
| --- | --- | --- |
| Contexto (reglas del negocio, qué sí y qué no) | `docs/contexto.md` en webs, `docs/contexto/` en SaaS | Es lo más valioso de la idea: evita que el agente invente reglas |
| Orquestación (`orchestrator.md`) | `docs/PLAN.md` + `AGENTS.md` | El plan ya dirige el flujo fase por fase. `AGENTS.md` es el archivo que el agente lee solo al abrir el proyecto |
| Especificaciones (paso a paso) | Fases de `docs/PLAN.md` en webs, `docs/specs/` en SaaS | En un SaaS cada función merece su spec; en una web el plan alcanza |
| Agentes y roles (arquitecto, desarrollador, diseñador) | Las skills del kit | El agente principal ya hace esos roles cargando la skill correcta. Un archivo de rol que nadie invoca no se lee |
| Revisores de código | `.claude/agents/` en SaaS | Ahí sí sirve: un subagente revisa con contexto limpio y no califica su propio trabajo |

**Por qué `AGENTS.md`:** Antigravity lee `AGENTS.md` automáticamente y Claude Code lee `CLAUDE.md`. Escribe las reglas una sola vez en `AGENTS.md` y deja en `CLAUDE.md` una sola línea, `@AGENTS.md`, que lo importa. Así las dos herramientas siguen las mismas reglas.

### Webs (playbooks 1.1 y 1.2)

```text
mi-web/
├── AGENTS.md            ← reglas del proyecto, máximo ~60 líneas
├── CLAUDE.md            ← una línea: @AGENTS.md
├── docs/
│   ├── PLAN.md          ← orquestación: fases, requisitos de calidad, estado y registro
│   └── contexto.md      ← negocio: qué sí, qué no, tono, horarios, sedes, precios fijos
├── assets/              ← logo, fotos, menú o catálogo del cliente
├── referencia/          ← solo en el playbook 1.2
├── .claude/skills/      ← kit para Claude Code
├── .agents/skills/      ← kit para Antigravity
└── (código del stack elegido)
```

### SaaS (playbook 2)

```text
mi-saas/
├── AGENTS.md
├── CLAUDE.md                     ← @AGENTS.md
├── docs/
│   ├── PLAN.md                   ← orquestación
│   ├── contexto/
│   │   ├── negocio.md            ← reglas de negocio y glosario
│   │   ├── fuera-de-alcance.md   ← lo que NO se construye
│   │   └── decisiones.md         ← decisiones técnicas, una entrada corta por decisión
│   ├── specs/
│   │   ├── 001-auth-y-tenants.md
│   │   └── 002-...md             ← una spec por fase o función
│   └── insumos/                  ← tus notas y bocetos iniciales
├── .claude/
│   ├── skills/
│   └── agents/
│       ├── revisor-codigo.md
│       └── revisor-seguridad.md
├── .agents/skills/
└── (código)
```

Los subagentes de `.claude/agents/` son de Claude Code. En Antigravity, pide la revisión en una conversación nueva y pégale el contenido del archivo del revisor.

**Qué va en `AGENTS.md`:** qué es el proyecto en un párrafo, el stack, los comandos (dev, test, build), las reglas de código y esta instrucción: "antes de cada tarea lee `docs/PLAN.md` y el contexto". Mantenlo corto, porque se carga en cada conversación. Los prompts iniciales ya le piden al agente crear todos estos archivos.

## Mapa de skills

De tus 94 skills, unas 35 sirven para estos proyectos. Las demás son de seguridad corporativa, nube o ahorro de tokens. La regla principal: **una sola skill de dirección visual por proyecto**, porque varias se contradicen. Por ejemplo, `high-end-visual-design` exige pastillas sobre cada título e `image-to-code` las prohíbe.

| Skill | Para qué sirve | 1.1 Desde cero | 1.2 Referencia | 2 SaaS |
| --- | --- | --- | --- | --- |
| `design-taste-frontend` | Dirección visual de landings: "Design Read", diales de variación y densidad, reglas anti-plantilla | Dirección | No | Solo landing |
| `impeccable` | Sistema completo de diseño: `init` (PRODUCT.md), `shape`, `critique`, `audit`, `polish`, `harden`, `adapt` | Calidad final | Solo `audit` y `adapt` | Dirección de la app |
| `image-to-code` | Analizar una imagen como especificación y construirla sin desviarse | No | Núcleo | No |
| `minimalist-ui` | Preset editorial cálido y plano; bueno para catálogos de ropa | Opcional | No | No |
| `high-end-visual-design` | Preset "agencia premium": cristal, sombras suaves, motion físico | Opcional | No | No |
| `industrial-brutalist-ui` | Preset brutalista tipo plano técnico | Raro | No | Raro |
| `mobile-native` | Arreglos CSS y meta para que la web se sienta nativa en el celular | Sí | Sí | Sí |
| `break-ui` | Probar la UI con datos extremos: nombres largos, cero productos, precios enormes | Sí | Sí | Sí |
| `animate` | Construir una animación tomando las decisiones en orden | Sí | Sí | Sí |
| `emil-design-eng` | Criterio de pulido de componentes y detalles finos | Sí | No | Sí |
| `review-animations` | Revisar motion ya hecho con criterio estricto | Opcional | Opcional | Opcional |
| `find-animation-opportunities` | Proponer dónde agregar motion (no implementa) | Opcional | No | Opcional |
| `apple-design` | Gestos, springs, hojas y materiales estilo Apple | Opcional | No | Opcional |
| `pick-ui-library` | Elegir librería para toasts, tablas, gráficos, drag and drop; solo con invocación manual | No | No | Sí |
| `prototype` | Varias versiones distintas de un componente con selector visual; solo manual | Opcional | No | Sí |
| `lean-build` | Construir cada fase sin sobre-ingeniería y con condición de parada | Sí | Sí | Sí |
| `verify-and-stop` | Demostrar que se cumple el criterio de aceptación y parar | Sí | Sí | Sí |
| `investigate-first` | Diagnosticar fallos raros antes de tocar código | Si hay bugs | Si hay bugs | Sí |
| `surgical-patch` | Arreglar bugs en la capa mínima | Si hay bugs | Si hay bugs | Sí |
| `safe-refactor` | Reestructurar sin cambiar comportamiento | No | No | Sí |
| `migration` | Cambios de esquema de base de datos reversibles | No | No | Sí |
| `threat-modeling` | Modelo de amenazas STRIDE sobre la arquitectura | No | No | Sí |
| `rbac-design` | Diseñar roles y permisos (y multi-tenant) | No | No | Sí |
| `owasp-top-10-web` | Revisión de seguridad web antes de lanzar | No | No | Sí |
| `api-security` | Revisión de la API (OWASP API Top 10) | No | No | Sí |
| `secure-code-review` | Revisión de código de auth, permisos e inputs | No | No | Sí |
| `secrets-management` | Manejo de `.env`, claves y rotación | No | No | Sí |
| `dependency-scanning` | Vulnerabilidades y licencias de dependencias | No | No | Sí |
| `llm-top-10`, `prompt-injection` | Seguridad de funciones con IA | No | No | Si tiene IA |
| `pci-dss-review` | Cumplimiento de pagos con tarjeta | No | No | Solo si guardas tarjetas tú mismo |
| `imagegen-frontend-web` | Generar imágenes de referencia por sección | Solo en Antigravity | No | No |
| `brandkit` | Generar tableros de identidad de marca | Solo en Antigravity, si el cliente no tiene marca | Igual | Igual |
| `caveman`, `caveman-compress` | Respuestas comprimidas y CLAUDE.md comprimido para ahorrar tokens | Opcional | Opcional | Opcional |

**No las cargues en estos proyectos:** las revisiones de nube (`aws-review`, `azure-review`, `gcp-review`, `iac-security`, `container-security`), las de operación de seguridad (`alert-triage`, `siem-rules`, `detection-engineering`, `ir-playbook`, `forensics-checklist`, `containment`, `log-analysis`, `post-incident-review`), las de cumplimiento (`soc2-gap`, `iso27001-gap`, `hipaa-review`, `nist-csf-assessment`), las de red e identidad corporativa, las de Caveman Cloud (`caveman-setup`, `caveman-discover`, `caveman-manage`, `caveman-optimize`, `caveman-evidence-review`) y `animate-expo` (es para apps React Native).

**Dos skills que no funcionan como esperas:**

- `grill-me` solo llama a una skill llamada `grilling` que no está en tu zip. Si la invocas no hace nada útil. Los prompts iniciales ya hacen la ronda de preguntas, así que no la necesitas.
- `image-to-code` está escrita para Codex y exige que el agente genere sus propias imágenes primero. Claude Code no genera imágenes, y en tu caso la imagen ya existe. El prompt 1.2 le indica usar tus capturas como fuente de verdad y aplicar solo sus reglas de análisis y fidelidad.

## Playbook 1.1: Página web desde cero

Para menús digitales de restaurantes y catálogos de tiendas de ropa. La dirección visual la pone `design-taste-frontend`. `impeccable` entra al final solo para revisar y pulir, no para rediseñar.

**Kit de skills**

```bash
KIT="design-taste-frontend impeccable mobile-native break-ui animate emil-design-eng lean-build verify-and-stop"
# Opcional, agrega UNO solo si el descubrimiento lo pide: minimalist-ui o high-end-visual-design
```

**Antes de pegar el prompt:** deja en `assets/` lo que tengas del cliente: logo, fotos, el menú o catálogo (PDF, Excel o fotos) y colores o fuentes si existen.

**Prompt inicial**

```text
Vamos a construir un sitio web desde cero. Esta sesión es SOLO de descubrimiento y planeación: no escribas código de la aplicación hasta que yo apruebe el plan.

## Contexto
- Tipo de negocio: [restaurante | tienda de ropa | otro]
- Marca: [nombre] — ciudad/país: [ ]
- Objetivo principal del sitio: [ej. ver el menú y pedir por WhatsApp | ver el catálogo y comprar]
- Lo que ya tengo (en /assets): [logo, fotos, menú/catálogo, colores, fuentes, dominio]
- Estilo o referencias que me gustan (opcional): [ ]
- Idioma(s) del sitio: [ ]
- Restricciones: [fecha de entrega, presupuesto de hosting, quién actualizará el contenido]

## Skills de este proyecto
- design-taste-frontend: dirección visual. Haz tu "Design Read" y define los diales.
- impeccable: solo sus comandos critique, audit, polish y adapt, en las fases finales. No uses su flujo de diseño nuevo.
- mobile-native y break-ui: en implementación y QA.
- animate y emil-design-eng: motion y pulido de componentes.
- lean-build para construir cada fase y verify-and-stop para cerrarla.
No uses otras skills de diseño aunque parezcan relevantes. Si crees que otra aporta, propónla y espera mi OK.

## Proceso
1. Revisa la carpeta del proyecto y haz inventario de /assets: qué hay, en qué formato, qué calidad tiene y qué falta.
2. Hazme UNA ronda de preguntas (máximo 12), agrupadas por tema, cada una con tu respuesta recomendada para que yo solo confirme o corrija. Cubre:
   - Audiencia y contexto de uso (¿desde el celular? ¿desde un QR en la mesa?).
   - Contenido: cuántos productos o platos, categorías, variantes, precios, fotos disponibles.
   - Quién y con qué frecuencia actualizará el contenido, y si lo hace alguien no técnico.
   - Conversión: WhatsApp, reservas, pedidos, pasarela de pago, apps de domicilio, Google Maps.
   - SEO local, multidioma, analítica.
   - Hosting y dominio.
   [SI ES RESTAURANTE, cubre además: menú por QR, alérgenos o etiquetas (vegano, picante), horarios, platos agotados, menú del día, varias sedes.]
   [SI ES TIENDA DE ROPA, cubre además: tallas, colores, guía de tallas, stock, filtros, lookbook o colecciones, si es catálogo con pedido por WhatsApp o tienda con carrito y pago.]
3. Con mis respuestas, propón el stack. Compara 2 o 3 opciones en una tabla: rendimiento, costo mensual de hosting, facilidad para que el cliente actualice el contenido, complejidad y tiempo de desarrollo. Recomienda una y explica por qué encaja con ESTE proyecto.
4. Haz tu Design Read de design-taste-frontend y propón la dirección visual: diales, paleta, tipografía y, si aplica, UN preset de estilo (minimalist-ui o high-end-visual-design) con su justificación.
5. Crea la estructura de archivos del proyecto: docs/contexto.md (reglas del negocio: qué sí, qué no, tono de comunicación, datos fijos como horarios, sedes y medios de pago), docs/PLAN.md con la estructura de abajo, AGENTS.md (máximo 60 líneas: qué es el proyecto, stack, comandos, reglas de código y la instrucción "antes de cada tarea lee docs/PLAN.md y docs/contexto.md") y CLAUDE.md con una sola línea: @AGENTS.md.
6. Muéstrame un resumen del plan y la lista de supuestos que tomaste. Pregúntame si lo apruebo. Corrige hasta que yo escriba "aprobado". Solo entonces dime cuál es la Fase 1 y espera mi orden para empezar.

## Requisitos de calidad (obligatorios)
Ponlos como checkboxes dentro de la fase del plan que les corresponde. Ninguna fase se cierra sin verificar los suyos.
- Móvil: áreas táctiles de mínimo 44×44 px; probado a 360, 390, 768, 1024 y 1440 px; secciones de altura completa con svh o dvh, nunca vh; elementos fijos (carrito, botón de WhatsApp) con env(safe-area-inset-bottom) y viewport-fit=cover; inputs de 16 px o más.
- Rendimiento: skeletons mientras cargan las imágenes; espacio reservado para cada imagen (width/height o aspect-ratio); imágenes WebP o AVIF comprimidas, con srcset y sizes; lazy loading en todas menos la principal; máximo 2 familias tipográficas con font-display: swap; Lighthouse móvil 90 o más, LCP menor a 2,5 s, CLS menor a 0,1, INP menor a 200 ms.
- Motion: reveal sutil al hacer scroll, solo con transform y opacity; todo el motion respeta prefers-reduced-motion.
- Accesibilidad: alt descriptivo (alt="" en imágenes decorativas); contraste AA; lang="es"; foco visible y navegación con teclado.
- SEO: title y meta description únicos por página; un solo H1 por página y URL canónica; Open Graph y Twitter card con foto real de 1200×630 px; favicon, apple-touch-icon y manifest; datos estructurados schema.org (Restaurant y Menu, o Store y Product) con dirección, horario y teléfono; sitemap.xml y robots.txt; sin noindex en producción y con noindex en el entorno de pruebas.
- Páginas: 404 personalizada con enlace al inicio; página de gracias si hay formulario, pedido o reserva; política de tratamiento de datos si se recogen datos personales, y aviso de cookies si la analítica las usa.
- Conversión: botón de WhatsApp con mensaje prellenado, clic para llamar y enlace a Google Maps; analítica con eventos de clic en WhatsApp, reserva y pedido.
- Al final del plan, como tareas mías (no las puedes hacer tú): dominio con HTTPS y una sola versión (con o sin www); en Google Search Console verificar el dominio, enviar el sitemap y solicitar indexación de las páginas principales; crear o actualizar el perfil de empresa en Google; probar en un iPhone y un Android reales; revisar la vista previa del link en WhatsApp; imprimir y probar el QR del menú.

## Estructura de docs/PLAN.md
1. Resumen: negocio, objetivo, audiencia, acción de conversión principal.
2. Decisiones y supuestos, con fecha.
3. Stack elegido, por qué, y alternativas descartadas.
4. Dirección de diseño: Design Read, diales, paleta con valores, tipografía, preset de estilo, qué evitar.
5. Mapa del sitio y secciones de cada página.
6. Modelo de contenido: estructura de cada plato o producto (campos), de dónde sale y cómo se actualiza.
7. Fases con checkboxes. Cada fase lleva: objetivo, tareas, skills a usar y criterio de aceptación verificable (por ejemplo "Lighthouse móvil ≥ 90" o "se ve bien a 390 px y 1440 px").
8. Riesgos y preguntas abiertas.
9. Registro de progreso (vacío; se llena al cerrar cada fase).

## Reglas del plan
- Fases pequeñas que quepan en una sola sesión.
- Orden sugerido: base del proyecto y design tokens → layout y navegación → secciones → contenido real → motion → QA con break-ui y mobile-native → critique, audit y polish con impeccable → SEO, rendimiento y accesibilidad → deploy.
- Nada de contenido inventado en producción: si falta un dato real, márcalo como pendiente en el plan.
- Cada fase lista sus requisitos de calidad como checkboxes y los verifica antes de cerrarse.
```

## Playbook 1.2: Página web desde una referencia

Aquí la referencia manda: estructura, espaciado, escala tipográfica y forma de los componentes se copian 1:1. Solo cambian los elementos de marca. Por eso este kit **no** lleva skills de dirección visual: `design-taste-frontend` o `high-end-visual-design` empujarían al agente a "mejorar" la referencia.

**Kit de skills**

```bash
KIT="image-to-code mobile-native break-ui animate impeccable lean-build verify-and-stop"
```

**Antes de pegar el prompt**

- Guarda capturas de página completa en `referencia/`, una por sección si la página es larga. Nombres ordenados: `01-hero-desktop.png`, `01-hero-mobile.png`, `02-menu-desktop.png`…
- Si puedes, toma desktop a 1440 px y móvil a 390 px de ancho. Sin capturas móviles, el agente infiere el responsive y lo marca como supuesto.
- Si la referencia tiene animaciones importantes, descríbelas en una línea o graba un video corto.
- En `assets/` va la identidad de la marca: logo, colores, fuentes, fotos y textos reales.

**Prompt inicial**

```text
Vamos a construir un sitio web replicando 1:1 una referencia visual y cambiándola a la identidad de otra marca. Esta sesión es SOLO de descubrimiento y planeación: no escribas código de la aplicación hasta que yo apruebe el plan.

## Contexto
- Referencia: capturas en /referencia (URL original, si sirve: [ ])
- Marca destino: [nombre] — tipo de negocio: [restaurante | tienda de ropa | otro]
- Identidad de la marca (en /assets): [logo, colores, fuentes, fotos, textos]
- Objetivo principal del sitio: [ ]
- Secciones de la referencia que NO quiero o que quiero agregar: [ ]
- Restricciones: [fecha, hosting, quién actualizará el contenido]

## Qué se copia y qué cambia
Se copia 1:1: layout y grilla, orden de secciones, espaciados y ritmo vertical, escala y jerarquía tipográfica, forma de botones, tarjetas y bordes, tratamiento de imágenes, comportamiento responsive y motion.
Cambia a la marca: colores, fuentes, logo, fotos, íconos propios y todo el texto.
Nunca reutilices imágenes, textos, logos ni íconos propios de la referencia: son de su dueño.

## Cómo usar image-to-code en este proyecto
La skill image-to-code fue escrita para generar sus propias imágenes primero. En este proyecto NO generes imágenes: las capturas de /referencia son la fuente de verdad y reemplazan ese paso. Aplica sus reglas de análisis profundo (secciones 8 y 9), extracción de texto, tipografía, espaciado, componentes y color (21 a 25), disciplina de copia y anti-drift (26 a 28). Ignora sus reglas de generación de imágenes y su motor de variación creativa (sección 12): aquí no se inventa dirección, se replica.

## Otras skills
- mobile-native y break-ui: QA en móvil y con datos extremos.
- animate: solo para replicar el motion de la referencia, no para agregar nuevo.
- impeccable: solo sus comandos audit y adapt al final. No uses polish, bolder ni ningún comando que cambie el diseño.
- lean-build para construir cada fase y verify-and-stop para cerrarla.
No uses otras skills de diseño.

## Proceso
1. Analiza cada captura de /referencia con el estándar de image-to-code. Para cada sección anota: estructura y grilla, jerarquía tipográfica (tamaños relativos, pesos, interlineado), espaciados aproximados en px, componentes, paleta con su rol y lo que no se ve claro.
2. Haz inventario de /assets de la marca: qué hay y qué falta.
3. Hazme UNA ronda de preguntas (máximo 10), cada una con tu respuesta recomendada. Cubre solo lo que las capturas y los assets no resuelven: partes ambiguas de la referencia, responsive faltante, contenido real (cantidad de platos o productos, categorías, precios), conversión (WhatsApp, reservas, carrito), quién actualiza el contenido, hosting y dominio.
4. Propón el stack: 2 o 3 opciones en una tabla (rendimiento, costo de hosting, facilidad de actualización del contenido, complejidad). Recomienda una y explica por qué.
5. Construye la Tabla de mapeo de marca: para cada rol (fondo, superficie, texto principal, texto secundario, acento, CTA, bordes, fuente de títulos, fuente de cuerpo, estilo de fotos) pon el valor de la referencia y el valor de la marca. Si una fuente de la marca tiene métricas muy distintas, propón cómo ajustar tamaños para conservar la misma línea y el mismo peso visual. Si el contraste de los colores de la marca falla en algún rol, indícalo y propón el ajuste mínimo.
6. Crea la estructura de archivos del proyecto: docs/contexto.md (reglas del negocio: qué sí, qué no, tono de comunicación, datos fijos como horarios, sedes y medios de pago), docs/PLAN.md con la estructura de abajo, AGENTS.md (máximo 60 líneas: qué es el proyecto, stack, comandos, reglas de código y la instrucción "antes de cada tarea lee docs/PLAN.md y docs/contexto.md") y CLAUDE.md con una sola línea: @AGENTS.md.
7. Muéstrame un resumen, la tabla de mapeo y la lista de supuestos. Pregúntame si apruebo. Corrige hasta que yo escriba "aprobado". Solo entonces dime cuál es la Fase 1 y espera mi orden para empezar.

## Requisitos de calidad (obligatorios)
Ponlos como checkboxes dentro de la fase del plan que les corresponde. Ninguna fase se cierra sin verificar los suyos.
- Móvil: áreas táctiles de mínimo 44×44 px; probado a 360, 390, 768, 1024 y 1440 px; secciones de altura completa con svh o dvh, nunca vh; elementos fijos (carrito, botón de WhatsApp) con env(safe-area-inset-bottom) y viewport-fit=cover; inputs de 16 px o más.
- Rendimiento: skeletons mientras cargan las imágenes; espacio reservado para cada imagen (width/height o aspect-ratio); imágenes WebP o AVIF comprimidas, con srcset y sizes; lazy loading en todas menos la principal; máximo 2 familias tipográficas con font-display: swap; Lighthouse móvil 90 o más, LCP menor a 2,5 s, CLS menor a 0,1, INP menor a 200 ms.
- Motion: reveal sutil al hacer scroll, solo con transform y opacity; todo el motion respeta prefers-reduced-motion.
- Accesibilidad: alt descriptivo (alt="" en imágenes decorativas); contraste AA; lang="es"; foco visible y navegación con teclado.
- SEO: title y meta description únicos por página; un solo H1 por página y URL canónica; Open Graph y Twitter card con foto real de 1200×630 px; favicon, apple-touch-icon y manifest; datos estructurados schema.org (Restaurant y Menu, o Store y Product) con dirección, horario y teléfono; sitemap.xml y robots.txt; sin noindex en producción y con noindex en el entorno de pruebas.
- Páginas: 404 personalizada con enlace al inicio; página de gracias si hay formulario, pedido o reserva; política de tratamiento de datos si se recogen datos personales, y aviso de cookies si la analítica las usa.
- Conversión: botón de WhatsApp con mensaje prellenado, clic para llamar y enlace a Google Maps; analítica con eventos de clic en WhatsApp, reserva y pedido.
- Al final del plan, como tareas mías (no las puedes hacer tú): dominio con HTTPS y una sola versión (con o sin www); en Google Search Console verificar el dominio, enviar el sitemap y solicitar indexación de las páginas principales; crear o actualizar el perfil de empresa en Google; probar en un iPhone y un Android reales; revisar la vista previa del link en WhatsApp; imprimir y probar el QR del menú.

## Estructura de docs/PLAN.md
1. Resumen: marca, objetivo, referencia usada.
2. Decisiones y supuestos, con fecha.
3. Stack elegido, por qué, y alternativas descartadas.
4. Especificación extraída de la referencia, sección por sección, enlazando cada captura.
5. Tabla de mapeo de marca.
6. Design tokens finales (colores, tipografía, espaciado, radios, sombras) ya con la marca aplicada.
7. Modelo de contenido y cómo se actualiza.
8. Fases con checkboxes. Cada fase lleva: objetivo, tareas, skills a usar y criterio de aceptación verificable.
9. Riesgos y preguntas abiertas.
10. Registro de progreso (vacío).

## Reglas del plan
- Primera fase: base del proyecto y design tokens. Después, una fase por sección o por grupo pequeño de secciones, en el orden de la referencia.
- Criterio de aceptación de cada sección: captura de la implementación con Playwright al mismo ancho que la referencia, puesta lado a lado con la original, y lista de diferencias. Solo se aceptan las diferencias que vienen de la tabla de mapeo de marca.
- Fases finales: responsive y móvil (mobile-native), datos extremos (break-ui), audit y adapt de impeccable, SEO y rendimiento, deploy.
- Cada fase lista sus requisitos de calidad como checkboxes y los verifica antes de cerrarse.
```

## Requisitos de calidad web

Los requisitos de calidad ya están dentro de los prompts 1.1 y 1.2 (y el prompt 2 tiene su propia versión para la app y la landing), en la sección "Requisitos de calidad (obligatorios)". No necesitas guardar ningún archivo aparte: el agente los pone como checkboxes en la fase que corresponde de `docs/PLAN.md`. Esta sección explica de dónde salen.

**Ajustes a lo que viste:**

- **44×44 px** es la recomendación de Apple y un buen estándar. El mínimo de accesibilidad (WCAG 2.2 AA) es 24×24 px, así que 44 te deja con margen.
- **`dvh` vs `svh`:** `dvh` cambia de tamaño cuando la barra de Safari aparece o se esconde, y eso puede hacer saltar el hero. Para secciones de pantalla completa, `svh` es más estable. Lo importante es no usar `vh`.
- **Enviar el sitemap y pedir indexación** lo haces tú en Google Search Console después del deploy. El agente no puede hacerlo. Por eso queda al final del plan como tarea tuya.

**Lo que faltaba y agregué:** perfil de empresa en Google (para un restaurante pesa más que todo el SEO de la página), datos estructurados de schema.org, métricas Core Web Vitals, medición de clics en WhatsApp, inputs de 16 px para que iOS no haga zoom, y la política de tratamiento de datos si recoges datos personales (en Colombia lo exige la Ley 1581).

Los pasos posteriores al deploy (Search Console, perfil de empresa en Google, pruebas en teléfonos reales, QR impreso) quedan al final de cada plan como tareas tuyas, porque el agente no puede hacerlos.

## Playbook 2: SaaS mediano

Un SaaS tiene dos superficies con reglas distintas: la app, donde el usuario completa tareas, y la landing, donde se convence. `impeccable` dirige la app y `design-taste-frontend` solo la landing. La seguridad entra desde el diseño con `threat-modeling` y `rbac-design`, no al final.

**Kit de skills**

```bash
KIT="impeccable design-taste-frontend pick-ui-library prototype mobile-native break-ui animate emil-design-eng lean-build verify-and-stop investigate-first surgical-patch safe-refactor migration threat-modeling rbac-design owasp-top-10-web api-security secure-code-review secrets-management dependency-scanning"
# Si el producto tiene funciones con IA, agrega: llm-top-10 prompt-injection
```

**Antes de pegar el prompt:** si tienes notas, bocetos, competidores o un documento de la idea, déjalos en `docs/insumos/`.

**Prompt inicial**

```text
Vamos a construir un SaaS. Esta sesión es SOLO de descubrimiento y planeación: no escribas código de la aplicación hasta que yo apruebe el plan.

## Contexto
- Idea en una frase: [ ]
- Problema que resuelve y para quién: [ ]
- Cómo lo resuelven hoy (competidores o alternativas): [ ]
- Modelo de negocio: [suscripción por usuario | por empresa | por uso | freemium]
- Mercado y país principal: [ ]
- Lo que ya tengo (en /docs/insumos): [notas, bocetos, marca, dominio]
- Restricciones: [fecha objetivo para el MVP, presupuesto mensual de infraestructura, tamaño del equipo]

## Skills de este proyecto
- impeccable: dirección de diseño de la app (modo Operate). Usa init para PRODUCT.md y shape antes de cada pantalla importante.
- design-taste-frontend: solo para la landing de marketing.
- pick-ui-library y prototype: elegir librerías y explorar variantes de componentes clave.
- threat-modeling y rbac-design: en la fase de diseño de arquitectura.
- lean-build para cada fase, verify-and-stop para cerrarla; investigate-first y surgical-patch para bugs; migration para cambios de esquema; safe-refactor para reestructurar.
- owasp-top-10-web, api-security, secure-code-review, secrets-management y dependency-scanning: en las fases de revisión de seguridad.
No uses otras skills aunque parezcan relevantes. Si crees que otra aporta, propónla y espera mi OK.

## Proceso
1. Revisa la carpeta del proyecto y /docs/insumos.
2. Ronda 1, producto (máximo 12 preguntas, cada una con tu respuesta recomendada). Cubre: tipos de usuario y roles, si es multi-empresa (multi-tenant), el flujo principal que el usuario hace cada día, las 3 a 5 funciones del MVP y lo que queda explícitamente fuera, planes y precios, onboarding, notificaciones (correo, WhatsApp), integraciones, funciones con IA, datos personales que se guardan y obligaciones legales del país (por ejemplo protección de datos).
3. Ronda 2, técnica (máximo 8 preguntas). Solo lo que la ronda 1 dejó abierto: volumen esperado de usuarios y datos, tiempo real, archivos, búsquedas, reportes, app móvil futura, preferencias o experiencia con tecnologías.
4. Propón el stack completo: frontend, backend, base de datos, autenticación, pagos, correo, almacenamiento de archivos, hosting, monitoreo. Compara 2 o 3 combinaciones en una tabla: costo mensual a 100 y a 10.000 usuarios, tiempo hasta el MVP, riesgo de quedar atado a un proveedor, madurez para multi-tenant. Recomienda una y explica por qué.
5. Crea la estructura de archivos del proyecto: docs/contexto/negocio.md (reglas de negocio y glosario), docs/contexto/fuera-de-alcance.md, docs/contexto/decisiones.md (una entrada corta por decisión, empezando por el stack), docs/PLAN.md con la estructura de abajo, y docs/specs/ con una spec por fase (objetivo, historias de usuario, criterios de aceptación, entidades y endpoints que toca, casos borde). Escribe en detalle solo la spec de la Fase 1; las demás, como esqueleto. Crea AGENTS.md (máximo 60 líneas: qué es el proyecto, stack, comandos, reglas de código y la instrucción "antes de cada tarea lee docs/PLAN.md, docs/contexto/ y la spec de la fase") y CLAUDE.md con una sola línea: @AGENTS.md. Crea también dos subagentes en .claude/agents/: revisor-codigo.md y revisor-seguridad.md. Ambos revisan el diff de la fase contra su spec (el de seguridad, también contra el modelo de amenazas y usando secure-code-review), no editan código y devuelven hallazgos ordenados por gravedad.
6. Muéstrame un resumen, el alcance del MVP y la lista de supuestos. Pregúntame si apruebo. Corrige hasta que yo escriba "aprobado". Solo entonces dime cuál es la Fase 1 y espera mi orden para empezar.

## Requisitos de calidad (obligatorios)
Ponlos como checkboxes dentro de la fase del plan que les corresponde. Ninguna fase se cierra sin verificar los suyos.
- App, experiencia: cada pantalla y lista tiene estado de carga (skeleton), estado vacío con una acción clara y estado de error con forma de reintentar; formularios con validación en cliente y en servidor, mensajes de error que dicen cómo corregir, y sin perder lo escrito si algo falla; confirmación en acciones destructivas y opción de deshacer cuando se pueda; listas largas paginadas o virtualizadas; fechas, números y moneda con el formato del país.
- App, móvil y accesibilidad: el flujo principal funciona a 360 px; áreas táctiles de mínimo 44×44 px; inputs de 16 px o más; contraste AA; todo usable con teclado, foco visible y labels en cada campo.
- Rendimiento: carga de código dividida por ruta; INP menor a 200 ms en las pantallas principales; consultas con índices y sin N+1; Lighthouse móvil de la landing 90 o más.
- Seguridad: los permisos se verifican en el servidor en cada acción (ocultar un botón no cuenta); prueba automática de que un usuario de un tenant no puede leer ni modificar datos de otro; rate limiting en login, registro, recuperación de contraseña y API pública; validación de todo input con esquemas; cookies de sesión httpOnly, Secure y SameSite; headers de seguridad (CSP, HSTS); webhooks de pago verificados por firma e idempotentes; secretos solo en variables de entorno.
- Datos: backups automáticos con una restauración probada; migraciones reversibles; un usuario puede exportar y pedir el borrado de sus datos.
- Observabilidad: captura de errores en frontend y backend; logs estructurados sin datos personales ni secretos; endpoint de health check; alerta cuando fallan pagos o sube la tasa de errores.
- Correo: correos transaccionales (verificación, recuperación de contraseña, recibo de pago) desde un dominio propio con SPF, DKIM y DMARC.
- Pruebas y CI: tests de la lógica de permisos y de las reglas de negocio; pruebas de punta a punta con Playwright de registro, login, pago y el flujo principal; CI que corre lint, tipos y tests en cada push.
- Landing: title y meta description únicos; Open Graph y Twitter card de 1200×630 px; favicon, apple-touch-icon y manifest; datos estructurados schema.org (Organization y SoftwareApplication); sitemap.xml y robots.txt; sin noindex en producción y con noindex en el entorno de pruebas; 404 personalizada; analítica con eventos de registro, inicio de prueba y pago.
- Legal: términos y condiciones, política de tratamiento de datos y aviso de cookies; la página de precios coincide exactamente con lo que cobra la pasarela; facturación electrónica si el país la exige (en Colombia, la DIAN).
- Al final del plan, como tareas mías (no las puedes hacer tú): dominio con HTTPS y DNS del correo; pasarela de pagos en modo producción; un pago real con tarjeta real y su reembolso; revisión de los textos legales por un abogado; Google Search Console con el sitemap enviado; prueba del flujo completo en un iPhone y un Android reales.

## Estructura de docs/PLAN.md
1. Resumen: problema, usuario, propuesta de valor, modelo de negocio.
2. Decisiones y supuestos, con fecha.
3. Alcance del MVP: historias de usuario con criterios de aceptación, y lista de lo que NO entra.
4. Stack elegido, por qué, y alternativas descartadas.
5. Arquitectura: diagrama en Mermaid de componentes y servicios externos.
6. Modelo de datos: entidades, relaciones y cómo se aísla cada tenant.
7. Roles y permisos: matriz rol × acción (con rbac-design).
8. Resumen del modelo de amenazas: top 10 amenazas y su mitigación (con threat-modeling).
9. Superficie de API o acciones del servidor.
10. Pantallas principales de la app y secciones de la landing.
11. Fases con checkboxes, cada una enlazada a su spec en docs/specs. Cada fase lleva: objetivo, tareas, skills a usar y criterio de aceptación verificable (pruebas que pasan, flujo que funciona de punta a punta).
12. Estrategia de pruebas, entornos (local, staging, producción) y variables de entorno necesarias (sin valores).
13. Riesgos y preguntas abiertas.
14. Registro de progreso (vacío).

## Reglas del plan
- Fases como rebanadas verticales: cada una deja algo que funciona de punta a punta, no "toda la base de datos" y luego "todo el frontend".
- Orden sugerido: base del proyecto, CI y entornos → autenticación y tenants → roles y permisos → entidad principal (CRUD completo) → flujo principal del usuario → pagos y planes → onboarding y notificaciones → landing → revisión de seguridad → QA con break-ui y mobile-native → audit y polish con impeccable → lanzamiento.
- Cada fase cierra con el subagente revisor-codigo sobre el diff. Si la fase tocó datos, auth o pagos, también con revisor-seguridad.
- Ningún secreto en el código ni en el plan.
- Cada fase lista sus requisitos de calidad como checkboxes y los verifica antes de cerrarse.
```

**Sobre pagos:** si usas una pasarela con página de pago alojada (como Stripe Checkout o el checkout de Mercado Pago), los datos de tarjeta no pasan por tu servidor y `pci-dss-review` casi nunca hace falta.

## Notas para Antigravity

Antigravity usa el mismo formato de skills (`SKILL.md`), así que el mismo kit funciona en las dos herramientas. Para pasar un proyecto de Claude Code a Antigravity a mitad de camino: haz commit, abre la carpeta en Antigravity y pega el prompt de continuación.

| Qué | Claude Code | Antigravity |
| --- | --- | --- |
| Skills del proyecto | `.claude/skills/` | `.agents/skills/` (también acepta `.agent/skills/`) |
| Skills globales | `~/.claude/skills/` | `~/.gemini/config/skills/` (app e IDE) |
| Invocar una skill a mano | `/nombre-skill` o nombrarla en el prompt | `/nombre-skill` o nombrarla en el prompt |
| Generar imágenes | No | Sí |

Como Antigravity sí genera imágenes, ahí puedes aprovechar tres skills que en Claude Code no rinden:

- En el playbook 1.1, `imagegen-frontend-web` o el flujo original de `image-to-code` pueden generar una imagen de referencia por sección antes de programar. Un buen uso: generar las referencias en Antigravity durante la fase de diseño y construir después con el playbook 1.2 en Claude Code.
- `brandkit` sirve cuando el cliente no tiene identidad de marca y necesitas proponerle logo, paleta y tipografía.

### Fuentes

- [Agent skills — documentación de Google Antigravity](https://antigravity.google/docs/skills): rutas de skills y cómo se invocan.
