package com.example.resilient_api.infrastructure.adapters.webclient;

import com.example.resilient_api.domain.exceptions.TechnicalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static com.example.resilient_api.domain.enums.TechnicalMessage.USER_SERVICE_ERROR;
import static com.example.resilient_api.infrastructure.entrypoints.util.Constants.X_MESSAGE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserWebClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${external.user.base-url}")
    private String userBaseUrl;

    public Mono<Map<Long, Boolean>> checkUsersExist(List<Long> userIds, String messageId) {
        log.info("Calling user service to check if users exist with messageId: {}", messageId);

        return webClientBuilder.build()
                .post()
                .uri(userBaseUrl + "/users/check-exists")
                .header(X_MESSAGE_ID, messageId)
                .bodyValue(Map.of("ids", userIds))  // Cambiado de "userIds" a "ids"
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                        response -> {
                            log.error("User service returned 5xx error for messageId: {}", messageId);
                            return Mono.error(new TechnicalException(USER_SERVICE_ERROR));
                        })
                .onStatus(status -> status.is4xxClientError(),
                        response -> {
                            log.error("User service returned 4xx error for messageId: {}", messageId);
                            return Mono.error(new TechnicalException(USER_SERVICE_ERROR));
                        })
                .bodyToMono(new ParameterizedTypeReference<Map<Long, Boolean>>() {})
                .doOnSuccess(result -> log.info("Successfully checked users existence for messageId: {}", messageId))
                .doOnError(error -> log.error("Error checking users existence for messageId: {}", messageId, error));
    }
}
