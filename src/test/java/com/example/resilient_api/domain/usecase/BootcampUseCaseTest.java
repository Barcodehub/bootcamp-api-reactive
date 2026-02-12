package com.example.resilient_api.domain.usecase;

import com.example.resilient_api.domain.enums.TechnicalMessage;
import com.example.resilient_api.domain.exceptions.BusinessException;
import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.BootcampWithCapacities;
import com.example.resilient_api.domain.model.CapacitySummary;
import com.example.resilient_api.domain.model.Page;
import com.example.resilient_api.domain.model.PaginationRequest;
import com.example.resilient_api.domain.spi.BootcampPersistencePort;
import com.example.resilient_api.domain.spi.CapacityExternalServicePort;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BootcampUseCaseTest {

    @Mock
    private BootcampPersistencePort bootcampPersistencePort;

    @Mock
    private CapacityExternalServicePort capacityExternalServicePort;

    @InjectMocks
    private BootcampUseCase bootcampUseCase;

    private Bootcamp validBootcamp;
    private String messageId;

    @BeforeEach
    void setUp() {
        validBootcamp = new Bootcamp(
                null,
                "Java Bootcamp",
                "Complete Java training",
                LocalDate.now().plusDays(30),
                90,
                List.of(1L, 2L, 3L)
        );
        messageId = "test-message-id-123";
    }

    @Test
    void registerBootcamp_WithValidData_ShouldReturnSavedBootcamp() {
        // Arrange
        Bootcamp savedBootcamp = new Bootcamp(1L, "Java Bootcamp", "Complete Java training",
                LocalDate.now().plusDays(30), 90, List.of(1L, 2L, 3L));

        when(bootcampPersistencePort.existByName(anyString())).thenReturn(Mono.just(false));
        when(capacityExternalServicePort.checkCapacitiesExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(1L, true, 2L, true, 3L, true)));
        when(bootcampPersistencePort.save(any(Bootcamp.class))).thenReturn(Mono.just(savedBootcamp));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(validBootcamp, messageId))
                .expectNext(savedBootcamp)
                .verifyComplete();

        verify(bootcampPersistencePort).existByName("Java Bootcamp");
        verify(capacityExternalServicePort).checkCapacitiesExist(List.of(1L, 2L, 3L), messageId);
        verify(bootcampPersistencePort).save(validBootcamp);
    }

    @Test
    void registerBootcamp_WithExistingName_ShouldThrowBusinessException() {
        // Arrange
        when(bootcampPersistencePort.existByName(anyString())).thenReturn(Mono.just(true));
        when(capacityExternalServicePort.checkCapacitiesExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(1L, true, 2L, true, 3L, true)));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(validBootcamp, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.TECHNOLOGY_ALREADY_EXISTS)
                .verify();

        verify(bootcampPersistencePort, never()).save(any(Bootcamp.class));
    }

    @Test
    void registerBootcamp_WithNullName_ShouldThrowBusinessException() {
        // Arrange
        Bootcamp invalidBootcamp = new Bootcamp(null, null, "Description",
                LocalDate.now().plusDays(30), 90, List.of(1L, 2L, 3L));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(invalidBootcamp, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.TECHNOLOGY_NAME_REQUIRED)
                .verify();
    }

    @Test
    void registerBootcamp_WithTooLongName_ShouldThrowBusinessException() {
        // Arrange
        String longName = "a".repeat(51);
        Bootcamp invalidBootcamp = new Bootcamp(null, longName, "Description",
                LocalDate.now().plusDays(30), 90, List.of(1L, 2L, 3L));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(invalidBootcamp, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.TECHNOLOGY_NAME_TOO_LONG)
                .verify();
    }

    @Test
    void registerBootcamp_WithNullCapacities_ShouldThrowBusinessException() {
        // Arrange
        Bootcamp invalidBootcamp = new Bootcamp(null, "Name", "Description",
                LocalDate.now().plusDays(30), 90, null);

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(invalidBootcamp, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_CAPACITIES_REQUIRED)
                .verify();
    }

    @Test
    void registerBootcamp_WithEmptyCapacities_ShouldThrowBusinessException() {
        // Arrange
        Bootcamp invalidBootcamp = new Bootcamp(null, "Name", "Description",
                LocalDate.now().plusDays(30), 90, List.of());

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(invalidBootcamp, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_CAPACITIES_REQUIRED)
                .verify();
    }

    @Test
    void registerBootcamp_WithTooManyCapacities_ShouldThrowBusinessException() {
        // Arrange
        List<Long> tooManyCapacities = List.of(1L, 2L, 3L, 4L, 5L);
        Bootcamp invalidBootcamp = new Bootcamp(null, "Name", "Description",
                LocalDate.now().plusDays(30), 90, tooManyCapacities);

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(invalidBootcamp, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_CAPACITIES_MAX)
                .verify();
    }

    @Test
    void registerBootcamp_WithDuplicateCapacities_ShouldThrowBusinessException() {
        // Arrange
        Bootcamp invalidBootcamp = new Bootcamp(null, "Name", "Description",
                LocalDate.now().plusDays(30), 90, List.of(1L, 2L, 1L));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(invalidBootcamp, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_CAPACITIES_DUPLICATED)
                .verify();
    }

    @Test
    void registerBootcamp_WithInvalidDuration_ShouldThrowBusinessException() {
        // Arrange
        Bootcamp invalidBootcamp = new Bootcamp(null, "Name", "Description",
                LocalDate.now().plusDays(30), 0, List.of(1L, 2L, 3L));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(invalidBootcamp, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_DURATION_INVALID)
                .verify();
    }

    @Test
    void registerBootcamp_WithNonExistingCapacity_ShouldThrowBusinessException() {
        // Arrange
        when(bootcampPersistencePort.existByName(anyString())).thenReturn(Mono.just(false));
        when(capacityExternalServicePort.checkCapacitiesExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(1L, true, 2L, false, 3L, true)));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.registerBootcamp(validBootcamp, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.TECHNOLOGIES_NOT_FOUND)
                .verify();

        verify(bootcampPersistencePort, never()).save(any(Bootcamp.class));
    }

    @Test
    void listBootcamps_ShouldReturnPagedBootcamps() {
        // Arrange
        PaginationRequest paginationRequest = new PaginationRequest(0, 10,
                PaginationRequest.SortField.NAME, PaginationRequest.SortDirection.ASC);

        Bootcamp bootcamp1 = new Bootcamp(1L, "Java Bootcamp", "Java training",
                LocalDate.now(), 90, List.of(1L, 2L));

        CapacitySummary capacity1 = new CapacitySummary(1L, "Backend", List.of());
        CapacitySummary capacity2 = new CapacitySummary(2L, "Frontend", List.of());

        when(bootcampPersistencePort.count()).thenReturn(Mono.just(1L));
        when(bootcampPersistencePort.findAllPaginated(any(PaginationRequest.class)))
                .thenReturn(Flux.just(bootcamp1));
        when(bootcampPersistencePort.findCapacityIdsByBootcampId(1L))
                .thenReturn(Flux.just(1L, 2L));
        when(capacityExternalServicePort.getCapacitiesWithTechnologies(List.of(1L, 2L), messageId))
                .thenReturn(Flux.just(capacity1, capacity2));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.listBootcamps(paginationRequest, messageId))
                .expectNextMatches(page ->
                    page.content().size() == 1 &&
                    page.totalElements() == 1 &&
                    page.content().get(0).capacities().size() == 2
                )
                .verifyComplete();

        verify(bootcampPersistencePort).count();
        verify(bootcampPersistencePort).findAllPaginated(paginationRequest);
    }

    @Test
    void getBootcampById_WithExistingId_ShouldReturnBootcamp() {
        // Arrange
        Long bootcampId = 1L;
        Bootcamp bootcamp = new Bootcamp(bootcampId, "Java Bootcamp", "Java training",
                LocalDate.now(), 90, List.of(1L, 2L));

        CapacitySummary capacity = new CapacitySummary(1L, "Backend", List.of());

        when(bootcampPersistencePort.findById(bootcampId)).thenReturn(Mono.just(bootcamp));
        when(bootcampPersistencePort.findCapacityIdsByBootcampId(bootcampId))
                .thenReturn(Flux.just(1L, 2L));
        when(capacityExternalServicePort.getCapacitiesWithTechnologies(List.of(1L, 2L), messageId))
                .thenReturn(Flux.just(capacity));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.getBootcampById(bootcampId, messageId))
                .expectNextMatches(result ->
                    result.id().equals(bootcampId) &&
                    result.name().equals("Java Bootcamp")
                )
                .verifyComplete();

        verify(bootcampPersistencePort).findById(bootcampId);
    }

    @Test
    void getBootcampById_WithNonExistingId_ShouldThrowBusinessException() {
        // Arrange
        Long bootcampId = 999L;
        when(bootcampPersistencePort.findById(bootcampId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(bootcampUseCase.getBootcampById(bootcampId, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_NOT_FOUND)
                .verify();
    }

    @Test
    void deleteBootcamp_WithNonExistingBootcamp_ShouldThrowBusinessException() {
        // Arrange
        Long bootcampId = 999L;
        when(bootcampPersistencePort.findById(bootcampId)).thenReturn(Mono.empty());

        // Act & Assert
        StepVerifier.create(bootcampUseCase.deleteBootcamp(bootcampId, messageId))
                .expectErrorMatches(throwable -> throwable instanceof BusinessException &&
                        ((BusinessException) throwable).getTechnicalMessage() == TechnicalMessage.BOOTCAMP_NOT_FOUND)
                .verify();

        verify(bootcampPersistencePort, never()).deleteById(anyLong());
    }

    @Test
    void checkBootcampsExist_WithExistingIds_ShouldReturnAllTrue() {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        when(bootcampPersistencePort.findExistingIdsByIds(ids)).thenReturn(Flux.just(1L, 2L));

        // Act & Assert
        StepVerifier.create(bootcampUseCase.checkBootcampsExist(ids, messageId))
                .expectNextMatches(result ->
                    result.size() == 2 &&
                    result.get(1L) &&
                    result.get(2L)
                )
                .verifyComplete();
    }
}
