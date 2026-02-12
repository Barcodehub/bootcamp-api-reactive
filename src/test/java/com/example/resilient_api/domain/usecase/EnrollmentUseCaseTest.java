package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.BootcampEnrollment;
import com.example.resilient_api.domain.spi.BootcampPersistencePort;
import com.example.resilient_api.domain.spi.EnrollmentPersistencePort;
import com.example.resilient_api.domain.spi.UserExternalServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentUseCaseTest {

    @Mock
    private EnrollmentPersistencePort enrollmentPersistencePort;

    @Mock
    private BootcampPersistencePort bootcampPersistencePort;

    @Mock
    private UserExternalServicePort userExternalServicePort;

    @InjectMocks
    private EnrollmentUseCase enrollmentUseCase;

    private String messageId;
    private Bootcamp validBootcamp;

    @BeforeEach
    void setUp() {
        messageId = "test-message-id-123";
        validBootcamp = new Bootcamp(1L, "Java Bootcamp", "Java training",
                LocalDate.now().plusDays(30), 90, List.of(1L, 2L));
    }

    @Test
    void enrollUserInBootcamp_WithValidData_ShouldReturnEnrollment() {
        // Arrange
        Long bootcampId = 1L;
        Long userId = 100L;
        BootcampEnrollment enrollment = new BootcampEnrollment(1L, bootcampId, userId, LocalDateTime.now());

        when(userExternalServicePort.checkUsersExist(List.of(userId), messageId))
                .thenReturn(Mono.just(java.util.Map.of(userId, true)));
        when(bootcampPersistencePort.findById(bootcampId)).thenReturn(Mono.just(validBootcamp));
        when(enrollmentPersistencePort.isUserEnrolled(bootcampId, userId)).thenReturn(Mono.just(false));
        when(enrollmentPersistencePort.countEnrollmentsByUserId(userId)).thenReturn(Mono.just(2L));
        when(enrollmentPersistencePort.findBootcampsByUserId(userId)).thenReturn(Flux.empty());
        when(enrollmentPersistencePort.enrollUser(bootcampId, userId)).thenReturn(Mono.just(enrollment));

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.enrollUserInBootcamp(bootcampId, userId, messageId))
                .expectNext(enrollment)
                .verifyComplete();

        verify(userExternalServicePort).checkUsersExist(List.of(userId), messageId);
        verify(bootcampPersistencePort).findById(bootcampId);
        verify(enrollmentPersistencePort).isUserEnrolled(bootcampId, userId);
        verify(enrollmentPersistencePort).enrollUser(bootcampId, userId);
    }

    @Test
    void enrollUserInBootcamp_WithNonExistingUser_ShouldThrowBusinessException() {
        // Arrange
        Long bootcampId = 1L;
        Long userId = 999L;

        when(userExternalServicePort.checkUsersExist(List.of(userId), messageId))
                .thenReturn(Mono.just(java.util.Map.of(userId, false)));

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.enrollUserInBootcamp(bootcampId, userId, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.USER_NOT_FOUND)
                .verify();

        verify(enrollmentPersistencePort, never()).enrollUser(anyLong(), anyLong());
    }

    @Test
    void enrollUserInBootcamp_WithNonExistingBootcamp_ShouldThrowBusinessException() {
        // Arrange
        Long bootcampId = 999L;
        Long userId = 100L;

        when(userExternalServicePort.checkUsersExist(List.of(userId), messageId))
                .thenReturn(Mono.just(java.util.Map.of(userId, true)));
        when(bootcampPersistencePort.findById(bootcampId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.enrollUserInBootcamp(bootcampId, userId, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_NOT_FOUND)
                .verify();

        verify(enrollmentPersistencePort, never()).enrollUser(anyLong(), anyLong());
    }

    @Test
    void enrollUserInBootcamp_WhenAlreadyEnrolled_ShouldThrowBusinessException() {
        // Arrange
        Long bootcampId = 1L;
        Long userId = 100L;

        when(userExternalServicePort.checkUsersExist(List.of(userId), messageId))
                .thenReturn(Mono.just(java.util.Map.of(userId, true)));
        when(bootcampPersistencePort.findById(bootcampId)).thenReturn(Mono.just(validBootcamp));
        when(enrollmentPersistencePort.isUserEnrolled(bootcampId, userId)).thenReturn(Mono.just(true));

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.enrollUserInBootcamp(bootcampId, userId, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.USER_ALREADY_ENROLLED)
                .verify();

        verify(enrollmentPersistencePort, never()).enrollUser(anyLong(), anyLong());
    }

    @Test
    void unenrollUserFromBootcamp_WithValidData_ShouldComplete() {
        // Arrange
        Long bootcampId = 1L;
        Long userId = 100L;

        when(enrollmentPersistencePort.isUserEnrolled(bootcampId, userId)).thenReturn(Mono.just(true));
        when(enrollmentPersistencePort.unenrollUser(bootcampId, userId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.unenrollUserFromBootcamp(bootcampId, userId, messageId))
                .verifyComplete();

        verify(enrollmentPersistencePort).isUserEnrolled(bootcampId, userId);
        verify(enrollmentPersistencePort).unenrollUser(bootcampId, userId);
    }

    @Test
    void unenrollUserFromBootcamp_WhenNotEnrolled_ShouldThrowBusinessException() {
        // Arrange
        Long bootcampId = 1L;
        Long userId = 100L;

        when(enrollmentPersistencePort.isUserEnrolled(bootcampId, userId)).thenReturn(Mono.just(false));

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.unenrollUserFromBootcamp(bootcampId, userId, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.ENROLLMENT_NOT_FOUND)
                .verify();

        verify(enrollmentPersistencePort, never()).unenrollUser(anyLong(), anyLong());
    }

    @Test
    void getUserBootcamps_WithValidUser_ShouldReturnBootcamps() {
        // Arrange
        Long userId = 100L;
        Bootcamp bootcamp1 = new Bootcamp(1L, "Java Bootcamp", "Java training",
                LocalDate.now(), 90, List.of(1L, 2L));
        Bootcamp bootcamp2 = new Bootcamp(2L, "Python Bootcamp", "Python training",
                LocalDate.now().plusDays(30), 60, List.of(3L, 4L));

        when(userExternalServicePort.checkUsersExist(List.of(userId), messageId))
                .thenReturn(Mono.just(java.util.Map.of(userId, true)));
        when(enrollmentPersistencePort.findBootcampsByUserId(userId))
                .thenReturn(Flux.just(bootcamp1, bootcamp2));

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.getUserBootcamps(userId, messageId))
                .expectNext(bootcamp1)
                .expectNext(bootcamp2)
                .verifyComplete();

        verify(userExternalServicePort).checkUsersExist(List.of(userId), messageId);
        verify(enrollmentPersistencePort).findBootcampsByUserId(userId);
    }

    @Test
    void getUserBootcamps_WithNonExistingUser_ShouldThrowBusinessException() {
        // Arrange
        Long userId = 999L;

        when(userExternalServicePort.checkUsersExist(List.of(userId), messageId))
                .thenReturn(Mono.just(java.util.Map.of(userId, false)));

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.getUserBootcamps(userId, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.USER_NOT_FOUND)
                .verify();

        verify(enrollmentPersistencePort, never()).findBootcampsByUserId(anyLong());
    }

    @Test
    void getUserIdsByBootcampId_WithValidBootcamp_ShouldReturnUserIds() {
        // Arrange
        Long bootcampId = 1L;

        when(enrollmentPersistencePort.findUserIdsByBootcampId(bootcampId))
                .thenReturn(Flux.just(100L, 200L, 300L));

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.getUserIdsByBootcampId(bootcampId, messageId))
                .expectNext(100L)
                .expectNext(200L)
                .expectNext(300L)
                .verifyComplete();

        verify(enrollmentPersistencePort).findUserIdsByBootcampId(bootcampId);
    }

    @Test
    void getUserIdsByBootcampId_WithNoEnrollments_ShouldReturnEmpty() {
        // Arrange
        Long bootcampId = 1L;

        when(enrollmentPersistencePort.findUserIdsByBootcampId(bootcampId))
                .thenReturn(Flux.empty());

        // Act & Assert
        StepVerifier.create(enrollmentUseCase.getUserIdsByBootcampId(bootcampId, messageId))
                .verifyComplete();

        verify(enrollmentPersistencePort).findUserIdsByBootcampId(bootcampId);
    }
}
