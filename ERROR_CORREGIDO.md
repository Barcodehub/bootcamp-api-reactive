# ✅ Error Corregido - Capacity-API

## 🐛 Error Original

```
error: incompatible types: Mono<PageResponse> cannot be converted to Mono<PageResponse<BootcampWithCapacitiesDTO>>
                .onErrorResume(ex -> {
                              ^
```

**Ubicación:** `BootcampWebClient.java`, línea 83

**Causa:** El método `.bodyToMono(PageResponse.class)` perdía la información del tipo genérico en tiempo de ejecución.

---

## ✅ Solución Aplicada

### 1. Agregar Import
```java
import org.springframework.core.ParameterizedTypeReference;
```

### 2. Usar ParameterizedTypeReference
**Antes:**
```java
.bodyToMono(PageResponse.class)
```

**Después:**
```java
.bodyToMono(new ParameterizedTypeReference<PageResponse<BootcampWithCapacitiesDTO>>() {})
```

---

## 📝 Explicación Técnica

### ¿Por qué falla con `.class`?

Cuando usas `PageResponse.class`, Java borra (type erasure) la información del tipo genérico en tiempo de ejecución. El WebClient solo ve `PageResponse` sin saber que contiene `BootcampWithCapacitiesDTO`.

### ¿Cómo funciona ParameterizedTypeReference?

`ParameterizedTypeReference` es una clase abstracta de Spring que captura la información del tipo genérico usando reflexión. Al crear una clase anónima:

```java
new ParameterizedTypeReference<PageResponse<BootcampWithCapacitiesDTO>>() {}
```

Java mantiene la información del tipo genérico en los metadatos de la clase, permitiendo que Jackson deserialice correctamente el JSON a `PageResponse<BootcampWithCapacitiesDTO>`.

---

## 🔧 Código Completo Corregido

```java
public Mono<PageResponse<BootcampWithCapacitiesDTO>> listBootcamps(
        int page, int size, String sortBy, String sortDirection, String messageId) {
    
    log.info("Calling bootcamp service to list bootcamps with messageId: {}", messageId);

    String uri = String.format("%s/bootcamp?page=%d&size=%d&sortBy=%s&sortDirection=%s",
            bootcampBaseUrl, page, size, sortBy, sortDirection);

    return webClientBuilder.build()
            .get()
            .uri(uri)
            .header(X_MESSAGE_ID, messageId)
            .retrieve()
            .onStatus(status -> status.is5xxServerError(),
                response -> {
                    log.error("Bootcamp service returned 5xx error for messageId: {}", messageId);
                    return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                })
            .onStatus(status -> status.is4xxClientError(),
                response -> {
                    log.error("Bootcamp service returned 4xx error for messageId: {}", messageId);
                    return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                })
            // ✅ CORRECCIÓN: Usar ParameterizedTypeReference
            .bodyToMono(new ParameterizedTypeReference<PageResponse<BootcampWithCapacitiesDTO>>() {})
            .doOnSuccess(result -> log.info("Successfully listed bootcamps with messageId: {}", messageId))
            .doOnError(ex -> log.error("Error calling bootcamp service for messageId: {}", messageId, ex))
            .onErrorResume(ex -> {
                if (ex instanceof TechnicalException) {
                    return Mono.error(ex);
                }
                log.error("Unexpected error calling bootcamp service for messageId: {}", messageId, ex);
                return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
            });
}
```

---

## ✅ Estado Actual

- **Error:** Corregido ✅
- **Compilación Bootcamp-API:** BUILD SUCCESSFUL ✅
- **Compilación Capacity-API:** BUILD SUCCESSFUL ✅
- **Archivos Modificados:** 1 (`BootcampWebClient.java`)
- **Líneas Modificadas:** 2 (1 import + 1 cambio en bodyToMono)

---

## 🎓 Lecciones Aprendidas

1. **Type Erasure en Java:**
   - Los genéricos son solo en tiempo de compilación
   - En runtime, `PageResponse<T>` se convierte en `PageResponse`

2. **ParameterizedTypeReference:**
   - Solución de Spring para capturar tipos genéricos
   - Usa reflexión para mantener información de tipos
   - Esencial para deserialización correcta de estructuras anidadas

3. **WebClient Reactivo:**
   - `.bodyToMono(Class)` → Para tipos simples
   - `.bodyToMono(ParameterizedTypeReference)` → Para tipos genéricos

---

## 📚 Referencias

- [Spring Documentation - ParameterizedTypeReference](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/core/ParameterizedTypeReference.html)
- [Java Type Erasure](https://docs.oracle.com/javase/tutorial/java/generics/erasure.html)
- [WebClient Body Extractors](https://docs.spring.io/spring-framework/reference/web/webflux-webclient/client-body.html)

---

**✅ Corrección aplicada exitosamente. El proyecto está listo para compilar y ejecutar.**

