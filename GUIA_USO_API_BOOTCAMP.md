# API Bootcamp - Guía de Uso

## Endpoints Disponibles en Capacity-API

Los usuarios deben llamar a los siguientes endpoints del microservicio **capacity-api** (puerto 8082) para gestionar bootcamps:

### 1. Registrar Bootcamp

**Endpoint:** `POST /capacity/bootcamp`

**Headers:**
- `x-message-id`: UUID para trazabilidad
- `Content-Type`: application/json

**Body:**
```json
{
  "name": "Bootcamp Java Full Stack",
  "description": "Bootcamp intensivo de desarrollo Java con Spring Boot y Angular",
  "launchDate": "2026-03-01",
  "duration": 90,
  "capacityIds": [1, 2, 3]
}
```

**Validaciones:**
- ✅ `name`: Requerido, máximo 50 caracteres, único
- ✅ `description`: Requerida, máximo 90 caracteres
- ✅ `launchDate`: Requerida, formato `YYYY-MM-DD`, no puede ser del pasado
- ✅ `duration`: Requerida, número entero, mínimo 1 día
- ✅ `capacityIds`: Lista de IDs de capacidades, mínimo 1, máximo 4, sin duplicados
- ✅ Las capacidades deben existir en el sistema

**Respuesta Exitosa (201 Created):**
```json
"Bootcamp created successfully"
```

**Ejemplo con cURL:**
```bash
curl -X POST http://localhost:8082/capacity/bootcamp \
  -H "x-message-id: $(uuidgen)" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bootcamp Java Full Stack",
    "description": "Bootcamp intensivo de desarrollo Java con Spring Boot",
    "launchDate": "2026-03-01",
    "duration": 90,
    "capacityIds": [1, 2, 3]
  }'
```

---

### 2. Listar Bootcamps (Paginado)

**Endpoint:** `GET /capacity/bootcamp`

**Query Parameters:**
- `page`: Número de página (default: 0)
- `size`: Tamaño de página (default: 10, max: 100)
- `sortBy`: Campo de ordenamiento
  - `NAME`: Ordenar por nombre (default)
  - `TECHNOLOGY_COUNT`: Ordenar por cantidad de capacidades
- `sortDirection`: Dirección de ordenamiento
  - `ASC`: Ascendente (default)
  - `DESC`: Descendente

**Headers:**
- `x-message-id`: UUID para trazabilidad

**Respuesta Exitosa (200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Bootcamp Java Full Stack",
      "description": "Bootcamp intensivo de desarrollo Java",
      "launchDate": "2026-03-01",
      "duration": 90,
      "capacities": [
        {
          "id": 1,
          "name": "Desarrollo Backend",
          "technologies": [
            {
              "id": 1,
              "name": "Java"
            },
            {
              "id": 2,
              "name": "Spring Boot"
            }
          ]
        },
        {
          "id": 2,
          "name": "Desarrollo Frontend",
          "technologies": [
            {
              "id": 3,
              "name": "Angular"
            },
            {
              "id": 4,
              "name": "TypeScript"
            }
          ]
        }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 15,
  "totalPages": 2,
  "first": true,
  "last": false
}
```

**Ejemplos con cURL:**

Listar por nombre ascendente:
```bash
curl -X GET "http://localhost:8082/capacity/bootcamp?page=0&size=10&sortBy=NAME&sortDirection=ASC" \
  -H "x-message-id: $(uuidgen)"
```

Listar por cantidad de capacidades descendente:
```bash
curl -X GET "http://localhost:8082/capacity/bootcamp?page=0&size=10&sortBy=TECHNOLOGY_COUNT&sortDirection=DESC" \
  -H "x-message-id: $(uuidgen)"
```

---

### 3. Eliminar Bootcamp

**Endpoint:** `DELETE /capacity/bootcamp/{id}`

**Path Parameters:**
- `id`: ID del bootcamp a eliminar

**Headers:**
- `x-message-id`: UUID para trazabilidad

**Comportamiento:**
- ✅ Elimina el bootcamp
- ✅ Elimina las relaciones bootcamp-capacidad
- ✅ Si una capacidad está asociada SOLO a este bootcamp, se elimina la capacidad y sus tecnologías
- ✅ Si una capacidad está asociada a OTROS bootcamps, NO se elimina
- ✅ La operación es TRANSACCIONAL (todo o nada)

**Respuesta Exitosa (200 OK):**
```json
"Bootcamp deleted successfully"
```

**Ejemplo con cURL:**
```bash
curl -X DELETE http://localhost:8082/capacity/bootcamp/1 \
  -H "x-message-id: $(uuidgen)"
```

---

## Respuestas de Error

### Error de Validación (400 Bad Request)
```json
{
  "code": "400",
  "message": "Bad Parameters, please verify data",
  "identifier": "550e8400-e29b-41d4-a716-446655440000",
  "date": "2026-01-16T10:30:00Z",
  "errors": [
    {
      "code": "400",
      "message": "Bootcamp must have at least 1 capacity",
      "param": "capacityIds"
    }
  ]
}
```

### Bootcamp No Encontrado (404 Not Found)
```json
{
  "code": "404",
  "message": "Bootcamp not found",
  "identifier": "550e8400-e29b-41d4-a716-446655440000",
  "date": "2026-01-16T10:30:00Z",
  "errors": [
    {
      "code": "404",
      "message": "Bootcamp not found",
      "param": "id"
    }
  ]
}
```

### Error Interno del Servidor (500 Internal Server Error)
```json
{
  "code": "500",
  "message": "Something went wrong, please try again",
  "identifier": "550e8400-e29b-41d4-a716-446655440000",
  "date": "2026-01-16T10:30:00Z",
  "errors": [
    {
      "code": "500",
      "message": "Something went wrong, please try again",
      "param": ""
    }
  ]
}
```

---

## Arquitectura de Microservicios

```
Usuario
   ↓
Capacity-API (Puerto 8082)
   ↓
Bootcamp-API (Puerto 8080)
   ↓
Base de Datos PostgreSQL
```

### Flujo de Datos:

1. **Registro de Bootcamp:**
   - Usuario → Capacity-API → Bootcamp-API
   - Bootcamp-API valida y guarda en BD
   - Retorna respuesta al usuario

2. **Listado de Bootcamps:**
   - Usuario → Capacity-API → Bootcamp-API
   - Bootcamp-API obtiene datos y enriquece con capacidades/tecnologías
   - Capacity-API retorna datos completos al usuario

3. **Eliminación de Bootcamp:**
   - Usuario → Capacity-API → Bootcamp-API
   - Bootcamp-API ejecuta eliminación transaccional
   - Notifica a Capacity-API para gestionar referencias
   - Retorna confirmación al usuario

---

## Configuración

### Bootcamp-API (application.yaml)
```yaml
server:
  port: 8080

spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/bootcamp_db
    username: postgres
    password: postgres

external:
  capacity:
    base-url: ${BASE_URL_CAPACITY:http://localhost:8082}
```

### Capacity-API (application.yaml)
```yaml
server:
  port: 8082

spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/capacity_db
    username: postgres
    password: postgres

external:
  bootcamp:
    base-url: ${BASE_URL_BOOTCAMP:http://localhost:8080}
  technology:
    base-url: ${BASE_URL_TECH:http://localhost:8081}
```

---

## Casos de Uso de Ejemplo

### Caso 1: Crear un Bootcamp de Java
```bash
curl -X POST http://localhost:8082/capacity/bootcamp \
  -H "x-message-id: 123e4567-e89b-12d3-a456-426614174000" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Bootcamp Java Backend",
    "description": "Aprende desarrollo backend con Java y Spring Boot",
    "launchDate": "2026-04-01",
    "duration": 60,
    "capacityIds": [1, 2]
  }'
```

### Caso 2: Listar Bootcamps Ordenados por Nombre
```bash
curl -X GET "http://localhost:8082/capacity/bootcamp?page=0&size=5&sortBy=NAME&sortDirection=ASC" \
  -H "x-message-id: 123e4567-e89b-12d3-a456-426614174001"
```

### Caso 3: Eliminar un Bootcamp
```bash
curl -X DELETE http://localhost:8082/capacity/bootcamp/1 \
  -H "x-message-id: 123e4567-e89b-12d3-a456-426614174002"
```

---

## Validaciones de Negocio

### Validaciones en Registro:
- ✅ Nombre único (no puede existir otro bootcamp con el mismo nombre)
- ✅ Fecha de lanzamiento futura (no se permiten fechas pasadas)
- ✅ Duración positiva (mínimo 1 día)
- ✅ Entre 1 y 4 capacidades asociadas
- ✅ Capacidades sin duplicados
- ✅ Todas las capacidades deben existir en el sistema

### Validaciones en Eliminación:
- ✅ El bootcamp debe existir
- ✅ Se eliminan las capacidades solo si no están referenciadas por otros bootcamps
- ✅ Se eliminan las tecnologías junto con las capacidades si aplica
- ✅ La operación es atómica (transaccional)

---

## Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 3.3.6**
- **Spring WebFlux** (Programación Reactiva)
- **Project Reactor** (Mono/Flux)
- **R2DBC** (Reactive Database Connectivity)
- **PostgreSQL** (Base de datos)
- **MapStruct** (Mapeo de objetos)
- **Lombok** (Reducción de código boilerplate)
- **Arquitectura Hexagonal**

---

## Principios Aplicados

✅ **SOLID**: Separación de responsabilidades, interfaces bien definidas
✅ **Clean Code**: Nombres descriptivos, métodos pequeños, código limpio
✅ **Programación Reactiva**: Uso de Mono/Flux para operaciones no bloqueantes
✅ **Transaccionalidad**: Operaciones atómicas con manejo de errores
✅ **Trazabilidad**: Header x-message-id para seguimiento de requests

