package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.CapacitySummary;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface CapacityExternalServicePort {
    Mono<Map<Long, Boolean>> checkCapacitiesExist(List<Long> capacityIds, String messageId);
    Flux<CapacitySummary> getCapacitiesByIds(List<Long> capacityIds, String messageId);
    Flux<CapacitySummary> getCapacitiesWithTechnologies(List<Long> capacityIds, String messageId);
    Mono<Void> deleteCapacitiesByIds(List<Long> capacityIds, String messageId);
}

