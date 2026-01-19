package com.example.resilient_api.infrastructure.adapters.persistenceadapter;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.PaginationRequest;
import com.example.resilient_api.domain.spi.BootcampPersistencePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampCapacityEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.BootcampEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampCapacityRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@AllArgsConstructor
@Slf4j
public class BootcampPersistenceAdapter implements BootcampPersistencePort {

    private final BootcampRepository bootcampRepository;
    private final BootcampCapacityRepository bootcampCapacityRepository;
    private final BootcampEntityMapper bootcampEntityMapper;
    private final DatabaseClient databaseClient;

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        log.info("Saving bootcamp with name: {}", bootcamp.name());

        // Guardar el bootcamp
        return bootcampRepository.save(bootcampEntityMapper.toEntity(bootcamp))
                .flatMap(savedBootcampEntity -> {
                    // Guardar las relaciones con capacidades
                    List<Long> capacityIds = bootcamp.capacityIds();
                    if (capacityIds != null && !capacityIds.isEmpty()) {
                        return saveBootcampCapacities(savedBootcampEntity.getId(), capacityIds)
                                .then(Mono.just(new Bootcamp(
                                        savedBootcampEntity.getId(),
                                        savedBootcampEntity.getName(),
                                        savedBootcampEntity.getDescription(),
                                        savedBootcampEntity.getLaunchDate(),
                                        savedBootcampEntity.getDuration(),
                                        capacityIds
                                )));
                    }
                    return Mono.just(new Bootcamp(
                            savedBootcampEntity.getId(),
                            savedBootcampEntity.getName(),
                            savedBootcampEntity.getDescription(),
                            savedBootcampEntity.getLaunchDate(),
                            savedBootcampEntity.getDuration(),
                            List.of()
                    ));
                })
                .doOnSuccess(savedBootcamp -> log.info("Bootcamp saved successfully with id: {}", savedBootcamp.id()))
                .doOnError(error -> log.error("Error saving bootcamp", error));
    }

    @Override
    public Mono<Boolean> existByName(String name) {
        return bootcampRepository.findByName(name)
                .map(bootcampEntity -> true)
                .defaultIfEmpty(false);
    }

    @Override
    public Flux<Long> findExistingIdsByIds(List<Long> ids) {
        return bootcampRepository.findAllByIdIn(ids)
                .map(BootcampEntity::getId);
    }

    @Override
    public Flux<Bootcamp> findAllPaginated(PaginationRequest paginationRequest) {
        String orderBy = buildOrderByClause(paginationRequest);
        String query = """
                SELECT c.id, c.name, c.description, c.launch_date, c.duration
                FROM bootcamp c
                LEFT JOIN bootcamp_capacity ct ON c.id = ct.bootcamp_id
                GROUP BY c.id, c.name, c.description, c.launch_date, c.duration
                ORDER BY %s
                LIMIT :limit OFFSET :offset
                """.formatted(orderBy);

        return databaseClient.sql(query)
                .bind("limit", paginationRequest.size())
                .bind("offset", paginationRequest.getOffset())
                .map((row, metadata) -> {
                    BootcampEntity entity = new BootcampEntity();
                    entity.setId(row.get("id", Long.class));
                    entity.setName(row.get("name", String.class));
                    entity.setDescription(row.get("description", String.class));
                    entity.setLaunchDate(row.get("launch_date", java.time.LocalDate.class));
                    entity.setDuration(row.get("duration", Integer.class));
                    return entity;
                })
                .all()
                .flatMap(entity ->
                    findCapacityIdsByBootcampId(entity.getId())
                            .collectList()
                            .map(capacityIds -> new Bootcamp(
                                    entity.getId(),
                                    entity.getName(),
                                    entity.getDescription(),
                                    entity.getLaunchDate(),
                                    entity.getDuration(),
                                    capacityIds
                            ))
                );
    }

    @Override
    public Mono<Long> count() {
        return bootcampRepository.count();
    }

    @Override
    public Flux<Long> findCapacityIdsByBootcampId(Long bootcampId) {
        return bootcampCapacityRepository.findAllByBootcampId(bootcampId)
                .map(BootcampCapacityEntity::getCapacityId);
    }

    @Override
    public Mono<Bootcamp> findById(Long id) {
        return bootcampRepository.findById(id)
                .flatMap(entity ->
                    findCapacityIdsByBootcampId(id)
                            .collectList()
                            .map(capacityIds -> new Bootcamp(
                                    entity.getId(),
                                    entity.getName(),
                                    entity.getDescription(),
                                    entity.getLaunchDate(),
                                    entity.getDuration(),
                                    capacityIds
                            ))
                );
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return bootcampRepository.deleteById(id);
    }

    @Override
    public Mono<Void> deleteBootcampCapacitiesByBootcampId(Long bootcampId) {
        return bootcampCapacityRepository.findAllByBootcampId(bootcampId)
                .flatMap(bootcampCapacityRepository::delete)
                .then();
    }

    @Override
    public Mono<Long> countBootcampsByCapacityId(Long capacityId) {
        String query = "SELECT COUNT(DISTINCT bootcamp_id) FROM bootcamp_capacity WHERE capacity_id = :capacityId";
        return databaseClient.sql(query)
                .bind("capacityId", capacityId)
                .map(row -> row.get(0, Long.class))
                .one();
    }

    private String buildOrderByClause(PaginationRequest paginationRequest) {
        String direction = paginationRequest.sortDirection() == PaginationRequest.SortDirection.ASC ? "ASC" : "DESC";

        return switch (paginationRequest.sortBy()) {
            case NAME -> "c.name " + direction;
            case TECHNOLOGY_COUNT -> "COUNT(ct.capacity_id) " + direction + ", c.name ASC";
        };
    }

    private Mono<Void> saveBootcampCapacities(Long bootcampId, List<Long> capacityIds) {
        return Flux.fromIterable(capacityIds)
                .map(capacityId -> {
                    BootcampCapacityEntity entity = new BootcampCapacityEntity();
                    entity.setBootcampId(bootcampId);
                    entity.setCapacityId(capacityId);
                    return entity;
                })
                .flatMap(bootcampCapacityRepository::save)
                .then();
    }
}

