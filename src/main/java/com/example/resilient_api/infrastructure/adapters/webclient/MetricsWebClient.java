package com.example.resilient_api.infrastructure.adapters.webclient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;

/**
 * Cliente para comunicarse con el microservicio de métricas
 * Permite registrar reportes de bootcamps de forma asíncrona
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsWebClient {

    private static final String X_MESSAGE_ID = "X-Message-Id";
    private final WebClient.Builder webClientBuilder;

    @Value("${external.metrics.base-url:http://localhost:8084}")
    private String metricsBaseUrl;

    /**
     * Registra un reporte de bootcamp de forma asíncrona (Fire and Forget)
     * No espera respuesta ni propaga errores
     *
     * @param bootcampId ID del bootcamp a reportar
     * @param messageId ID del mensaje para trazabilidad
     * @param authToken Token JWT para autenticación
     */
    public void registerBootcampReportAsync(Long bootcampId, String messageId, String authToken) {
        log.info("=== METRICS CLIENT === Starting async bootcamp report registration");
        log.info("=== METRICS CLIENT === BootcampId: {}, MessageId: {}, MetricsURL: {}",
                bootcampId, messageId, metricsBaseUrl);
        log.info("=== METRICS CLIENT === Auth Token present: {}", authToken != null);

        try {
            webClientBuilder.build()
                    .post()
                    .uri(metricsBaseUrl + "/metrics/bootcamp/report")
                    .header(X_MESSAGE_ID, messageId)
                    .header("Authorization", authToken)
                    .bodyValue(new BootcampReportRequest(bootcampId))
                    .retrieve()
                    .bodyToMono(Void.class)
                    .doOnSubscribe(s -> log.info("=== METRICS CLIENT === Subscription started for bootcampId: {}", bootcampId))
                    .doOnSuccess(result -> log.info("=== METRICS CLIENT === SUCCESS - Bootcamp report registered for bootcampId: {} with messageId: {}",
                            bootcampId, messageId))
                    .doOnError(error -> log.error("=== METRICS CLIENT === ERROR - Failed to register bootcamp report for bootcampId: {} with messageId: {}. Error: {}",
                            bootcampId, messageId, error.getMessage(), error))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe(
                            result -> log.info("=== METRICS CLIENT === Subscribe callback - Success for bootcampId: {}", bootcampId),
                            error -> log.error("=== METRICS CLIENT === Subscribe callback - Error for bootcampId: {}: {}",
                                    bootcampId, error.getMessage(), error),
                            () -> log.info("=== METRICS CLIENT === Subscribe callback - Completed for bootcampId: {}", bootcampId)
                    );

            log.info("=== METRICS CLIENT === Async call initiated successfully for bootcampId: {}", bootcampId);
        } catch (Exception ex) {
            log.error("=== METRICS CLIENT === EXCEPTION - Error initiating async call for bootcampId: {}", bootcampId, ex);
        }
    }

    /**
     * DTO interno para el request de registro de reporte
     */
    private record BootcampReportRequest(Long bootcampId) {}
}
