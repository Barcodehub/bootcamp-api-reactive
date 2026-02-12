package com.example.resilient_api.domain.api;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.BootcampWithCapacities;
import com.example.resilient_api.domain.model.Page;
import com.example.resilient_api.domain.model.PaginationRequest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface BootcampServicePort {
    Mono<Bootcamp> registerBootcamp(Bootcamp bootcamp, String messageId);
    Mono<Map<Long, Boolean>> checkBootcampsExist(List<Long> ids, String messageId);
    Mono<Page<BootcampWithCapacities>> listBootcamps(PaginationRequest paginationRequest, String messageId);
    Mono<BootcampWithCapacities> getBootcampById(Long id, String messageId);
    Mono<Void> deleteBootcamp(Long id, String messageId);
}

