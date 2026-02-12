package com.example.resilient_api.infrastructure.entrypoints;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.BootcampEnrollment;
import com.example.resilient_api.domain.spi.BootcampPersistencePort;
import com.example.resilient_api.domain.spi.EnrollmentPersistencePort;
import com.example.resilient_api.domain.spi.UserExternalServicePort;
import com.example.resilient_api.infrastructure.entrypoints.dto.EnrollmentRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class EnrollmentIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private EnrollmentPersistencePort enrollmentPersistencePort;

    @MockBean
    private BootcampPersistencePort bootcampPersistencePort;

    @MockBean
    private UserExternalServicePort userExternalServicePort;

    private Bootcamp validBootcamp;
    private BootcampEnrollment enrollment;

    @BeforeEach
    void setUp() {
        validBootcamp = new Bootcamp(
                1L,
                "Java Bootcamp",
                "Java training",
                LocalDate.now().plusDays(30),
                90,
                List.of(1L, 2L)
        );

        enrollment = new BootcampEnrollment(1L, 1L, 100L, LocalDateTime.now());
    }

    @Test
    void enrollUser_WithValidData_ShouldReturn201() {
        // Arrange
        EnrollmentRequestDTO request = EnrollmentRequestDTO.builder()
                .bootcampId(1L)
                .build();

        when(userExternalServicePort.checkUsersExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(100L, true)));
        when(bootcampPersistencePort.findById(1L)).thenReturn(Mono.just(validBootcamp));
        when(enrollmentPersistencePort.isUserEnrolled(anyLong(), anyLong())).thenReturn(Mono.just(false));
        when(enrollmentPersistencePort.countEnrollmentsByUserId(anyLong())).thenReturn(Mono.just(2L));
        when(enrollmentPersistencePort.findBootcampsByUserId(anyLong())).thenReturn(Flux.empty());
        when(enrollmentPersistencePort.enrollUser(anyLong(), anyLong())).thenReturn(Mono.just(enrollment));

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp/enroll")
                .header("Authorization", "Bearer test-token")
                .header("X-User-Id", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.bootcampId").isEqualTo(1)
                .jsonPath("$.userId").isEqualTo(100);
    }

    @Test
    void enrollUser_WithNonExistingBootcamp_ShouldReturn404() {
        // Arrange
        EnrollmentRequestDTO request = EnrollmentRequestDTO.builder()
                .bootcampId(999L)
                .build();

        when(userExternalServicePort.checkUsersExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(100L, true)));
        when(bootcampPersistencePort.findById(999L)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp/enroll")
                .header("X-User-Id", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Bootcamp not found");
    }

    @Test
    void enrollUser_WhenAlreadyEnrolled_ShouldReturn400() {
        // Arrange
        EnrollmentRequestDTO request = EnrollmentRequestDTO.builder()
                .bootcampId(1L)
                .build();

        when(userExternalServicePort.checkUsersExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(100L, true)));
        when(bootcampPersistencePort.findById(1L)).thenReturn(Mono.just(validBootcamp));
        when(enrollmentPersistencePort.isUserEnrolled(1L, 100L)).thenReturn(Mono.just(true));

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp/enroll")
                .header("X-User-Id", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User is already enrolled in this bootcamp");
    }

    @Test
    void enrollUser_WhenMaxBootcampsReached_ShouldReturn400() {
        // Arrange
        EnrollmentRequestDTO request = EnrollmentRequestDTO.builder()
                .bootcampId(1L)
                .build();

        when(userExternalServicePort.checkUsersExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(100L, true)));
        when(bootcampPersistencePort.findById(1L)).thenReturn(Mono.just(validBootcamp));
        when(enrollmentPersistencePort.isUserEnrolled(anyLong(), anyLong())).thenReturn(Mono.just(false));
        when(enrollmentPersistencePort.countEnrollmentsByUserId(100L)).thenReturn(Mono.just(5L));

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp/enroll")
                .header("X-User-Id", "100")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User has reached the maximum number of bootcamps (5)");
    }

    @Test
    void enrollUser_WithoutUserIdHeader_ShouldReturn400() {
        // Arrange
        EnrollmentRequestDTO request = EnrollmentRequestDTO.builder()
                .bootcampId(1L)
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp/enroll")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unauthorized action");
    }

    @Test
    void unenrollUser_WithValidData_ShouldReturn200() {
        // Arrange
        when(enrollmentPersistencePort.isUserEnrolled(1L, 100L)).thenReturn(Mono.just(true));
        when(enrollmentPersistencePort.unenrollUser(1L, 100L)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.delete()
                .uri("/bootcamp/1/user/100")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void unenrollUser_WhenNotEnrolled_ShouldReturn404() {
        // Arrange
        when(enrollmentPersistencePort.isUserEnrolled(1L, 100L)).thenReturn(Mono.just(false));

        // Act & Assert
        webTestClient.delete()
                .uri("/bootcamp/1/user/100")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Enrollment not found");
    }

    @Test
    void getUserBootcamps_WithValidUser_ShouldReturn200() {
        // Arrange
        Bootcamp bootcamp1 = new Bootcamp(1L, "Java Bootcamp", "Description",
                LocalDate.now(), 90, List.of(1L));
        Bootcamp bootcamp2 = new Bootcamp(2L, "Python Bootcamp", "Description",
                LocalDate.now(), 60, List.of(2L));

        when(userExternalServicePort.checkUsersExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(100L, true)));
        when(enrollmentPersistencePort.findBootcampsByUserId(100L))
                .thenReturn(Flux.just(bootcamp1, bootcamp2));

        // Act & Assert
        webTestClient.get()
                .uri("/bootcamp/user/100")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].name").isEqualTo("Java Bootcamp")
                .jsonPath("$[1].name").isEqualTo("Python Bootcamp");
    }

    @Test
    void getUserBootcamps_WithNonExistingUser_ShouldReturn404() {
        // Arrange
        when(userExternalServicePort.checkUsersExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(999L, false)));

        // Act & Assert
        webTestClient.get()
                .uri("/bootcamp/user/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("User not found");
    }

    @Test
    void getUserIdsByBootcampId_WithValidBootcamp_ShouldReturn200() {
        // Arrange
        when(enrollmentPersistencePort.findUserIdsByBootcampId(1L))
                .thenReturn(Flux.just(100L, 200L, 300L));

        // Act & Assert
        webTestClient.get()
                .uri("/bootcamp/1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(3)
                .jsonPath("$[0]").isEqualTo(100)
                .jsonPath("$[1]").isEqualTo(200)
                .jsonPath("$[2]").isEqualTo(300);
    }

    @Test
    void getUserIdsByBootcampId_WithNoEnrollments_ShouldReturn200() {
        // Arrange
        when(enrollmentPersistencePort.findUserIdsByBootcampId(1L))
                .thenReturn(Flux.empty());

        // Act & Assert
        webTestClient.get()
                .uri("/bootcamp/1/users")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }
}
