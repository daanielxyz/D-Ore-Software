# D'Ore

Demo de un SaaS de inventario para una floristería. Contexto, reglas, plan y
decisiones en `docs/`.

## Correr en local
Requisitos: Java 21, Node 24 y Docker abierto.

```bash
docker compose up -d                  # base de datos
cd backend && ./mvnw spring-boot:run  # http://localhost:8081
cd frontend && npm install && npm run dev
```

**Entrar:** el usuario es `admin`. Define la clave antes de arrancar el backend
(`$env:APP_PASSWORD='tu-clave'` en PowerShell, `export APP_PASSWORD=tu-clave` en
bash). Si no la defines, el backend imprime una temporal al iniciar.

Para cargar datos ficticios de ejemplo, arranca el backend con
`APP_DEMO_DATA=true` sobre una base vacía. Guion en `docs/GUION_DEMO.md`.

En PowerShell usa `.\mvnw.cmd` en lugar de `./mvnw`.

Pruebas del backend (necesitan Docker): `cd backend && ./mvnw test`.

`app/` es el demo original y queda solo como referencia.
