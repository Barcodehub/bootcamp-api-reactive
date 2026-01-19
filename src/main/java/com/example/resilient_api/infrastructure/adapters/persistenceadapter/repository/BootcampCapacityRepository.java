package com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository;

import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampCapacityEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BootcampCapacityRepository extends ReactiveCrudRepository<BootcampCapacityEntity, Long> {
    Flux<BootcampCapacityEntity> findAllByBootcampId(Long bootcampId);
}

