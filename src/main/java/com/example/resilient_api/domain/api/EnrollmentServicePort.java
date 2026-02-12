package com.example.resilient_api.domain.api;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.BootcampEnrollment;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EnrollmentServicePort {
    Mono<BootcampEnrollment> enrollUserInBootcamp(Long bootcampId, Long userId, String messageId);
    Mono<Void> unenrollUserFromBootcamp(Long bootcampId, Long userId, String messageId);
    Flux<Bootcamp> getUserBootcamps(Long userId, String messageId);
    Flux<Long> getUserIdsByBootcampId(Long bootcampId, String messageId);
}
