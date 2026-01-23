package com.example.resilient_api.infrastructure.adapters.externalservice;

import com.example.resilient_api.domain.spi.UserExternalServicePort;
import com.example.resilient_api.infrastructure.adapters.webclient.UserWebClient;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class UserExternalServiceAdapter implements UserExternalServicePort {

    private final UserWebClient userWebClient;

    @Override
    public Mono<Map<Long, Boolean>> checkUsersExist(List<Long> userIds, String messageId) {
        return userWebClient.checkUsersExist(userIds, messageId);
    }
}
