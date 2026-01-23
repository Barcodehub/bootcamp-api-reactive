package com.example.resilient_api.domain.spi;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface UserExternalServicePort {
    Mono<Map<Long, Boolean>> checkUsersExist(List<Long> userIds, String messageId);
}
