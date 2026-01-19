# Configuración y Ejecución de Microservicios Bootcamp

## 🗄️ Configuración de Base de Datos

### PostgreSQL con Docker

#### 1. Base de datos para Bootcamp-API
```bash
docker run -d \
  --name postgres-bootcamp \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=bootcamp_db \
  -p 5432:5432 \
  postgres:15
```

#### 2. Base de datos para Capacity-API
```bash
docker run -d \
  --name postgres-capacity \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=capacity_db \
  -p 5433:5432 \
  postgres:15
```

#### 3. Base de datos para Technology-API
```bash
docker run -d \
  --name postgres-technology \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=technology_db \
  -p 5434:5432 \
  postgres:15
```

---

## ⚙️ Archivos de Configuración

### Bootcamp-API - application.yaml
```yaml
server:
  port: 8080

spring:
  application:
    name: bootcamp-api
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/bootcamp_db
    username: postgres
    password: postgres
    pool:
      initial-size: 10
      max-size: 20

external:
  capacity:
    base-url: ${BASE_URL_CAPACITY:http://localhost:8082}

logging:
  level:
    com.example.resilient_api: DEBUG
    org.springframework.r2dbc: DEBUG
```

### Capacity-API - application.yaml
```yaml
server:
  port: 8082

spring:
  application:
    name: capacity-api
  r2dbc:
    url: r2dbc:postgresql://localhost:5433/capacity_db
    username: postgres
    password: postgres
    pool:
      initial-size: 10
      max-size: 20

external:
  bootcamp:
    base-url: ${BASE_URL_BOOTCAMP:http://localhost:8080}
  technology:
    base-url: ${BASE_URL_TECH:http://localhost:8081}

logging:
  level:
    com.example.resilient_api: DEBUG
    org.springframework.r2dbc: DEBUG
```

### Technology-API - application.yaml
```yaml
server:
  port: 8081

spring:
  application:
    name: technology-api
  r2dbc:
    url: r2dbc:postgresql://localhost:5434/technology_db
    username: postgres
    password: postgres
    pool:
      initial-size: 10
      max-size: 20

logging:
  level:
    com.example.resilient_api: DEBUG
    org.springframework.r2dbc: DEBUG
```

---

## 🚀 Orden de Ejecución

### 1. Iniciar Bases de Datos (primero)
```bash
# Iniciar los 3 contenedores de PostgreSQL
docker start postgres-technology
docker start postgres-capacity
docker start postgres-bootcamp

# Verificar que estén corriendo
docker ps
```

### 2. Iniciar Technology-API (segundo)
```bash
cd technology-api
./gradlew bootRun
```
**Puerto:** 8081
**Esperar mensaje:** "Started ResilientApiApplication"

### 3. Iniciar Capacity-API (tercero)
```bash
cd capacity-api
./gradlew bootRun
```
**Puerto:** 8082
**Esperar mensaje:** "Started ResilientApiApplication"

### 4. Iniciar Bootcamp-API (cuarto)
```bash
cd bootcamp-api
./gradlew bootRun
```
**Puerto:** 8080
**Esperar mensaje:** "Started ResilientApiApplication"

---

## 🧪 Verificación de Servicios

### Health Checks

#### Technology-API
```bash
curl http://localhost:8081/actuator/health
```

#### Capacity-API
```bash
curl http://localhost:8082/actuator/health
```

#### Bootcamp-API
```bash
curl http://localhost:8080/actuator/health
```

**Respuesta esperada:**
```json
{
  "status": "UP"
}
```

---

## 📝 Flujo de Prueba Completo

### 1. Crear Tecnologías (en Technology-API)
```bash
# Crear Java
curl -X POST http://localhost:8081/technology \
  -H "x-message-id: test-001" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Java",
    "description": "Lenguaje de programación orientado a objetos"
  }'

# Crear Spring Boot
curl -X POST http://localhost:8081/technology \
  -H "x-message-id: test-002" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Spring Boot",
    "description": "Framework para desarrollo de aplicaciones Java"
  }'

# Crear Angular
curl -X POST http://localhost:8081/technology \
  -H "x-message-id: test-003" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Angular",
    "description": "Framework para desarrollo frontend"
  }'
```

### 2. Crear Capacidades (en Capacity-API)
```bash
# Crear capacidad Backend
curl -X POST http://localhost:8082/capacity \
  -H "x-message-id: test-004" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Desarrollo Backend",
    "description": "Capacidad de desarrollo backend con Java",
    "technologyIds": [1, 2]
  }'

# Crear capacidad Frontend
curl -X POST http://localhost:8082/capacity \
  -H "x-message-id: test-005" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Desarrollo Frontend",
    "description": "Capacidad de desarrollo frontend con Angular",
    "technologyIds": [3]
  }'
```

### 3. Crear Bootcamp (en Capacity-API)
```bash
curl -X POST http://localhost:8082/capacity/bootcamp \
  -H "x-message-id: test-006" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bootcamp Java Full Stack",
    "description": "Bootcamp completo de desarrollo Java Full Stack",
    "launchDate": "2026-03-01",
    "duration": 90,
    "capacityIds": [1, 2]
  }'
```

### 4. Listar Bootcamps
```bash
curl -X GET "http://localhost:8082/capacity/bootcamp?page=0&size=10&sortBy=NAME&sortDirection=ASC" \
  -H "x-message-id: test-007"
```

### 5. Eliminar Bootcamp
```bash
curl -X DELETE http://localhost:8082/capacity/bootcamp/1 \
  -H "x-message-id: test-008"
```

---

## 🐛 Troubleshooting

### Error: Base de datos no conecta
```bash
# Verificar que PostgreSQL esté corriendo
docker ps | grep postgres

# Ver logs del contenedor
docker logs postgres-bootcamp

# Reiniciar contenedor si es necesario
docker restart postgres-bootcamp
```

### Error: Puerto ya en uso
```bash
# Ver qué proceso usa el puerto
netstat -ano | findstr :8080

# En Windows, matar el proceso
taskkill /PID <PID> /F

# En Linux/Mac
kill -9 <PID>
```

### Error: Tablas no existen
Las tablas se crean automáticamente gracias a `schema.sql`. Si hay problemas:

1. Verificar que `schema.sql` esté en `src/main/resources/`
2. Revisar logs de inicio de la aplicación
3. Conectarse manualmente y crear las tablas:

```sql
-- Para Bootcamp-API
\c bootcamp_db

CREATE TABLE IF NOT EXISTS bootcamp (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(90) NOT NULL,
    launch_date DATE NOT NULL,
    duration INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS bootcamp_capacity (
    id BIGSERIAL PRIMARY KEY,
    bootcamp_id BIGINT NOT NULL,
    capacity_id BIGINT NOT NULL,
    CONSTRAINT fk_bootcamp FOREIGN KEY (bootcamp_id) 
        REFERENCES bootcamp(id) ON DELETE CASCADE,
    CONSTRAINT uk_bootcamp_capacity UNIQUE (bootcamp_id, capacity_id)
);
```

---

## 📊 Monitoreo

### Métricas de Prometheus
```bash
# Bootcamp-API
curl http://localhost:8080/actuator/prometheus

# Capacity-API
curl http://localhost:8082/actuator/prometheus

# Technology-API
curl http://localhost:8081/actuator/prometheus
```

### Logs
```bash
# Ver logs en tiempo real
tail -f logs/application.log

# En Docker (si se ejecuta en contenedor)
docker logs -f bootcamp-api
```

---

## 🔒 Variables de Entorno

Puedes configurar variables de entorno para cambiar configuraciones:

```bash
# Para Bootcamp-API
export BASE_URL_CAPACITY=http://localhost:8082
export SERVER_PORT=8080
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=bootcamp_db
export DB_USER=postgres
export DB_PASSWORD=postgres

# Ejecutar
./gradlew bootRun
```

O usar un archivo `.env`:
```env
BASE_URL_CAPACITY=http://localhost:8082
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=5432
DB_NAME=bootcamp_db
DB_USER=postgres
DB_PASSWORD=postgres
```

---

## 📦 Build y Deploy

### Build JAR
```bash
# Bootcamp-API
cd bootcamp-api
./gradlew clean build
# JAR generado en: build/libs/bootcamp-api-0.0.1-SNAPSHOT.jar

# Capacity-API
cd capacity-api
./gradlew clean build
# JAR generado en: build/libs/capacity-api-0.0.1-SNAPSHOT.jar
```

### Ejecutar JAR
```bash
java -jar build/libs/bootcamp-api-0.0.1-SNAPSHOT.jar
```

### Docker (opcional)
```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
docker build -t bootcamp-api:1.0 .
docker run -d -p 8080:8080 --name bootcamp-api bootcamp-api:1.0
```

---

## ✅ Checklist de Verificación

- [ ] PostgreSQL corriendo en puertos 5432, 5433, 5434
- [ ] Technology-API iniciado en puerto 8081
- [ ] Capacity-API iniciado en puerto 8082
- [ ] Bootcamp-API iniciado en puerto 8080
- [ ] Health checks responden OK
- [ ] Crear tecnologías funciona
- [ ] Crear capacidades funciona
- [ ] Crear bootcamp funciona
- [ ] Listar bootcamps funciona
- [ ] Eliminar bootcamp funciona

---

## 🎓 Comandos Útiles

```bash
# Ver todos los endpoints de un servicio
curl http://localhost:8080/actuator/mappings

# Ver configuración actual
curl http://localhost:8080/actuator/env

# Ver métricas
curl http://localhost:8080/actuator/metrics

# Ver logs de nivel específico
curl -X POST http://localhost:8080/actuator/loggers/com.example.resilient_api \
  -H "Content-Type: application/json" \
  -d '{"configuredLevel": "DEBUG"}'
```

---

## 📚 Documentación Relacionada

- [GUIA_USO_API_BOOTCAMP.md](GUIA_USO_API_BOOTCAMP.md) - Guía de uso de la API
- [IMPLEMENTACION_COMPLETA.md](IMPLEMENTACION_COMPLETA.md) - Resumen de implementación
- [RESUMEN_IMPLEMENTACION_BOOTCAMP.md](RESUMEN_IMPLEMENTACION_BOOTCAMP.md) - Detalles técnicos

---

## 🎉 ¡Listo para Usar!

Una vez que todos los servicios estén corriendo, puedes empezar a crear bootcamps llamando a:

```
http://localhost:8082/capacity/bootcamp
```

**¡Buena suerte con tu aplicación!** 🚀

