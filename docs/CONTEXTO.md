# Contexto

## Qué es
Demo de un SaaS para una floristería en Cali (Colombia). Idioma de la interfaz:
español. Moneda: COP. Zona horaria: America/Bogota.

## Prioridades
1. **Manejo de inventario funcional** (lo más importante).
2. **Asistente de IA** para agregar, quitar, modificar y eliminar inventario con
   texto o nota de voz.
3. La misma gestión de forma manual, como en un SaaS normal.
4. Funciones básicas que una floristería esperaría (pedidos, clientes, ventas).

## Equipo
| Persona | Responsabilidad |
|---|---|
| daanielxyz | Revisión del demo, base nueva y migración |
| Nicolás | Asistente de IA del inventario |

## Estado del código
- Repo: `daanielxyz/D-Ore-Software`.
- `app/`: demo generado en Higgsfield con un solo prompt. Marca usada: "FLORE".
- Stack del demo: TypeScript, React 19, TanStack Start, Tailwind, Cloudflare
  Workers con base D1 (SQLite), Bun.
- `backend/` y `frontend/`: aún no existen.

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
