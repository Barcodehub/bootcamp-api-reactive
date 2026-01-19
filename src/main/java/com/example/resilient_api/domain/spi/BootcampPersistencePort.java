package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.PaginationRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BootcampPersistencePort {
    Mono<Bootcamp> save(Bootcamp bootcamp);
    Mono<Boolean> existByName(String name);
    Flux<Long> findExistingIdsByIds(List<Long> ids);
    Flux<Bootcamp> findAllPaginated(PaginationRequest paginationRequest);
    Mono<Long> count();
    Flux<Long> findCapacityIdsByBootcampId(Long bootcampId);
    Mono<Bootcamp> findById(Long id);
    Mono<Void> deleteById(Long id);
    Mono<Void> deleteBootcampCapacitiesByBootcampId(Long bootcampId);
    Mono<Long> countBootcampsByCapacityId(Long capacityId);
}

