# 🎯 Guía de Prueba: Inscribirse en 5 Bootcamps Simultáneamente

## ✅ Regla de Negocio Implementada

**Máximo 5 bootcamps por usuario al mismo tiempo.**

---

## 🧪 Prueba Completa: Inscribirse en 5 Bootcamps

### Prerrequisitos

1. ✅ Tener un usuario normal (no admin)
2. ✅ Tener un token JWT de ese usuario
3. ✅ Tener 5 bootcamps creados **sin conflictos de fechas**

---

## 📅 Paso 1: Crear 5 Bootcamps Sin Solapamiento de Fechas

Los bootcamps deben tener fechas que NO se solapen. Aquí hay un ejemplo:

### Bootcamp 1: Enero-Marzo 2026
```http
POST http://localhost:8082/capacity/bootcamp
Content-Type: application/json

{
  "name": "Bootcamp Java Backend",
  "description": "Desarrollo backend con Spring Boot",
  "launchDate": "2026-01-15",
  "duration": 60,
  "capacityIds": [1, 2]
}
```
**Fechas:** 2026-01-15 a 2026-03-16 (60 días)

---

### Bootcamp 2: Marzo-Mayo 2026
```http
POST http://localhost:8082/capacity/bootcamp
Content-Type: application/json

{
  "name": "Bootcamp React Frontend",
  "description": "Desarrollo frontend con React",
  "launchDate": "2026-03-20",
  "duration": 60,
  "capacityIds": [3, 4]
}
```
**Fechas:** 2026-03-20 a 2026-05-19 (60 días)

---

### Bootcamp 3: Mayo-Julio 2026
```http
POST http://localhost:8082/capacity/bootcamp
Content-Type: application/json

{
  "name": "Bootcamp DevOps",
  "description": "Automatización y CI/CD",
  "launchDate": "2026-05-25",
  "duration": 60,
  "capacityIds": [5, 6]
}
```
**Fechas:** 2026-05-25 a 2026-07-24 (60 días)

---

### Bootcamp 4: Julio-Septiembre 2026
```http
POST http://localhost:8082/capacity/bootcamp
Content-Type: application/json

{
  "name": "Bootcamp Python Data Science",
  "description": "Análisis de datos con Python",
  "launchDate": "2026-07-30",
  "duration": 60,
  "capacityIds": [7, 8]
}
```
**Fechas:** 2026-07-30 a 2026-09-28 (60 días)

---

### Bootcamp 5: Octubre-Diciembre 2026
```http
POST http://localhost:8082/capacity/bootcamp
Content-Type: application/json

{
  "name": "Bootcamp Cloud AWS",
  "description": "Arquitectura en la nube con AWS",
  "launchDate": "2026-10-05",
  "duration": 60,
  "capacityIds": [9, 10]
}
```
**Fechas:** 2026-10-05 a 2026-12-04 (60 días)

---

## 📝 Paso 2: Inscribirse en los 5 Bootcamps

Usa el **mismo token de usuario normal** en todas las inscripciones.

### Inscripción 1
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_DE_USUARIO_NORMAL>

{
  "bootcampId": 1
}
```
✅ **Resultado esperado:** 201 Created

---

### Inscripción 2
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_DE_USUARIO_NORMAL>

{
  "bootcampId": 2
}
```
✅ **Resultado esperado:** 201 Created

---

### Inscripción 3
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_DE_USUARIO_NORMAL>

{
  "bootcampId": 3
}
```
✅ **Resultado esperado:** 201 Created

---

### Inscripción 4
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_DE_USUARIO_NORMAL>

{
  "bootcampId": 4
}
```
✅ **Resultado esperado:** 201 Created

---

### Inscripción 5
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_DE_USUARIO_NORMAL>

{
  "bootcampId": 5
}
```
✅ **Resultado esperado:** 201 Created

---

## ❌ Paso 3: Intentar Inscribirse en un 6to Bootcamp (Debe Fallar)

### Crear Bootcamp 6
```http
POST http://localhost:8082/capacity/bootcamp
Content-Type: application/json

{
  "name": "Bootcamp Node.js",
  "description": "Backend con Node.js y Express",
  "launchDate": "2027-01-10",
  "duration": 60,
  "capacityIds": [11, 12]
}
```

### Intentar Inscribirse en el 6to
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_DE_USUARIO_NORMAL>

{
  "bootcampId": 6
}
```

❌ **Resultado esperado:** 400 Bad Request

**Respuesta:**
```json
{
  "code": "400",
  "message": "User cannot enroll in more than 5 bootcamps",
  "identifier": "xxx-xxx-xxx",
  "date": "2026-01-21T14:30:00",
  "errors": [
    {
      "code": "400",
      "param": "userId",
      "message": "User cannot enroll in more than 5 bootcamps"
    }
  ]
}
```

---

## 📊 Paso 4: Verificar Mis Bootcamps

```http
GET http://localhost:8082/capacity/bootcamp/my-bootcamps
Authorization: Bearer <TU_TOKEN_DE_USUARIO_NORMAL>
```

**Respuesta esperada:**
```json
[
  {
    "id": 1,
    "name": "Bootcamp Java Backend",
    "description": "Desarrollo backend con Spring Boot",
    "launchDate": "2026-01-15",
    "duration": 60,
    "capacityIds": [1, 2]
  },
  {
    "id": 2,
    "name": "Bootcamp React Frontend",
    "description": "Desarrollo frontend con React",
    "launchDate": "2026-03-20",
    "duration": 60,
    "capacityIds": [3, 4]
  },
  {
    "id": 3,
    "name": "Bootcamp DevOps",
    "description": "Automatización y CI/CD",
    "launchDate": "2026-05-25",
    "duration": 60,
    "capacityIds": [5, 6]
  },
  {
    "id": 4,
    "name": "Bootcamp Python Data Science",
    "description": "Análisis de datos con Python",
    "launchDate": "2026-07-30",
    "duration": 60,
    "capacityIds": [7, 8]
  },
  {
    "id": 5,
    "name": "Bootcamp Cloud AWS",
    "description": "Arquitectura en la nube con AWS",
    "launchDate": "2026-10-05",
    "duration": 60,
    "capacityIds": [9, 10]
  }
]
```

✅ **5 bootcamps en total**

---

## 🔄 Paso 5: Desinscribirse de Uno y Poder Inscribirse en Otro

### Desinscribirse del Bootcamp 1
```http
DELETE http://localhost:8082/capacity/bootcamp/1/unenroll
Authorization: Bearer <TU_TOKEN_DE_USUARIO_NORMAL>
```

✅ **Resultado:** 200 OK

---

### Ahora Inscribirse en el Bootcamp 6
```http
POST http://localhost:8082/capacity/bootcamp/enroll
Content-Type: application/json
Authorization: Bearer <TU_TOKEN_DE_USUARIO_NORMAL>

{
  "bootcampId": 6
}
```

✅ **Resultado:** 201 Created (ahora sí funciona porque solo tienes 4 bootcamps activos)

---

## 🎯 Script de Postman para Prueba Automática

Puedes crear una colección en Postman con este script:

### Collection Pre-request Script
```javascript
// Login automático si no hay token o expiró
if (!pm.environment.get("auth_token")) {
    pm.sendRequest({
        url: pm.environment.get("users_api_url") + "/auth/login",
        method: 'POST',
        header: { 'Content-Type': 'application/json' },
        body: {
            mode: 'raw',
            raw: JSON.stringify({
                email: "user@example.com",
                password: "Pass123!"
            })
        }
    }, function (err, response) {
        if (!err && response.code === 200) {
            pm.environment.set("auth_token", response.json().token);
        }
    });
}
```

### Request: "Inscribirse en 5 Bootcamps" - Tests
```javascript
// Variables para rastrear inscripciones
var enrollmentCount = pm.environment.get("enrollment_count") || 0;

if (pm.response.code === 201) {
    enrollmentCount++;
    pm.environment.set("enrollment_count", enrollmentCount);
    console.log("✅ Inscripción #" + enrollmentCount + " exitosa");
    
    if (enrollmentCount < 5) {
        console.log("📝 Puedes inscribirte en " + (5 - enrollmentCount) + " bootcamps más");
    } else {
        console.log("⚠️ Has alcanzado el límite de 5 bootcamps");
    }
} else if (pm.response.code === 400) {
    console.log("❌ Límite alcanzado. Ya tienes 5 bootcamps activos.");
}
```

---

## 📋 Validaciones del Sistema

El sistema valida automáticamente:

1. ✅ **Usuario existe** (verificación en users-api)
2. ✅ **Bootcamp existe**
3. ✅ **Usuario no está ya inscrito en ese bootcamp**
4. ✅ **Usuario tiene menos de 5 bootcamps activos** ← ESTA VALIDACIÓN
5. ✅ **No hay conflictos de fechas entre bootcamps**

---

## 🔍 ¿Cómo Funciona la Validación?

En `EnrollmentUseCase.java`:

```java
private Mono<Void> validateMaxBootcamps(Long userId) {
    return enrollmentPersistencePort.countEnrollmentsByUserId(userId)
            .flatMap(count -> {
                if (count >= MAX_BOOTCAMPS_PER_USER) { // MAX_BOOTCAMPS_PER_USER = 5
                    log.warn("User {} has reached maximum bootcamp limit: {}", userId, count);
                    return Mono.error(new BusinessException(
                        TechnicalMessage.MAX_BOOTCAMPS_REACHED));
                }
                return Mono.empty();
            });
}
```

Hace una consulta a la BD:
```sql
SELECT COUNT(*) FROM bootcamp_user WHERE user_id = ?
```

Si el resultado es >= 5, rechaza la inscripción.

---

## 🎨 Diagrama de Flujo

```
Usuario intenta inscribirse en bootcamp
    ↓
¿Usuario existe? → No → Error 404
    ↓ Sí
¿Bootcamp existe? → No → Error 404
    ↓ Sí
¿Ya está inscrito? → Sí → Error 400 "Already enrolled"
    ↓ No
¿Tiene < 5 bootcamps? → No → Error 400 "Max 5 bootcamps" ❌
    ↓ Sí
¿Hay conflicto de fechas? → Sí → Error 400 "Date conflict"
    ↓ No
✅ Inscripción exitosa (201 Created)
```

---

## 💡 Tips de Prueba

### Tip 1: Usa Bootcamps Cortos para Pruebas Rápidas
En lugar de 60 días, usa 1 o 2 días:
```json
{
  "launchDate": "2026-01-15",
  "duration": 1
}
```

### Tip 2: Usa Fechas Muy Separadas
Para evitar conflictos de fechas, separa los bootcamps por varios meses.

### Tip 3: Verifica la Base de Datos
Puedes verificar directamente en PostgreSQL:
```sql
-- Ver inscripciones de un usuario
SELECT * FROM bootcamp_user WHERE user_id = 1;

-- Contar inscripciones de un usuario
SELECT COUNT(*) FROM bootcamp_user WHERE user_id = 1;
```

---

## ✅ Checklist de Prueba Completa

- [ ] Crear usuario normal (isAdmin: false)
- [ ] Login y obtener token
- [ ] Crear 5 bootcamps sin solapamiento de fechas
- [ ] Inscribirse en bootcamp 1 (✅ 201)
- [ ] Inscribirse en bootcamp 2 (✅ 201)
- [ ] Inscribirse en bootcamp 3 (✅ 201)
- [ ] Inscribirse en bootcamp 4 (✅ 201)
- [ ] Inscribirse en bootcamp 5 (✅ 201)
- [ ] Verificar: GET /my-bootcamps (5 bootcamps)
- [ ] Crear bootcamp 6
- [ ] Intentar inscribirse en bootcamp 6 (❌ 400 "Max 5 bootcamps")
- [ ] Desinscribirse de bootcamp 1 (✅ 200)
- [ ] Inscribirse en bootcamp 6 (✅ 201)
- [ ] Verificar: GET /my-bootcamps (5 bootcamps: 2,3,4,5,6)

---

## 🎯 Conclusión

✅ **Sí, puedes inscribirte en 5 bootcamps al mismo tiempo**, pero:
- ❌ **NO puedes inscribirte en un 6to** mientras tengas 5 activos
- ✅ **Debes desinscribirte de uno** para poder inscribirte en otro
- ✅ **Los bootcamps no deben solaparse en fechas**

El sistema está funcionando correctamente y aplicando todas las validaciones. 🚀
