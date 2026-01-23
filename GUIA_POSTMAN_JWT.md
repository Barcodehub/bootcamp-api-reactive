# 📮 Guía Completa de Pruebas en Postman - Sistema de Enrollment con JWT

## 🔐 Configuración Inicial

**Base URLs:**
- **Capacity-API** (Entrada principal): `http://localhost:8082`
- **Bootcamp-API** (Directo): `http://localhost:8080`
- **Users-API**: `http://localhost:8081`

---

## 🚀 PASO 1: Obtener Token JWT

### 1.1 Crear un Usuario (si no existe)

```http
POST http://localhost:8081/users
Content-Type: application/json

{
  "name": "Juan Pérez",
  "email": "juan.perez@example.com",
  "password": "SecurePass123!",
  "isAdmin": false
}
```

**Respuesta esperada (201 Created):**
```json
{
  "id": 1,
  "name": "Juan Pérez",
  "email": "juan.perez@example.com",
  "isAdmin": false
}
```

### 1.2 Login para obtener Token JWT

```http
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "email": "juan.perez@example.com",
  "password": "SecurePass123!"
}
```

**Respuesta esperada (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWFuLnBlcmV6QGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJpc0FkbWluIjpmYWxzZSwiaWF0IjoxNzM3NDg4NDAwLCJleHAiOjE3Mzc0OTIwMDB9.xyz123..."
}
```

**⚠️ IMPORTANTE:** Guarda este token, lo necesitarás para todas las peticiones de enrollment.

---

## 📋 PASO 2: Inscribir Usuario en Bootcamp

### Endpoint Principal (Capacity-API)

```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWFuLnBlcmV6QGV4YW1wbGUuY29tIiwidXNlcklkIjoxLCJpc0FkbWluIjpmYWxzZSwiaWF0IjoxNzM3NDg4NDAwLCJleHAiOjE3Mzc0OTIwMDB9.xyz123...

{
  "bootcampId": 1
}
```

**⚠️ NOTA:** 
- **NO envíes** `userId` en el body - se obtiene automáticamente del token JWT
- El token identifica al usuario que está haciendo la petición
- Solo puedes inscribirte a ti mismo

**Respuesta esperada (201 Created):**
```json
{
  "id": 1,
  "bootcampId": 1,
  "userId": 1,
  "enrolledAt": "2026-01-21T10:30:00.123456"
}
```

---

## 🗑️ PASO 3: Desinscribir Usuario de Bootcamp

```http
DELETE http://localhost:8082/capacity/bootcamp/1/unenroll
Authorization: Bearer TU_TOKEN_AQUI
```

**⚠️ NOTA:** 
- Ya NO incluyas `/user/{userId}` en la URL
- El userId se obtiene del token JWT
- Solo puedes desinscribirte a ti mismo

**Respuesta esperada (200 OK):**
```json
{
  "code": "200",
  "message": "User unenrolled successfully",
  "identifier": "abc-123",
  "date": "2026-01-21T10:35:00"
}
```

---

## 📚 PASO 4: Obtener Mis Bootcamps

```http
GET http://localhost:8082/capacity/bootcamp/my-bootcamps
Authorization: Bearer TU_TOKEN_AQUI
```

**⚠️ NOTA:** 
- Ya NO incluyas `/user/{userId}` en la URL
- El userId se obtiene del token JWT
- Solo ves tus propios bootcamps

**Respuesta esperada (200 OK):**
```json
[
  {
    "id": 1,
    "name": "Bootcamp Java Backend",
    "description": "Desarrollo backend con Spring Boot",
    "launchDate": "2026-03-01",
    "duration": 90,
    "capacityIds": [1, 2, 3]
  }
]
```

---

## 🎯 Configuración en Postman

### Opción 1: Variables de Entorno (Recomendado)

1. Crear una **Environment** llamada "Bootcamp Dev"
2. Agregar variables:

```
capacity_api_url = http://localhost:8082
bootcamp_api_url = http://localhost:8080
users_api_url = http://localhost:8081
auth_token = (dejar vacío, se llenará después del login)
```

3. En el request de LOGIN, agregar en **Tests**:

```javascript
if (pm.response.code === 200) {
    var jsonData = pm.response.json();
    pm.environment.set("auth_token", jsonData.token);
}
```

4. Usar en otros requests:
```
Authorization: Bearer {{auth_token}}
```

### Opción 2: Token Manual

1. Copia el token del response de login
2. En cada request de enrollment:
   - Pestaña **Authorization**
   - Type: **Bearer Token**
   - Token: Pega tu token aquí

---

## 🧪 Escenarios de Prueba Completos

### ✅ Escenario 1: Flujo Exitoso Completo

**Paso 1: Crear usuario y hacer login**
```http
POST http://localhost:8081/users
Content-Type: application/json

{
  "name": "María García",
  "email": "maria.garcia@example.com",
  "password": "Pass123!",
  "isAdmin": false
}
```

```http
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "email": "maria.garcia@example.com",
  "password": "Pass123!"
}
```
→ Guardar token

**Paso 2: Crear bootcamp (sin autenticación requerida)**
```http
POST http://localhost:8082/capacity/bootcamp
Content-Type: application/json

{
  "name": "Bootcamp React 2026",
  "description": "Desarrollo frontend moderno con React y TypeScript",
  "launchDate": "2026-03-01",
  "duration": 90,
  "capacityIds": [1, 2, 3]
}
```

**Paso 3: Inscribirse en el bootcamp**
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer TU_TOKEN_DE_MARIA

{
  "bootcampId": 1
}
```

**Paso 4: Ver mis bootcamps**
```http
GET http://localhost:8082/capacity/bootcamp/my-bootcamps
Authorization: Bearer TU_TOKEN_DE_MARIA
```

**Paso 5: Desinscribirse**
```http
DELETE http://localhost:8082/capacity/bootcamp/1/unenroll
Authorization: Bearer TU_TOKEN_DE_MARIA
```

---

### ❌ Escenario 2: Errores Comunes

#### Error 1: Sin Token
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json

{
  "bootcampId": 1
}
```
**Respuesta:** 401 Unauthorized

#### Error 2: Token Inválido
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer token_invalido_123

{
  "bootcampId": 1
}
```
**Respuesta:** 401 Unauthorized - "Invalid authentication token"

#### Error 3: Token Expirado
**Respuesta:** 401 Unauthorized - "Authentication token has expired"
**Solución:** Hacer login nuevamente

#### Error 4: Admin intentando inscribirse
```http
POST http://localhost:8081/auth/login
Content-Type: application/json

{
  "email": "admin@example.com",
  "password": "admin123"
}
```
Luego:
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TOKEN_DE_ADMIN

{
  "bootcampId": 1
}
```
**Respuesta:** 403 Forbidden - "You are not authorized to perform this action"

#### Error 5: Usuario Ya Inscrito
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TU_TOKEN

{
  "bootcampId": 1
}
```
Primera vez: ✅ 201 Created
Segunda vez: ❌ 400 Bad Request - "User is already enrolled in this bootcamp"

---

### 📊 Escenario 3: Límite de 5 Bootcamps

```http
POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TU_TOKEN
{"bootcampId": 1}

POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TU_TOKEN
{"bootcampId": 2}

POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TU_TOKEN
{"bootcampId": 3}

POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TU_TOKEN
{"bootcampId": 4}

POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TU_TOKEN
{"bootcampId": 5}

# El 6to fallará
POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TU_TOKEN
{"bootcampId": 6}
```
**Respuesta 6to:** 400 Bad Request - "User cannot enroll in more than 5 bootcamps"

---

### 📅 Escenario 4: Conflicto de Fechas

**Crear Bootcamp A:**
```http
POST http://localhost:8082/capacity/bootcamp
Content-Type: application/json

{
  "name": "Bootcamp Java",
  "description": "Java desde cero",
  "launchDate": "2026-03-01",
  "duration": 90,
  "capacityIds": [1, 2]
}
```

**Crear Bootcamp B (se solapa con A):**
```http
POST http://localhost:8082/capacity/bootcamp
Content-Type: application/json

{
  "name": "Bootcamp Python",
  "description": "Python avanzado",
  "launchDate": "2026-04-15",
  "duration": 90,
  "capacityIds": [3, 4]
}
```

**Inscribirse en A:**
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TU_TOKEN
{"bootcampId": 1}
```
✅ Success

**Intentar inscribirse en B:**
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Authorization: Bearer TU_TOKEN
{"bootcampId": 2}
```
❌ 400 Bad Request - "User is already enrolled in a bootcamp with conflicting dates"

**Cálculo:**
- Bootcamp A: 2026-03-01 a 2026-05-30
- Bootcamp B: 2026-04-15 a 2026-07-14
- **Solapan del 2026-04-15 al 2026-05-30**

---

## 📝 Colección Postman Completa

### Estructura Recomendada

```
Bootcamp System/
├── 0. Auth/
│   ├── Create User
│   └── Login (guarda token automáticamente)
├── 1. Bootcamps (Public)/
│   ├── Create Bootcamp
│   ├── List Bootcamps
│   └── Delete Bootcamp
├── 2. Enrollment (Authenticated)/
│   ├── Enroll in Bootcamp
│   ├── Get My Bootcamps
│   └── Unenroll from Bootcamp
└── 3. Error Cases/
    ├── Without Token
    ├── Invalid Token
    ├── Already Enrolled
    ├── Max 5 Bootcamps
    └── Date Conflict
```

---

## 🔑 Script Pre-request para Auto-Refresh Token

Si quieres auto-refrescar el token antes de cada request:

```javascript
// En Pre-request Script de la colección
const loginUrl = pm.environment.get("users_api_url") + "/auth/login";

pm.sendRequest({
    url: loginUrl,
    method: 'POST',
    header: {
        'Content-Type': 'application/json'
    },
    body: {
        mode: 'raw',
        raw: JSON.stringify({
            email: "juan.perez@example.com",
            password: "SecurePass123!"
        })
    }
}, function (err, response) {
    if (!err && response.code === 200) {
        const jsonData = response.json();
        pm.environment.set("auth_token", jsonData.token);
    }
});
```

---

## ✅ Checklist de Pruebas

- [ ] **Auth**
  - [ ] Crear usuario exitosamente
  - [ ] Login exitoso y obtener token
  - [ ] Login con credenciales incorrectas

- [ ] **Enrollment con JWT**
  - [ ] Inscribirse exitosamente con token válido
  - [ ] Error al inscribirse sin token
  - [ ] Error al inscribirse con token inválido
  - [ ] Error al inscribirse con token expirado
  - [ ] Error cuando admin intenta inscribirse

- [ ] **Reglas de Negocio**
  - [ ] Error: Usuario ya inscrito
  - [ ] Error: Máximo 5 bootcamps
  - [ ] Error: Conflicto de fechas
  - [ ] Éxito: Inscripción sin conflictos

- [ ] **Desinscripción**
  - [ ] Desinscribirse exitosamente
  - [ ] Error al desinscribirse sin estar inscrito

- [ ] **Consulta**
  - [ ] Ver mis bootcamps exitosamente
  - [ ] Lista vacía si no estoy inscrito

---

## 🎯 Tips Importantes

1. **Token JWT es obligatorio** para:
   - POST `/capacity/bootcamp/enroll`
   - DELETE `/capacity/bootcamp/{id}/unenroll`
   - GET `/capacity/bootcamp/my-bootcamps`

2. **NO necesitas token** para:
   - Crear bootcamps
   - Listar bootcamps
   - Eliminar bootcamps
   - Crear usuarios
   - Login

3. **El userId se extrae del token** - NO lo envíes manualmente

4. **Solo USERS pueden inscribirse** - ADMIN no puede

5. **Token expira en 1 hora** - Haz login nuevamente si expira

6. **Usa el mismo secret JWT** en todos los microservicios:
   ```
   jwt.secret: mySecretKeyForJWT2026ThisIsA32CharacterKeyMinimumForHS256Algorithm
   ```

---

## 🚨 Solución de Problemas

### Problema: 403 Forbidden "CSRF token cannot be found"
**Solución:** Ya está resuelto con la nueva configuración de Spring Security que deshabilita CSRF.

### Problema: 401 Unauthorized
**Causa:** Token missing, inválido o expirado
**Solución:** 
1. Verifica que el header Authorization esté presente
2. Verifica formato: `Bearer TU_TOKEN`
3. Haz login nuevamente para obtener nuevo token

### Problema: Token no se guarda en Postman
**Solución:** Agrega script en Tests del request de login:
```javascript
pm.environment.set("auth_token", pm.response.json().token);
```

### Problema: "User not found"
**Causa:** El userId en el token no existe en la BD
**Solución:** Crea el usuario primero o usa un usuario existente

---

## 📊 Resumen de URLs Actualizadas

| Acción | Método | URL | Auth |
|--------|--------|-----|------|
| **Login** | POST | `/auth/login` | ❌ No |
| **Crear Usuario** | POST | `/users` | ❌ No |
| **Crear Bootcamp** | POST | `/capacity/bootcamp` | ❌ No |
| **Listar Bootcamps** | GET | `/capacity/bootcamp` | ❌ No |
| **Inscribirse** | POST | `/capacity/bootcamp/enroll` | ✅ Sí (USER) |
| **Desinscribirse** | DELETE | `/capacity/bootcamp/{id}/unenroll` | ✅ Sí (USER) |
| **Mis Bootcamps** | GET | `/capacity/bootcamp/my-bootcamps` | ✅ Sí (USER) |

**Base URL:** `http://localhost:8082` (Capacity-API)

---

¡Listo! Ahora puedes probar todos los endpoints en Postman con autenticación JWT correctamente configurada. 🚀
