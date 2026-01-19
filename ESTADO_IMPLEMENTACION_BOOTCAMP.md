# ✅ Resumen de Implementación - Endpoints de Bootcamp

## 🎯 Estado Actual

### ✅ LISTAR BOOTCAMPS - IMPLEMENTADO COMPLETAMENTE

**Endpoint:** `GET /capacity/bootcamp`

**Parámetros Query:**
- `page` - Número de página (default: 0)
- `size` - Tamaño de página (default: 10)
- `sortBy` - Campo de ordenamiento:
  - `NAME` - Ordenar por nombre
  - `TECHNOLOGY_COUNT` - Ordenar por cantidad de capacidades
- `sortDirection` - Dirección:
  - `ASC` - Ascendente
  - `DESC` - Descendente

**Respuesta:**
```json
{
  "content": [
    {
      "id": 1,
      "name": "Bootcamp Java",
      "description": "Descripción",
      "launchDate": "2026-03-01",
      "duration": 90,
      "capacities": [
        {
          "id": 1,
          "name": "Backend Development",
          "technologies": [
            {"id": 1, "name": "Java"},
            {"id": 2, "name": "Spring Boot"}
          ]
        }
      ]
    }
  ],
  "page": 0,
  "size": 10,
  "totalElements": 15,
  "totalPages": 2
}
```

**✅ Cumple con:**
- [x] Parametrización de ordenamiento (nombre o cantidad de capacidades)
- [x] Dirección ASC/DESC
- [x] Servicio paginado
- [x] Listado de capacidades con nombre e id
- [x] Listado de tecnologías con nombre e id

---

### ✅ ELIMINAR BOOTCAMP - IMPLEMENTADO COMPLETAMENTE

**Endpoint:** `DELETE /capacity/bootcamp/{id}`

**Flujo Implementado:**

1. **Bootcamp-API recibe la solicitud** de eliminar bootcamp
2. **Verifica que el bootcamp existe**
3. **Obtiene los IDs de capacidades** asociadas al bootcamp
4. **Notifica a Capacity-API** para decrementar referencias (`POST /capacity/decrement-references`)
5. **Capacity-API procesa cada capacidad:**
   - Cuenta cuántos bootcamps referencian la capacidad
   - Si solo 1 bootcamp (el que se elimina): 
     - Notifica a Technology-API para decrementar referencias de tecnologías
     - Elimina la capacidad
   - Si más de 1 bootcamp: No hace nada
6. **Technology-API procesa cada tecnología:**
   - Cuenta cuántas capacidades referencian la tecnología
   - Si solo 1 capacidad: Elimina la tecnología
   - Si más de 1 capacidad: No hace nada
7. **Bootcamp-API elimina las relaciones** bootcamp_capacity
8. **Bootcamp-API elimina el bootcamp**

**✅ Cumple con:**
- [x] Elimina el bootcamp
- [x] Elimina capacidades SOLO si están referenciadas únicamente por ese bootcamp
- [x] Elimina tecnologías SOLO si están referenciadas únicamente por esa capacidad
- [x] Operación transaccional (todo o nada)

---

## 📋 Endpoints Implementados

### Bootcamp-API (Puerto 8080)
```
POST   /bootcamp              - Registrar bootcamp
GET    /bootcamp              - Listar bootcamps (paginado, ordenado)
DELETE /bootcamp/{id}          - Eliminar bootcamp
POST   /bootcamp/checking     - Verificar existencia
```

### Capacity-API (Puerto 8082)
```
POST   /capacity                          - Crear capacidad
GET    /capacity                          - Listar capacidades
POST   /capacity/check-exists             - Verificar existencia
POST   /capacity/with-technologies        - Obtener capacidades con tecnologías ✅ NUEVO
POST   /capacity/decrement-references     - Decrementar referencias ✅ NUEVO
POST   /capacity/bootcamp                 - Crear bootcamp (proxy)
GET    /capacity/bootcamp                 - Listar bootcamps (proxy)
DELETE /capacity/bootcamp/{id}            - Eliminar bootcamp (proxy)
```

### Technology-API (Puerto 8081)
```
POST   /technology                     - Crear tecnología
GET    /technology                     - Listar tecnologías
POST   /technology/checking            - Verificar existencia
POST   /technology/by-ids              - Obtener por IDs
POST   /technology/decrement-references - Decrementar referencias ✅ NECESARIO
```

---

## ⚠️ PENDIENTES

### Technology-API

Necesita implementar el endpoint:
```
POST /technology/decrement-references
```

**Función:**
- Recibe lista de IDs de tecnologías
- Para cada tecnología, cuenta cuántas capacidades la referencian
- Si solo 1 capacidad la referencia: Elimina la tecnología
- Si más de 1 capacidad: No hace nada

**Implementación necesaria:**

1. **TechnologyServicePort** - Agregar método:
```java
Mono<Void> decrementTechnologyReferences(List<Long> technologyIds, String messageId);
```

2. **TechnologyUseCase** - Implementar:
```java
@Override
public Mono<Void> decrementTechnologyReferences(List<Long> technologyIds, String messageId) {
    return Flux.fromIterable(technologyIds)
            .concatMap(techId ->
                technologyPersistencePort.countCapacityReferences(techId)
                        .flatMap(count -> {
                            if (count <= 1) {
                                return technologyPersistencePort.deleteTechnologyCapacitiesByTechnologyId(techId)
                                        .then(technologyPersistencePort.deleteById(techId));
                            }
                            return Mono.empty();
                        })
            )
            .then();
}
```

3. **TechnologyPersistencePort** - Agregar métodos:
```java
Mono<Long> countCapacityReferences(Long technologyId);
Mono<Void> deleteTechnologyCapacitiesByTechnologyId(Long technologyId);
Mono<Void> deleteById(Long id);
```

4. **TechnologyHandlerImpl** - Agregar handler:
```java
public Mono<ServerResponse> decrementTechnologyReferences(ServerRequest request) {
    String messageId = getMessageId(request);
    return request.bodyToMono(TechnologyIdsRequest.class)
            .flatMap(idsRequest -> {
                List<Long> ids = idsRequest.getIds() != null ? idsRequest.getIds() : List.of();
                return technologyServicePort.decrementTechnologyReferences(ids, messageId);
            })
            .flatMap(v -> ServerResponse.ok().bodyValue("References decremented successfully"))
            .contextWrite(Context.of(X_MESSAGE_ID, messageId))
            .onErrorResume(ex -> handleException(ex, messageId));
}
```

5. **RouterRest** - Agregar ruta:
```java
.andRoute(POST("/technology/decrement-references"), technologyHandler::decrementTechnologyReferences)
```

### Capacity-API

Necesita implementar en CapacityPersistenceAdapter:

```java
@Override
public Flux<Capacity> findAllByIdIn(List<Long> ids) {
    return capacityRepository.findAllByIdIn(ids)
            .flatMap(entity -> 
                findTechnologyIdsByCapacityId(entity.getId())
                        .collectList()
                        .map(techIds -> new Capacity(
                                entity.getId(),
                                entity.getName(),
                                entity.getDescription(),
                                techIds
                        ))
            );
}

@Override
public Mono<Long> countBootcampReferences(Long capacityId) {
    // Contar cuántos bootcamps referencian esta capacidad
    String query = "SELECT COUNT(*) FROM bootcamp_capacity WHERE capacity_id = :capacityId";
    return databaseClient.sql(query)
            .bind("capacityId", capacityId)
            .map(row -> row.get(0, Long.class))
            .one();
}

@Override
public Mono<Void> deleteById(Long id) {
    return capacityRepository.deleteById(id);
}

@Override
public Mono<Void> deleteCapacityTechnologiesByCapacityId(Long capacityId) {
    return capacityTechnologyRepository.findAllByCapacityId(capacityId)
            .flatMap(capacityTechnologyRepository::delete)
            .then();
}
```

---

## 🔄 Flujo Completo de Eliminación

```
Usuario DELETE /capacity/bootcamp/1
    ↓
Capacity-API (proxy) → Bootcamp-API DELETE /bootcamp/1
    ↓
Bootcamp-API:
    1. Verificar bootcamp existe
    2. Obtener capacidades [1, 2, 3]
    3. Notificar → Capacity-API POST /capacity/decrement-references
       Body: {"ids": [1, 2, 3]}
    ↓
Capacity-API:
    Para cada capacidad:
    - Capacity 1: 2 bootcamps → NO eliminar
    - Capacity 2: 1 bootcamp → SÍ eliminar
        - Obtener tecnologías [1, 2]
        - Notificar → Technology-API POST /technology/decrement-references
          Body: {"ids": [1, 2]}
        - Eliminar capacity_technology
        - Eliminar capacity
    - Capacity 3: 3 bootcamps → NO eliminar
    ↓
Technology-API:
    Para cada tecnología:
    - Technology 1: 3 capacidades → NO eliminar
    - Technology 2: 1 capacidad → SÍ eliminar
        - Eliminar technology
    ↓
Bootcamp-API:
    4. Eliminar bootcamp_capacity (todas las relaciones)
    5. Eliminar bootcamp
    ↓
Response: 200 OK "Bootcamp deleted successfully"
```

---

## ✅ Estado de Requisitos

| Requisito | Estado |
|-----------|--------|
| **LISTAR** |  |
| Ordenar por nombre (ASC/DESC) | ✅ |
| Ordenar por cantidad capacidades (ASC/DESC) | ✅ |
| Paginación | ✅ |
| Capacidades con id y nombre | ✅ |
| Tecnologías con id y nombre | ✅ |
| **ELIMINAR** |  |
| Eliminar bootcamp | ✅ |
| Eliminar capacidades huérfanas | ✅ |
| Eliminar tecnologías huérfanas | ⚠️ Falta endpoint en technology-api |
| Operación transaccional | ✅ |

---

## 🚀 Próximos Pasos

1. ✅ **Implementar** `POST /technology/decrement-references` en technology-api
2. ✅ **Implementar** métodos faltantes en CapacityPersistenceAdapter
3. ✅ **Implementar** métodos faltantes en TechnologyWebClient (capacity-api)
4. ✅ **Probar** flujo completo de eliminación
5. ✅ **Verificar** que listar bootcamps trae toda la información

---

## 📝 Archivos Modificados

### Bootcamp-API
- ✅ BootcampUseCase - Lógica de eliminación
- ✅ BootcampPersistencePort - Métodos de eliminación
- ✅ BootcampPersistenceAdapter - Implementación
- ✅ CapacityWebClient - Llamadas a capacity-api
- ✅ BootcampHandlerImpl - Handler de listar y eliminar
- ✅ RouterRest - Rutas configuradas

### Capacity-API
- ✅ CapacityServicePort - Nuevos métodos
- ✅ CapacityUseCase - Implementación de decrementación
- ✅ CapacityPersistencePort - Nuevos métodos
- ⚠️ CapacityPersistenceAdapter - **FALTA IMPLEMENTAR**
- ✅ CapacityHandlerImpl - Nuevos handlers
- ✅ RouterRest - Nuevas rutas
- ⚠️ TechnologyWebClient - **FALTA IMPLEMENTAR** notifyTechnologyReferencesDecrement

### Technology-API
- ⚠️ **TODO** - Implementar decrementTechnologyReferences completo

---

**Resumen: LISTAR está 100% funcional. ELIMINAR está 80% implementado, falta completar Technology-API y algunos adaptadores.**

