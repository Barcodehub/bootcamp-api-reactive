package com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository;

import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface BootcampRepository extends ReactiveCrudRepository<BootcampEntity, Long> {
    Mono<BootcampEntity> findByName(String name);
    Flux<BootcampEntity> findAllByIdIn(List<Long> ids);
}

