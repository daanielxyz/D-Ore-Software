# Contexto

## Qué es
Demo de un SaaS para una floristería en Cali (Colombia). Idioma de la interfaz:
español. Moneda: COP. Zona horaria: America/Bogota.

**Entrega: 2026-11-27 antes de las 4 p. m.** Se envía al cliente un video
grabado recorriendo la página y el enlace de la demo. No es presencial.

## Marca
El logo dice **"D'Orée Floristería"**. La interfaz y los documentos todavía dicen
"D'Ore" (`App.tsx`, `LoginPage.tsx`, `index.html`, `README.md`).

Logos en `redesign/images/` (en el equipo de daanielxyz, **sin subir a git**):
- `Logotipo floral D’Orée en dorado.png`: 1536×1024, fondo blanco, 1,9 MB.
- `Logotipo floral D’Orée en dorado transparente.png`: mismo tamaño, esquinas
  transparentes y un halo de color alrededor (sin probar sobre fondo claro), 2,3 MB.

Hay que reducirlos antes de usarlos en la web y copiarlos dentro de `frontend/`
(la imagen Docker no incluye `redesign/`).

Colores medidos en el logo de fondo blanco (aproximados; punto de partida, no la
paleta final):

| Zona | Color |
|---|---|
| Letras doradas | `#C88808` |
| Texto "Floristería" (verde) | `#185818` |
| Hojas | `#598501` |
| Fucsia | `#FB3EB6` |
| Naranja | `#FD5E02` |
| Amarillo | `#FDDD13` |
| Morado | `#8A1EA0` |

El dorado sobre blanco tiene contraste cercano a 3:1: no sirve para texto pequeño.

## Prioridades
1. **Manejo de inventario funcional** (lo más importante).
2. **Asistente de IA** para agregar, quitar, modificar y eliminar inventario con
   texto o nota de voz.
3. La misma gestión de forma manual, como en un SaaS normal.
4. Funciones básicas que una floristería esperaría (pedidos, clientes, ventas).

## Equipo
| Persona | Responsabilidad |
|---|---|
| daanielxyz | Todo, incluido el asistente (desde 2026-11-26) |
| Nicolás | Sin tareas por ahora; hizo el prototipo en `prototipos/asistente/` |

## Cómo trabajamos
- Hablar en español.
- daanielxyz publica ramas y hace los pull requests con GitHub Desktop; el agente
  solo hace commits locales (ver `REGLAS.md`) y explica los pasos.

## Estado del código
- Repo: `daanielxyz/D-Ore-Software`.
- `app/`: demo generado en Higgsfield con un solo prompt. Marca usada: "FLORE".
- Stack del demo: TypeScript, React 19, TanStack Start, Tailwind, Cloudflare
  Workers con base D1 (SQLite), Bun.
- `backend/`: Java 21, Spring Boot 4.2.0, PostgreSQL y Flyway. Paquetes:
  inventory, assistant, auth, orders, customers, sales, finance, analytics,
  content, listening, demo.
- `frontend/`: React, Vite y TypeScript. Una carpeta por pantalla en
  `src/features/`; navegación por hash en `App.tsx`.
- Estilos: todo en `frontend/src/index.css`, con variables en `:root` (hay además
  colores sueltos, como `#fff` y los fondos de `.tag`). La fuente `Outfit` está
  declarada pero no se carga: se ve la del sistema si el equipo no la tiene.
- Pantallas: resumen, asistente, pedidos, inventario, clientes, ventas, finanzas,
  analítica, contenido, social listening y login.
- Publicado en https://dore-0z6o.onrender.com (Render, usuario `admin`). Cada
  cambio en `main` vuelve a publicar. Datos de ejemplo cargados el 2026-11-26;
  volver a publicar no los recarga. Plan elegido en Render: sin confirmar.

## Equipo local
- Windows 11, Java 21, Node 24 y Docker Desktop. No hay `gh` ni `bun`.
- Puertos: base 5433, backend 8081, Vite 5173.
- Si el backend no encuentra `GEMINI_API_KEY` (variable de usuario), reiniciar las
  aplicaciones desde las que se abre la terminal.

## Hallazgos del demo (revisión del 2026-11-26)
Salen de leer el código. **No se ha ejecutado**: Bun no está instalado y la base
D1 local está comentada en `app/wrangler.jsonc`.

Archivos principales: `app/src/routes/app.tsx`,
`app/src/lib/api/flore.functions.ts`, `app/migrations/0002_flore.sql`.

### Inventario
- Solo permite crear productos y sumar o restar de 1 en 1. No hay editar ni eliminar.
- No hay historial de movimientos (entradas, salidas, mermas, ajustes).
- El navegador envía el stock final, no la diferencia: dos cambios simultáneos se pisan.
- Sin unidad de medida (tallo, ramo, rollo), vencimiento ni lotes.
- SKU sin restricción de unicidad; el proveedor es texto libre.

### Negocio
- Los pedidos no tienen productos, no descuentan stock y no se enlazan con clientes ni ventas.
- Los totales del cliente (`total_spent`, `orders_count`) nunca se actualizan.
- El código de pedido sale de los últimos 5 dígitos del reloj y puede repetirse.
- La fecha por defecto usa UTC: después de las 7 p. m. en Cali registra el día siguiente.

### Seguridad
- Sin login: `/app` y todas las funciones de servidor son públicas.

### Cosas simuladas
- El "+12,8%" y la gráfica de ventas son valores fijos.
- Configuración no guarda; "Exportar reporte" y "Centro de ayuda" no hacen nada.
- La pantalla de configuración dice que la app no usa IA.

### Calidad
- Toda la app está en un solo archivo, con líneas muy largas y tipos `any`.
- Mucho código de plantilla de Higgsfield (`app/packages/`, componentes de
  generación de imágenes y video) que las rutas del producto no usan.
- El CI usa un runner de Higgsfield (`arc-runners-frontend`) que probablemente
  no existe para este repo.

### Reutilizable
Paleta y tipografía (`app/design-brief.md`), textos en español, estructura de
pantallas, datos de ejemplo y recursos en `app/public/assets/`.
