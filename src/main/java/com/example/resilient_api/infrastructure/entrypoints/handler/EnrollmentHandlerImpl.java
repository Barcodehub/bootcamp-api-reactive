package com.example.resilient_api.infrastructure.entrypoints.handler;

import com.example.resilient_api.domain.api.EnrollmentServicePort;
import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.infrastructure.adapters.webclient.MetricsWebClient;
import com.example.resilient_api.infrastructure.entrypoints.dto.EnrollmentRequestDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.EnrollmentResponseDTO;
import com.example.resilient_api.infrastructure.entrypoints.mapper.BootcampMapper;
import com.example.resilient_api.infrastructure.entrypoints.util.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.example.resilient_api.infrastructure.entrypoints.util.Constants.X_MESSAGE_ID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EnrollmentHandlerImpl {

    private final EnrollmentServicePort enrollmentServicePort;
    private final BootcampMapper bootcampMapper;
    private final MetricsWebClient metricsWebClient;

    public Mono<ServerResponse> enrollUser(ServerRequest request) {
        String messageId = getMessageId(request);
        String authToken = request.headers().firstHeader("Authorization");
        log.info("Received enroll user request with messageId: {}", messageId);

        // Extraer userId del header enviado por capacity-api
        String userIdHeader = request.headers().firstHeader("X-User-Id");
        if (userIdHeader == null) {
            log.error("X-User-Id header is missing");
            return Mono.error(new BusinessException(TechnicalMessage.UNAUTHORIZED_ACTION));
        }

        Long userId = Long.parseLong(userIdHeader);

        return request.bodyToMono(EnrollmentRequestDTO.class)
                .flatMap(enrollmentRequest ->
                        enrollmentServicePort.enrollUserInBootcamp(
                                enrollmentRequest.getBootcampId(),
                                userId,
                                messageId
                        ).doOnSuccess(enrollment -> {
                            // Actualizar reporte de forma asíncrona sin afectar el rendimiento
                            if (authToken != null) {
                                metricsWebClient.registerBootcampReportAsync(
                                    enrollmentRequest.getBootcampId(),
                                    messageId,
                                    authToken
                                );
                            }
                        }))
                .flatMap(enrollment -> {
                    EnrollmentResponseDTO responseDTO = EnrollmentResponseDTO.builder()
                            .id(enrollment.id())
                            .bootcampId(enrollment.bootcampId())
                            .userId(enrollment.userId())
                            .enrolledAt(enrollment.enrolledAt())
                            .build();

                    return ServerResponse.status(HttpStatus.CREATED)
                            .bodyValue(responseDTO);
                })
                .doOnSuccess(response -> log.info("Successfully processed enroll user request with messageId: {}", messageId))
                .doOnError(error -> log.error("Error processing enroll user request with messageId: {}", messageId, error));
    }

    public Mono<ServerResponse> unenrollUser(ServerRequest request) {
        String messageId = getMessageId(request);
        String authToken = request.headers().firstHeader("Authorization");
        log.info("Received unenroll user request with messageId: {}", messageId);

        Long bootcampId = Long.parseLong(request.pathVariable("bootcampId"));
        Long userId = Long.parseLong(request.pathVariable("userId"));

        return enrollmentServicePort.unenrollUserFromBootcamp(bootcampId, userId, messageId)
                .doOnSuccess(v -> {
                    // Actualizar reporte de forma asíncrona sin afectar el rendimiento
                    if (authToken != null) {
                        metricsWebClient.registerBootcampReportAsync(bootcampId, messageId, authToken);
                    }
                })
                .flatMap(v -> {
                    APIResponse apiResponse = APIResponse.builder()
                            .code(TechnicalMessage.ENROLLMENT_DELETED.getCode())
                            .message(TechnicalMessage.ENROLLMENT_DELETED.getMessage())
                            .identifier(messageId)
                            .date(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                            .build();

                    return ServerResponse.ok().bodyValue(apiResponse);
                })
                .doOnSuccess(response -> log.info("Successfully processed unenroll user request with messageId: {}", messageId))
                .doOnError(error -> log.error("Error processing unenroll user request with messageId: {}", messageId, error));
    }

    public Mono<ServerResponse> getUserBootcamps(ServerRequest request) {
        String messageId = getMessageId(request);
        log.info("Received get user bootcamps request with messageId: {}", messageId);

        Long userId = Long.parseLong(request.pathVariable("userId"));

        return enrollmentServicePort.getUserBootcamps(userId, messageId)
                .map(bootcampMapper::bootcampToBootcampDTO)
                .collectList()
                .flatMap(bootcamps -> ServerResponse.ok().bodyValue(bootcamps))
                .doOnSuccess(response -> log.info("Successfully processed get user bootcamps request with messageId: {}", messageId))
                .doOnError(error -> log.error("Error processing get user bootcamps request with messageId: {}", messageId, error));
    }

    public Mono<ServerResponse> getUserIdsByBootcampId(ServerRequest request) {
        String messageId = getMessageId(request);
        Long bootcampId = Long.parseLong(request.pathVariable("id"));

        log.info("Received get user IDs by bootcamp request for bootcampId: {} with messageId: {}", bootcampId, messageId);

        return enrollmentServicePort.getUserIdsByBootcampId(bootcampId, messageId)
                .collectList()
                .flatMap(userIds -> {
                    log.info("Found {} users for bootcamp {} with messageId: {}", userIds.size(), bootcampId, messageId);
                    return ServerResponse.ok().bodyValue(userIds);
                })
                .doOnSuccess(response -> log.info("Successfully processed get user IDs request with messageId: {}", messageId))
                .doOnError(error -> log.error("Error processing get user IDs request with messageId: {}", messageId, error));
    }

    private String getMessageId(ServerRequest request) {
        String messageId = request.headers().firstHeader(X_MESSAGE_ID);
        if (messageId == null || messageId.isEmpty()) {
            messageId = UUID.randomUUID().toString();
        }
        return messageId;
    }
}
