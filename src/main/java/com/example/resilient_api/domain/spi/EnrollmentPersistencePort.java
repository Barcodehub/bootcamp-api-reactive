package com.example.resilient_api.domain.spi;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.BootcampEnrollment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EnrollmentPersistencePort {
    Mono<BootcampEnrollment> enrollUser(Long bootcampId, Long userId);
    Mono<Void> unenrollUser(Long bootcampId, Long userId);
    Mono<Long> countEnrollmentsByUserId(Long userId);
    Flux<BootcampEnrollment> findEnrollmentsByUserId(Long userId);
    Flux<Bootcamp> findBootcampsByUserId(Long userId);
    Flux<Long> findUserIdsByBootcampId(Long bootcampId);
    Mono<Boolean> isUserEnrolled(Long bootcampId, Long userId);
}
