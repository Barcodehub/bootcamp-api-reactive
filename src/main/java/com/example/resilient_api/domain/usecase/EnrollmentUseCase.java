package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.api.EnrollmentServicePort;
import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.BootcampEnrollment;
import com.example.resilient_api.domain.spi.BootcampPersistencePort;
import com.example.resilient_api.domain.spi.EnrollmentPersistencePort;
import com.example.resilient_api.domain.spi.UserExternalServicePort;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

@Slf4j
public class EnrollmentUseCase implements EnrollmentServicePort {

    private static final int MAX_BOOTCAMPS_PER_USER = 5;

    private final EnrollmentPersistencePort enrollmentPersistencePort;
    private final BootcampPersistencePort bootcampPersistencePort;
    private final UserExternalServicePort userExternalServicePort;

    public EnrollmentUseCase(
            EnrollmentPersistencePort enrollmentPersistencePort,
            BootcampPersistencePort bootcampPersistencePort,
            UserExternalServicePort userExternalServicePort) {
        this.enrollmentPersistencePort = enrollmentPersistencePort;
        this.bootcampPersistencePort = bootcampPersistencePort;
        this.userExternalServicePort = userExternalServicePort;
    }

    @Override
    public Mono<BootcampEnrollment> enrollUserInBootcamp(Long bootcampId, Long userId, String messageId) {
        log.info("Processing enrollment request for user {} in bootcamp {} with messageId: {}",
                userId, bootcampId, messageId);

        return validateUserExistsSync(userId, messageId)
                .then(Mono.defer(() -> bootcampPersistencePort.findById(bootcampId)))
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_NOT_FOUND)))
                .flatMap(bootcamp -> enrollmentPersistencePort.isUserEnrolled(bootcamp.id(), userId)
                        .flatMap(isEnrolled -> {
                            if (isEnrolled) {
                                log.warn("User {} is already enrolled in bootcamp {}", userId, bootcamp.id());
                                return Mono.error(new BusinessException(TechnicalMessage.USER_ALREADY_ENROLLED));
                            }
                            return validateMaxBootcamps(userId)
                                    .then(validateDateConflicts(bootcamp, userId, messageId))
                                    .then(enrollmentPersistencePort.enrollUser(bootcampId, userId));
                        }))
                .doOnSuccess(enrollment -> log.info("User {} successfully enrolled in bootcamp {} with messageId: {}",
                        userId, bootcampId, messageId))
                .doOnError(error -> log.error("Error enrolling user {} in bootcamp {} with messageId: {}",
                        userId, bootcampId, messageId, error));
    }

    @Override
    public Mono<Void> unenrollUserFromBootcamp(Long bootcampId, Long userId, String messageId) {
        log.info("Processing unenrollment request for user {} from bootcamp {} with messageId: {}",
                userId, bootcampId, messageId);

        return enrollmentPersistencePort.isUserEnrolled(bootcampId, userId)
                .flatMap(isEnrolled -> {
                    if (Boolean.FALSE.equals(isEnrolled)) {
                        return Mono.error(new BusinessException(TechnicalMessage.ENROLLMENT_NOT_FOUND));
                    }
                    return enrollmentPersistencePort.unenrollUser(bootcampId, userId);
                })
                .doOnSuccess(v -> log.info("User {} successfully unenrolled from bootcamp {} with messageId: {}",
                        userId, bootcampId, messageId))
                .doOnError(error -> log.error("Error unenrolling user {} from bootcamp {} with messageId: {}",
                        userId, bootcampId, messageId, error));
    }

    @Override
    public Flux<Bootcamp> getUserBootcamps(Long userId, String messageId) {
        log.info("Getting bootcamps for user {} with messageId: {}", userId, messageId);

        return validateUserExistsSync(userId, messageId)
                .thenMany(Flux.defer(() -> enrollmentPersistencePort.findBootcampsByUserId(userId)))
                .doOnComplete(() -> log.info("Successfully retrieved bootcamps for user {} with messageId: {}",
                        userId, messageId))
                .doOnError(error -> log.error("Error getting bootcamps for user {} with messageId: {}",
                        userId, messageId, error));
    }

    @Override
    public Flux<Long> getUserIdsByBootcampId(Long bootcampId, String messageId) {
        log.info("Getting user IDs for bootcamp: {} with messageId: {}", bootcampId, messageId);
        return enrollmentPersistencePort.findUserIdsByBootcampId(bootcampId);
    }

    private Mono<Void> validateMaxBootcamps(Long userId) {
        return enrollmentPersistencePort.countEnrollmentsByUserId(userId)
                .flatMap(count -> {
                    if (count >= MAX_BOOTCAMPS_PER_USER) {
                        log.warn("User {} has reached maximum bootcamp limit: {}", userId, count);
                        return Mono.error(new BusinessException(TechnicalMessage.MAX_BOOTCAMPS_REACHED));
                    }
                    return Mono.empty();
                });
    }

    private Mono<Void> validateDateConflicts(Bootcamp newBootcamp, Long userId, String messageId) {
        log.debug("Checking date conflicts for user {} and bootcamp {}", userId, newBootcamp.id());

        LocalDate newBootcampStart = newBootcamp.launchDate();
        LocalDate newBootcampEnd = newBootcampStart.plusDays(newBootcamp.duration());

        return enrollmentPersistencePort.findBootcampsByUserId(userId)
                .collectList()
                .flatMap(enrolledBootcamps -> {
                    boolean hasConflict = enrolledBootcamps.stream()
                            .anyMatch(existingBootcamp -> {
                                LocalDate existingStart = existingBootcamp.launchDate();
                                LocalDate existingEnd = existingStart.plusDays(existingBootcamp.duration());

                                boolean overlaps = !(newBootcampEnd.isBefore(existingStart) ||
                                                    newBootcampStart.isAfter(existingEnd));

                                if (overlaps) {
                                    log.warn("Date conflict detected for user {} between bootcamp {} ({} to {}) and bootcamp {} ({} to {})",
                                            userId, newBootcamp.id(), newBootcampStart, newBootcampEnd,
                                            existingBootcamp.id(), existingStart, existingEnd);
                                }

                                return overlaps;
                            });

                    if (hasConflict) {
                        return Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_DATE_CONFLICT));
                    }

                    return Mono.empty();
                });
    }

    private Mono<Void> validateUserExistsSync(Long userId, String messageId) {
        return userExternalServicePort.checkUsersExist(List.of(userId), messageId)
                .flatMap(existenceMap -> {
                    Boolean exists = null;
                    for (Object key : existenceMap.keySet()) {
                        if (key != null && Long.valueOf(key.toString()).equals(userId)) {
                            exists = existenceMap.get(key);
                            break;
                        }
                    }
                    if (exists == null || !exists) {
                        log.warn("User {} does not exist (existenceMap: {})", userId, existenceMap);
                        return Mono.error(new BusinessException(TechnicalMessage.USER_NOT_FOUND));
                    }
                    return Mono.empty();
                });
    }
}
