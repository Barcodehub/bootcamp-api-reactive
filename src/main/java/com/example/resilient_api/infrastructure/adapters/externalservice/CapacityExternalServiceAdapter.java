package com.example.resilient_api.infrastructure.adapters.externalservice;

import com.example.resilient_api.domain.model.CapacitySummary;
import com.example.resilient_api.domain.spi.CapacityExternalServicePort;
import com.example.resilient_api.infrastructure.adapters.webclient.CapacityWebClient;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class CapacityExternalServiceAdapter implements CapacityExternalServicePort {

    private final CapacityWebClient capacityWebClient;

    @Override
    public Mono<Map<Long, Boolean>> checkCapacitiesExist(List<Long> capacityIds, String messageId) {
        return capacityWebClient.checkCapacitiesExist(capacityIds, messageId);
    }

    @Override
    public Flux<CapacitySummary> getCapacitiesByIds(List<Long> capacityIds, String messageId) {
        return capacityWebClient.getCapacitiesByIds(capacityIds, messageId);
    }

    @Override
    public Flux<CapacitySummary> getCapacitiesWithTechnologies(List<Long> capacityIds, String messageId) {
        return capacityWebClient.getCapacitiesWithTechnologies(capacityIds, messageId);
    }

    @Override
    public Mono<Void> deleteCapacitiesByIds(List<Long> capacityIds, String messageId) {
        return capacityWebClient.deleteCapacitiesByIds(capacityIds, messageId);
    }
}
