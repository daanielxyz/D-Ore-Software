# Guion de la demo

Duración estimada: 10 minutos (sin ensayar). Todos los datos son ficticios.

## Preparación
1. Abrir Docker.
2. Para empezar de cero: `docker compose down -v`. **Borra toda la base local.**
3. `docker compose up -d`
4. Tener definida `GEMINI_API_KEY`.
5. Backend, en PowerShell, desde `backend/`:
   ```powershell
   $env:APP_PASSWORD='<clave>'; $env:APP_DEMO_DATA='true'; .\mvnw.cmd spring-boot:run
   ```
6. Frontend, desde `frontend/`: `npm run dev`
7. Entrar con `admin` y la clave.

Las fechas de ejemplo son relativas al día de la carga: cargar el mismo día.

## Recorrido
| # | Pantalla | Qué hacer | Qué contar |
|---|---|---|---|
| 1 | Resumen | Mostrar alertas y entregas | De un vistazo: qué reponer, qué vence y qué entregar |
| 2 | Inventario | Filtrar por stock bajo y por vencimiento | Control por lotes de flor fresca |
| 3 | Inventario | Girasol → Movimiento → Merma, 4, lote vencido | La flor vencida no se puede vender |
| 4 | Inventario | Abrir Girasol y ver historial | Cada cambio queda registrado |
| 5 | Asistente | "Llegaron 50 tallos de Rosa Freedom roja" → confirmar | Escribe y el inventario se actualiza, con confirmación |
| 6 | Asistente | Nota de voz: "Se dañaron 3 tulipanes" → confirmar | También entiende notas de voz |
| 7 | Pedidos | Pasar el pedido de Valentina a "En diseño" | El inventario se descuenta solo |
| 8 | Pedidos | Marcar como entregado el pedido de Camila | La venta se registra sola |
| 9 | Ventas | Venta de mostrador de 5 rosas | También descuenta inventario |
| 10 | Clientes | Mostrar compras acumuladas | Historial por cliente |

## Qué no prometer
- Un solo usuario, sin roles.
- Sin pagos, abonos ni facturación electrónica.
- Finanzas, analítica, contenido y social listening: aún no están.

## Antes de presentar
- [ ] Ensayo completo con datos recién cargados.
- [ ] Probar el micrófono en el equipo de la presentación.
- [ ] Plan B si falla internet o Gemini: hacer los pasos 5 y 6 a mano.
