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

En PowerShell usa `.\mvnw.cmd` en lugar de `./mvnw`.

Pruebas del backend (necesitan Docker): `cd backend && ./mvnw test`.

`app/` es el demo original y queda solo como referencia.
