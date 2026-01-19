package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.BootcampWithCapacities;
import com.example.resilient_api.domain.model.Page;
import com.example.resilient_api.domain.model.PaginationRequest;
import com.example.resilient_api.domain.model.CapacitySummary;
import com.example.resilient_api.domain.api.BootcampServicePort;
import com.example.resilient_api.domain.spi.BootcampPersistencePort;
import com.example.resilient_api.domain.spi.CapacityExternalServicePort;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BootcampUseCase implements BootcampServicePort {

    private static final int MAX_NAME_LENGTH = 50;
    private static final int MAX_DESCRIPTION_LENGTH = 90;
    private static final int MIN_CAPACITIES = 1;
    private static final int MAX_CAPACITIES = 4;
    private static final int MIN_DURATION = 1;

    private final BootcampPersistencePort bootcampPersistencePort;
    private final CapacityExternalServicePort capacityExternalServicePort;

    public BootcampUseCase(BootcampPersistencePort bootcampPersistencePort,
                           CapacityExternalServicePort capacityExternalServicePort) {
        this.bootcampPersistencePort = bootcampPersistencePort;
        this.capacityExternalServicePort = capacityExternalServicePort;
    }

    @Override
    public Mono<Bootcamp> registerBootcamp(Bootcamp bootcamp, String messageId) {
        return validateBootcamp(bootcamp)
                .then(validateCapacities(bootcamp.capacityIds()))
                .then(checkCapacitiesExistInExternalService(bootcamp.capacityIds(), messageId))
                .then(bootcampPersistencePort.existByName(bootcamp.name()))
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_ALREADY_EXISTS)))
                .flatMap(exists -> bootcampPersistencePort.save(bootcamp));
    }

    @Override
    public Mono<Map<Long, Boolean>> checkBootcampsExist(List<Long> ids, String messageId) {
        if (ids == null || ids.isEmpty()) {
            return Mono.just(Map.of());
        }

        return bootcampPersistencePort.findExistingIdsByIds(ids)
                .collect(Collectors.toSet())
                .map(existingIds -> ids.stream()
                        .collect(Collectors.toMap(
                                id -> id,
                                existingIds::contains
                        ))
                );
    }

    @Override
    public Mono<Page<BootcampWithCapacities>> listBootcamps(PaginationRequest paginationRequest, String messageId) {
        // Obtener el conteo total y las capacidades en paralelo
        Mono<Long> totalCount = bootcampPersistencePort.count();
        Mono<List<Bootcamp>> bootcamps = bootcampPersistencePort
                .findAllPaginated(paginationRequest)
                .collectList();

        return Mono.zip(totalCount, bootcamps)
                .flatMap(tuple -> {
                    Long total = tuple.getT1();
                    List<Bootcamp> bootcampList = tuple.getT2();

                    if (bootcampList.isEmpty()) {
                        return Mono.just(Page.of(List.of(), paginationRequest.page(), paginationRequest.size(), total));
                    }

                    // Enriquecer cada capacidad con sus tecnologías
                    return enrichBootcampsWithCapacities(bootcampList, messageId)
                            .collectList()
                            .map(enrichedBootcamps -> Page.of(
                                    enrichedBootcamps,
                                    paginationRequest.page(),
                                    paginationRequest.size(),
                                    total
                            ));
                });
    }

    @Override
    public Mono<Void> deleteBootcamp(Long id, String messageId) {
        // 1. Verificar que el bootcamp existe
        return bootcampPersistencePort.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_NOT_FOUND)))
                .flatMap(bootcamp ->
                    // 2. Obtener IDs de capacidades asociadas
                    bootcampPersistencePort.findCapacityIdsByBootcampId(id)
                            .collectList()
                            .flatMap(capacityIds -> {
                                if (capacityIds.isEmpty()) {
                                    // Si no hay capacidades, solo eliminar el bootcamp
                                    return bootcampPersistencePort.deleteBootcampCapacitiesByBootcampId(id)
                                            .then(bootcampPersistencePort.deleteById(id));
                                }

                                // 3. Para cada capacidad, contar cuántos bootcamps la referencian
                                return reactor.core.publisher.Flux.fromIterable(capacityIds)
                                        .flatMap(capacityId ->
                                            bootcampPersistencePort.countBootcampsByCapacityId(capacityId)
                                                .map(count -> new Object() {
                                                    final Long id = capacityId;
                                                    final Long refCount = count;
                                                })
                                        )
                                        .collectList()
                                        .flatMap(capacityReferences -> {
                                            // 4. Filtrar capacidades con solo 1 referencia (este bootcamp)
                                            List<Long> capacitiesToDelete = capacityReferences.stream()
                                                    .filter(ref -> ref.refCount <= 1)
                                                    .map(ref -> ref.id)
                                                    .toList();

                                            // 5. Eliminar relaciones bootcamp_capacity primero
                                            return bootcampPersistencePort.deleteBootcampCapacitiesByBootcampId(id)
                                                    // 6. Si hay capacidades a eliminar, notificar a capacity-api
                                                    .then(capacitiesToDelete.isEmpty()
                                                        ? Mono.empty()
                                                        : capacityExternalServicePort.deleteCapacitiesByIds(capacitiesToDelete, messageId))
                                                    // 7. Eliminar el bootcamp
                                                    .then(bootcampPersistencePort.deleteById(id));
                                        });
                            })
                );
    }

    private reactor.core.publisher.Flux<BootcampWithCapacities> enrichBootcampsWithCapacities(
            List<Bootcamp> bootcamps, String messageId) {

        return reactor.core.publisher.Flux.fromIterable(bootcamps)
                .concatMap(bootcamp ->
                    bootcampPersistencePort.findCapacityIdsByBootcampId(bootcamp.id())
                            .collectList()
                            .flatMap(capacityIds -> {
                                if (capacityIds.isEmpty()) {
                                    return Mono.just(new BootcampWithCapacities(
                                            bootcamp.id(),
                                            bootcamp.name(),
                                            bootcamp.description(),
                                            bootcamp.launchDate(),
                                            bootcamp.duration(),
                                            List.of()
                                    ));
                                }

                                // Consultar las capacidades con sus tecnologías al servicio externo
                                return capacityExternalServicePort.getCapacitiesWithTechnologies(capacityIds, messageId)
                                        .collectList()
                                        .map(capacities -> new BootcampWithCapacities(
                                                bootcamp.id(),
                                                bootcamp.name(),
                                                bootcamp.description(),
                                                bootcamp.launchDate(),
                                                bootcamp.duration(),
                                                capacities
                                        ));
                            })
                );
    }

    private Mono<Void> validateBootcamp(Bootcamp bootcamp) {
        if (bootcamp.name() == null || bootcamp.name().trim().isEmpty()) {
            return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_NAME_REQUIRED));
        }
        if (bootcamp.description() == null || bootcamp.description().trim().isEmpty()) {
            return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_DESCRIPTION_REQUIRED));
        }
        if (bootcamp.name().length() > MAX_NAME_LENGTH) {
            return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_NAME_TOO_LONG));
        }
        if (bootcamp.description().length() > MAX_DESCRIPTION_LENGTH) {
            return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGY_DESCRIPTION_TOO_LONG));
        }
        if (bootcamp.launchDate() == null) {
            return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_LAUNCH_DATE_REQUIRED));
        }
        if (bootcamp.launchDate().isBefore(LocalDate.now())) {
            return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_LAUNCH_DATE_PAST));
        }
        if (bootcamp.duration() == null || bootcamp.duration() < MIN_DURATION) {
            return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_DURATION_INVALID));
        }
        return Mono.empty();
    }

    private Mono<Void> validateCapacities(List<Long> capacityIds) {
        // Validar que se proporcionen capacidades
        if (capacityIds == null || capacityIds.isEmpty()) {
            return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_CAPACITIES_REQUIRED));
        }

        // Validar mínimo de capacidades
        if (capacityIds.size() < MIN_CAPACITIES) {
            return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_CAPACITIES_MIN));
        }

        // Validar máximo de capacidades
        if (capacityIds.size() > MAX_CAPACITIES) {
            return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_CAPACITIES_MAX));
        }

        // Validar que no haya capacidades duplicadas
        if (capacityIds.size() != new HashSet<>(capacityIds).size()) {
            return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_CAPACITIES_DUPLICATED));
        }

        return Mono.empty();
    }

    private Mono<Void> checkCapacitiesExistInExternalService(List<Long> capacityIds, String messageId) {
        return capacityExternalServicePort.checkCapacitiesExist(capacityIds, messageId)
                .flatMap(existenceMap -> {
                    // Verificar que todas las tecnologías existan
                    boolean allExist = existenceMap.values().stream().allMatch(exists -> exists);
                    if (!allExist) {
                        return Mono.error(new BusinessException(TechnicalMessage.TECHNOLOGIES_NOT_FOUND));
                    }
                    return Mono.empty();
                });
    }
}


