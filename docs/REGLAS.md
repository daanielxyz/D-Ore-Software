# Reglas

## Documentación (personas y agentes)
- Al empezar un chat: `git pull` y leer los cuatro documentos.
- Al terminar una tarea o antes de cambiar de chat:
  - `PLAN.md`: marcar tareas y actualizar **tu** sección de "Estado actual".
  - `DECISIONES.md`: añadir las decisiones nuevas al final. No reescribir las
    anteriores; si una cambia, añadir otra que la reemplace.
  - `CONTEXTO.md`: actualizar solo si cambió un hecho del proyecto.
- Escribir hechos verificados. Lo no probado se marca como "sin verificar".
- Nunca escribir claves, tokens ni datos reales de clientes.

## Git
**Para todos los agentes (obligatorio):**
- El agente puede hacer commits.
- El agente **nunca** hace push, ni crea o une pull requests, ni hace nada en el
  repositorio remoto. Eso lo hace la persona.
- El agente no puede quedar como colaborador: sin `Co-Authored-By`, sin
  "Generated with ..." y sin cambiar el autor de git.

**Flujo:**
- Nada directo a `main`: una rama por tarea (`feat/...`, `fix/...`) y pull request.
- La otra persona revisa antes de unir.
- Commits pequeños y en español.

## Revisión del trabajo de Nicolás
Nicolás usa Antigravity con modelos de Google. Antes de cada commit su agente
verifica el trabajo, y después de cada commit el agente de daanielxyz lo revisa
antes de que se una a `main`.

## Territorios (para no pisarse)
| Zona | Dueño |
|---|---|
| `backend/.../inventory`, `orders`, `customers`, `auth` | daanielxyz |
| `backend/.../assistant` | Nicolás |
| `frontend/src/features/assistant` | Nicolás |
| `prototipos/asistente` | Nicolás |
| Resto de `frontend/` | daanielxyz |
| Contrato del inventario y migraciones de base de datos | Compartido: se acuerda antes de cambiar |

## Código
- Backend: Java 21 y Spring Boot, organizado por funcionalidad. Controlador →
  servicio → repositorio. La lógica de negocio vive en los servicios.
- Se exponen DTOs, no entidades.
- Frontend: React, Vite y TypeScript estricto, sin `any`.
- Nombres de código en inglés; textos visibles en español.
- Dinero en pesos enteros. Fechas del negocio en America/Bogota.
- Esquema solo por migraciones Flyway, con versión por fecha y hora
  (`V20261126_1500__descripcion.sql`) para evitar choques.
- Todo cambio de stock pasa por el servicio de inventario y deja un movimiento.
- Las reglas de inventario y las herramientas del asistente llevan pruebas.
- Secretos en variables de entorno; `.env` fuera de git y `.env.example` dentro.

## Asistente
- Nada se modifica sin confirmación del usuario.
- Con la capa gratuita de Gemini, solo datos ficticios.
- No accede a la base de datos: solo llama a los mismos servicios que la interfaz.
- La IA propone y el backend valida.
- Si el producto o la cantidad son ambiguos, pregunta; no adivina.

## `app/`
Solo lectura, como referencia. Se elimina cuando la versión nueva lo iguale.
