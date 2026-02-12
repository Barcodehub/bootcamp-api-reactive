package com.example.resilient_api.infrastructure.adapters.webclient;

import com.example.resilient_api.domain.exceptions.TechnicalException;
import com.example.resilient_api.domain.model.CapacitySummary;
import com.example.resilient_api.infrastructure.adapters.webclient.dto.CapacityExistsResponse;
import com.example.resilient_api.infrastructure.adapters.webclient.dto.CapacityIdsRequest;
import com.example.resilient_api.infrastructure.adapters.webclient.dto.CapacitySummaryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.example.resilient_api.domain.enums.TechnicalMessage.TECHNOLOGY_SERVICE_ERROR;
import static com.example.resilient_api.infrastructure.entrypoints.util.Constants.X_MESSAGE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CapacityWebClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${external.capacity.base-url}")
    private String capacityBaseUrl;

    public Mono<Map<Long, Boolean>> checkCapacitiesExist(List<Long> capacityIds, String messageId) {
        log.info("Calling capacity service to check capacities exist with messageId: {}", messageId);

        return webClientBuilder.build()
                .post()
                .uri(capacityBaseUrl + "/capacity/check-exists")
                .header(X_MESSAGE_ID, messageId)
                .bodyValue(new CapacityIdsRequest(capacityIds))
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                    response -> {
                        log.error("Capacity service returned 5xx error for messageId: {}", messageId);
                        return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                    })
                .onStatus(status -> status.is4xxClientError(),
                    response -> {
                        log.error("Capacity service returned 4xx error for messageId: {}", messageId);
                        return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                    })
                .bodyToMono(CapacityExistsResponse.class)
                .map(CapacityExistsResponse::getExists)
                .doOnSuccess(result -> log.info("Successfully received response from capacity service with messageId: {}", messageId))
                .doOnError(ex -> log.error("Error calling capacity service for messageId: {}", messageId, ex))
                .onErrorResume(ex -> {
                    if (ex instanceof TechnicalException) {
                        return Mono.error(ex);
                    }
                    log.error("Unexpected error calling capacity service for messageId: {}", messageId, ex);
                    return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                });
    }

    public Flux<CapacitySummary> getCapacitiesByIds(List<Long> capacityIds, String messageId) {
        log.info("Calling capacity service to get capacities by ids with messageId: {}", messageId);

        return webClientBuilder.build()
                .post()
                .uri(capacityBaseUrl + "/capacity/by-ids")
                .header(X_MESSAGE_ID, messageId)
                .bodyValue(new CapacityIdsRequest(capacityIds))
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                    response -> {
                        log.error("Capacity service returned 5xx error for messageId: {}", messageId);
                        return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                    })
                .onStatus(status -> status.is4xxClientError(),
                    response -> {
                        log.error("Capacity service returned 4xx error for messageId: {}", messageId);
                        return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                    })
                .bodyToFlux(CapacitySummaryResponse.class)
                .map(response -> new CapacitySummary(response.getId(), response.getName(), List.of()))
                .doOnComplete(() -> log.info("Successfully received capacities from capacity service with messageId: {}", messageId))
                .doOnError(ex -> log.error("Error calling capacity service for messageId: {}", messageId, ex))
                .onErrorResume(ex -> {
                    if (ex instanceof TechnicalException) {
                        return Flux.error(ex);
                    }
                    log.error("Unexpected error calling capacity service for messageId: {}", messageId, ex);
                    return Flux.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                });
    }

    public Flux<CapacitySummary> getCapacitiesWithTechnologies(List<Long> capacityIds, String messageId) {
        log.info("Calling capacity service to get capacities with technologies by ids with messageId: {}", messageId);

        return webClientBuilder.build()
                .post()
                .uri(capacityBaseUrl + "/capacity/with-technologies")
                .header(X_MESSAGE_ID, messageId)
                .bodyValue(new CapacityIdsRequest(capacityIds))
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                    response -> {
                        log.error("Capacity service returned 5xx error for messageId: {}", messageId);
                        return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                    })
                .onStatus(status -> status.is4xxClientError(),
                    response -> {
                        log.error("Capacity service returned 4xx error for messageId: {}", messageId);
                        return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                    })
                .bodyToFlux(CapacitySummaryResponse.class)
                .map(response -> new CapacitySummary(
                        response.getId(),
                        response.getName(),
                        response.getTechnologies()
                ))
                .doOnComplete(() -> log.info("Successfully received capacities with technologies from capacity service with messageId: {}", messageId))
                .doOnError(ex -> log.error("Error calling capacity service for messageId: {}", messageId, ex))
                .onErrorResume(ex -> {
                    if (ex instanceof TechnicalException) {
                        return Flux.error(ex);
                    }
                    log.error("Unexpected error calling capacity service for messageId: {}", messageId, ex);
                    return Flux.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                });
    }

    public Mono<Void> deleteCapacitiesByIds(List<Long> capacityIds, String messageId) {
        log.info("Calling capacity service to delete capacities by ids with messageId: {}", messageId);

        return webClientBuilder.build()
                .post()
                .uri(capacityBaseUrl + "/capacity/delete-by-ids")
                .header(X_MESSAGE_ID, messageId)
                .bodyValue(new CapacityIdsRequest(capacityIds))
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                    response -> {
                        log.error("Capacity service returned 5xx error for messageId: {}", messageId);
                        return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                    })
                .onStatus(status -> status.is4xxClientError(),
                    response -> {
                        log.error("Capacity service returned 4xx error for messageId: {}", messageId);
                        return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                    })
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Successfully deleted capacities with messageId: {}", messageId))
                .doOnError(ex -> log.error("Error calling capacity service for messageId: {}", messageId, ex))
                .onErrorResume(ex -> {
                    if (ex instanceof TechnicalException) {
                        return Mono.error(ex);
                    }
                    log.error("Unexpected error calling capacity service for messageId: {}", messageId, ex);
                    return Mono.error(new TechnicalException(TECHNOLOGY_SERVICE_ERROR));
                });
    }
}

