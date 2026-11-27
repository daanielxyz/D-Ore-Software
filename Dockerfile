# Una sola imagen: Spring Boot sirve la API y el frontend compilado.
FROM node:24-alpine AS frontend
WORKDIR /build
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

FROM eclipse-temurin:21-jdk AS backend
WORKDIR /build
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
COPY backend/src src
COPY --from=frontend /build/dist src/main/resources/static
RUN sh mvnw -q -B package -DskipTests && cp target/backend-*.jar app.jar \
    && java -Djarmode=tools -jar app.jar extract --destination extracted

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend /build/extracted/ ./
COPY deploy/start.sh start.sh
RUN useradd --system dore
USER dore
EXPOSE 10000
CMD ["sh", "start.sh"]
