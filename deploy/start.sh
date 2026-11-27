#!/bin/sh
# Arranque en el servidor. Los secretos llegan como variables de entorno.
set -e

# Render entrega postgresql://usuario:clave@host/base y Java necesita jdbc:postgresql://host/base.
if [ -n "$DATABASE_URL" ]; then
  export DB_URL="jdbc:postgresql://${DATABASE_URL#*@}"
fi

if [ -z "$APP_PASSWORD" ]; then
  echo "Falta APP_PASSWORD: no se publica sin una contraseña." >&2
  exit 1
fi

export SERVER_ADDRESS=0.0.0.0
export SERVER_PORT="${PORT:-10000}"
# Detrás del proxy HTTPS: así las cookies salen marcadas como seguras.
export SERVER_FORWARD_HEADERS_STRATEGY=framework

exec java -XX:MaxRAMPercentage=60 -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -jar app.jar
