package com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository;

import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampUserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampUserRepository extends ReactiveCrudRepository<BootcampUserEntity, Long> {

    @Query("SELECT COUNT(*) FROM bootcamp_user WHERE user_id = :userId")
    Mono<Long> countByUserId(Long userId);

    @Query("SELECT * FROM bootcamp_user WHERE user_id = :userId")
    Flux<BootcampUserEntity> findByUserId(Long userId);

    @Query("SELECT * FROM bootcamp_user WHERE bootcamp_id = :bootcampId AND user_id = :userId")
    Mono<BootcampUserEntity> findByBootcampIdAndUserId(Long bootcampId, Long userId);

    @Query("DELETE FROM bootcamp_user WHERE bootcamp_id = :bootcampId AND user_id = :userId")
    Mono<Void> deleteByBootcampIdAndUserId(Long bootcampId, Long userId);

    @Query("SELECT * FROM bootcamp_user WHERE bootcamp_id = :bootcampId")
    Flux<BootcampUserEntity> findByBootcampId(Long bootcampId);
}
