package com.example.resilient_api.infrastructure.adapters.persistenceadapter;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.BootcampEnrollment;
import com.example.resilient_api.domain.spi.EnrollmentPersistencePort;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.entity.BootcampUserEntity;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.BootcampEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampUserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@AllArgsConstructor
@Slf4j
public class EnrollmentPersistenceAdapter implements EnrollmentPersistencePort {

    private final BootcampUserRepository bootcampUserRepository;
    private final BootcampRepository bootcampRepository;
    private final BootcampEntityMapper bootcampEntityMapper;

    @Override
    public Mono<BootcampEnrollment> enrollUser(Long bootcampId, Long userId) {
        log.info("Enrolling user {} in bootcamp {}", userId, bootcampId);

        BootcampUserEntity entity = new BootcampUserEntity();
        entity.setBootcampId(bootcampId);
        entity.setUserId(userId);
        entity.setEnrolledAt(LocalDateTime.now());

        return bootcampUserRepository.save(entity)
                .map(saved -> new BootcampEnrollment(
                        saved.getId(),
                        saved.getBootcampId(),
                        saved.getUserId(),
                        saved.getEnrolledAt()
                ))
                .doOnSuccess(enrollment -> log.info("User {} successfully enrolled in bootcamp {}", userId, bootcampId));
    }

    @Override
    public Mono<Void> unenrollUser(Long bootcampId, Long userId) {
        log.info("Unenrolling user {} from bootcamp {}", userId, bootcampId);
        return bootcampUserRepository.deleteByBootcampIdAndUserId(bootcampId, userId)
                .doOnSuccess(v -> log.info("User {} successfully unenrolled from bootcamp {}", userId, bootcampId));
    }

    @Override
    public Mono<Long> countEnrollmentsByUserId(Long userId) {
        return bootcampUserRepository.countByUserId(userId);
    }

    @Override
    public Flux<BootcampEnrollment> findEnrollmentsByUserId(Long userId) {
        return bootcampUserRepository.findByUserId(userId)
                .map(entity -> new BootcampEnrollment(
                        entity.getId(),
                        entity.getBootcampId(),
                        entity.getUserId(),
                        entity.getEnrolledAt()
                ));
    }

    @Override
    public Flux<Bootcamp> findBootcampsByUserId(Long userId) {
        return bootcampUserRepository.findByUserId(userId)
                .flatMap(enrollment -> bootcampRepository.findById(enrollment.getBootcampId()))
                .map(bootcampEntityMapper::toModel);
    }

    @Override
    public Mono<Boolean> isUserEnrolled(Long bootcampId, Long userId) {
        return bootcampUserRepository.findByBootcampIdAndUserId(bootcampId, userId)
                .hasElement();
    }
}
