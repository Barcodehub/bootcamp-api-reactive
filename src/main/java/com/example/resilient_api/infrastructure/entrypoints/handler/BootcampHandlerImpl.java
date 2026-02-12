package com.example.resilient_api.infrastructure.entrypoints.handler;

import com.example.resilient_api.domain.api.BootcampServicePort;
import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.exceptions.TechnicalException;
import com.example.resilient_api.infrastructure.adapters.webclient.MetricsWebClient;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampIdsRequest;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampWithCapacitiesDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.PageResponse;
import com.example.resilient_api.infrastructure.entrypoints.dto.CapacitySummaryDTO;
import com.example.resilient_api.infrastructure.entrypoints.dto.TechnologySummaryDTO;
import com.example.resilient_api.domain.model.PaginationRequest;
import com.example.resilient_api.infrastructure.entrypoints.mapper.BootcampMapper;
import com.example.resilient_api.infrastructure.entrypoints.util.APIResponse;
import com.example.resilient_api.infrastructure.entrypoints.util.ErrorDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Instant;
import java.util.List;

import static com.example.resilient_api.infrastructure.entrypoints.util.Constants.X_MESSAGE_ID;
import static com.example.resilient_api.infrastructure.entrypoints.util.Constants.TECHNOLOGY_ERROR;

@Component
@RequiredArgsConstructor
@Slf4j
public class BootcampHandlerImpl {

    private final BootcampServicePort bootcampServicePort;
    private final BootcampMapper bootcampMapper;
    private final MetricsWebClient metricsWebClient;

    public Mono<ServerResponse> createBootcamp(ServerRequest request) {
        String messageId = getMessageId(request);
        String authToken = request.headers().firstHeader("Authorization");

        return request.bodyToMono(BootcampDTO.class)
                .flatMap(bootcamp -> bootcampServicePort.registerBootcamp(
                        bootcampMapper.bootcampDTOToBootcamp(bootcamp), messageId)
                        .doOnSuccess(savedBootcamp -> {

                            // Registrar reporte
                                log.info("=== HANDLER === Calling registerBootcampReportAsync");
                                metricsWebClient.registerBootcampReportAsync(savedBootcamp.id(), messageId, authToken);

                        })
                )
                .flatMap(savedBootcamp ->
                        ServerResponse.status(HttpStatus.CREATED)
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(bootcampMapper.bootcampToBootcampDTO(savedBootcamp))
                )
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error(TECHNOLOGY_ERROR, ex))
                .onErrorResume(BusinessException.class, ex -> handleBusinessException(ex, messageId))
                .onErrorResume(TechnicalException.class, ex -> handleTechnicalException(ex, messageId))
                .onErrorResume(ex -> handleUnexpectedException(ex, messageId));
    }

    public Mono<ServerResponse> checkBootcampsExist(ServerRequest request) {
        String messageId = getMessageId(request);
        return request.bodyToMono(BootcampIdsRequest.class)
                .flatMap(idsRequest -> {
                    List<Long> ids = idsRequest.getIds() != null ? idsRequest.getIds() : List.of();
                    return bootcampServicePort.checkBootcampsExist(ids, messageId)
                            .doOnSuccess(result -> log.info("Bootcamps existence checked successfully with messageId: {}", messageId));
                })
                .flatMap(result -> ServerResponse.status(HttpStatus.OK).bodyValue(result))
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error("Error checking capacities existence for messageId: {}", messageId, ex))
                .onErrorResume(TechnicalException.class, ex -> handleTechnicalException(ex, messageId))
                .onErrorResume(ex -> handleUnexpectedException(ex, messageId));
    }

    public Mono<ServerResponse> listBootcamps(ServerRequest request) {
        String messageId = getMessageId(request);

        // Extraer parámetros de query
        int page = request.queryParam("page")
                .map(Integer::parseInt)
                .orElse(PaginationRequest.DEFAULT_PAGE);

        int size = request.queryParam("size")
                .map(Integer::parseInt)
                .orElse(PaginationRequest.DEFAULT_SIZE);

        PaginationRequest.SortField sortBy = request.queryParam("sortBy")
                .map(String::toUpperCase)
                .map(PaginationRequest.SortField::valueOf)
                .orElse(PaginationRequest.SortField.NAME);

        PaginationRequest.SortDirection sortDirection = request.queryParam("sortDirection")
                .map(String::toUpperCase)
                .map(PaginationRequest.SortDirection::valueOf)
                .orElse(PaginationRequest.SortDirection.ASC);

        PaginationRequest paginationRequest = new PaginationRequest(page, size, sortBy, sortDirection);

        return bootcampServicePort.listBootcamps(paginationRequest, messageId)
                .map(pageResult -> {
                    // Mapear de dominio a DTO
                    List<BootcampWithCapacitiesDTO> content = pageResult.content().stream()
                            .map(bootcamp -> BootcampWithCapacitiesDTO.builder()
                                    .id(bootcamp.id())
                                    .name(bootcamp.name())
                                    .description(bootcamp.description())
                                    .launchDate(bootcamp.launchDate())
                                    .duration(bootcamp.duration())
                                    .capacities(bootcamp.capacities().stream()
                                            .map(capacity -> CapacitySummaryDTO.builder()
                                                    .id(capacity.id())
                                                    .name(capacity.name())
                                                    .technologies(capacity.technologies().stream()
                                                            .map(tech -> TechnologySummaryDTO.builder()
                                                                    .id(tech.id())
                                                                    .name(tech.name())
                                                                    .build())
                                                            .toList())
                                                    .build())
                                            .toList())
                                    .build())
                            .toList();

                    return PageResponse.<BootcampWithCapacitiesDTO>builder()
                            .content(content)
                            .page(pageResult.page())
                            .size(pageResult.size())
                            .totalElements(pageResult.totalElements())
                            .totalPages(pageResult.totalPages())
                            .first(pageResult.first())
                            .last(pageResult.last())
                            .build();
                })
                .flatMap(pageResponse -> ServerResponse.ok().bodyValue(pageResponse))
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnSuccess(response -> log.info("Bootcamps listed successfully with messageId: {}", messageId))
                .doOnError(ex -> log.error("Error listing bootcamps for messageId: {}", messageId, ex))
                .onErrorResume(TechnicalException.class, ex -> handleTechnicalException(ex, messageId))
                .onErrorResume(ex -> handleUnexpectedException(ex, messageId));
    }

    public Mono<ServerResponse> getBootcampById(ServerRequest request) {
        String messageId = getMessageId(request);
        Long id = Long.valueOf(request.pathVariable("id"));

        return bootcampServicePort.getBootcampById(id, messageId)
                .map(bootcamp -> BootcampWithCapacitiesDTO.builder()
                        .id(bootcamp.id())
                        .name(bootcamp.name())
                        .description(bootcamp.description())
                        .launchDate(bootcamp.launchDate())
                        .duration(bootcamp.duration())
                        .capacities(bootcamp.capacities().stream()
                                .map(capacity -> CapacitySummaryDTO.builder()
                                        .id(capacity.id())
                                        .name(capacity.name())
                                        .technologies(capacity.technologies().stream()
                                                .map(tech -> TechnologySummaryDTO.builder()
                                                        .id(tech.id())
                                                        .name(tech.name())
                                                        .build())
                                                .toList())
                                        .build())
                                .toList())
                        .build())
                .flatMap(bootcampDTO -> ServerResponse.ok().bodyValue(bootcampDTO))
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnSuccess(response -> log.info("Bootcamp retrieved successfully with messageId: {}", messageId))
                .doOnError(ex -> log.error("Error retrieving bootcamp for messageId: {}", messageId, ex))
                .onErrorResume(BusinessException.class, ex -> handleBusinessException(ex, messageId))
                .onErrorResume(TechnicalException.class, ex -> handleTechnicalException(ex, messageId))
                .onErrorResume(ex -> handleUnexpectedException(ex, messageId));
    }

    public Mono<ServerResponse> deleteBootcamp(ServerRequest request) {
        String messageId = getMessageId(request);
        Long id = Long.valueOf(request.pathVariable("id"));

        return bootcampServicePort.deleteBootcamp(id, messageId)
                .doOnSuccess(v -> log.info("Bootcamp deleted successfully with messageId: {}", messageId))
                .flatMap(v -> ServerResponse.status(HttpStatus.OK)
                        .bodyValue(TechnicalMessage.BOOTCAMP_DELETED.getMessage()))
                .contextWrite(Context.of(X_MESSAGE_ID, messageId))
                .doOnError(ex -> log.error("Error deleting bootcamp for messageId: {}", messageId, ex))
                .onErrorResume(BusinessException.class, ex -> handleBusinessException(ex, messageId))
                .onErrorResume(TechnicalException.class, ex -> handleTechnicalException(ex, messageId))
                .onErrorResume(ex -> handleUnexpectedException(ex, messageId));
    }

    private Mono<ServerResponse> handleBusinessException(BusinessException ex, String messageId) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                messageId,
                TechnicalMessage.INVALID_PARAMETERS,
                List.of(buildErrorDTO(ex.getTechnicalMessage())));
    }

    private Mono<ServerResponse> handleTechnicalException(TechnicalException ex, String messageId) {
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                messageId,
                TechnicalMessage.INTERNAL_ERROR,
                List.of(buildErrorDTO(ex.getTechnicalMessage())));
    }

    private Mono<ServerResponse> handleUnexpectedException(Throwable ex, String messageId) {
        log.error("Unexpected error occurred for messageId: {}", messageId, ex);
        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                messageId,
                TechnicalMessage.INTERNAL_ERROR,
                List.of(ErrorDTO.builder()
                        .code(TechnicalMessage.INTERNAL_ERROR.getCode())
                        .message(TechnicalMessage.INTERNAL_ERROR.getMessage())
                        .build()));
    }

    private ErrorDTO buildErrorDTO(TechnicalMessage technicalMessage) {
        return ErrorDTO.builder()
                .code(technicalMessage.getCode())
                .message(technicalMessage.getMessage())
                .param(technicalMessage.getParam())
                .build();
    }

    private Mono<ServerResponse> buildErrorResponse(HttpStatus httpStatus, String identifier, TechnicalMessage error,
                                                    List<ErrorDTO> errors) {
        return Mono.defer(() -> {
            APIResponse apiErrorResponse = APIResponse
                    .builder()
                    .code(error.getCode())
                    .message(error.getMessage())
                    .identifier(identifier)
                    .date(Instant.now().toString())
                    .errors(errors)
                    .build();
            return ServerResponse.status(httpStatus)
                    .bodyValue(apiErrorResponse);
        });
    }

    private String getMessageId(ServerRequest serverRequest) {
        return serverRequest.headers().firstHeader(X_MESSAGE_ID);
    }
}
