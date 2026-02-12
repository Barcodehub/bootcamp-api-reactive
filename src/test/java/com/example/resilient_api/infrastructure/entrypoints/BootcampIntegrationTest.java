package com.example.resilient_api.infrastructure.entrypoints;

import com.example.resilient_api.domain.model.Bootcamp;
import com.example.resilient_api.domain.model.CapacitySummary;
import com.example.resilient_api.domain.spi.BootcampPersistencePort;
import com.example.resilient_api.domain.spi.CapacityExternalServicePort;
import com.example.resilient_api.infrastructure.entrypoints.dto.BootcampDTO;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class BootcampIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private BootcampPersistencePort bootcampPersistencePort;

    @MockBean
    private CapacityExternalServicePort capacityExternalServicePort;

    private BootcampDTO validBootcampDTO;
    private Bootcamp savedBootcamp;

    @BeforeEach
    void setUp() {
        validBootcampDTO = BootcampDTO.builder()
                .name("Java Bootcamp")
                .description("Complete Java training")
                .launchDate(LocalDate.now().plusDays(30))
                .duration(90)
                .capacityIds(List.of(1L, 2L, 3L))
                .build();

        savedBootcamp = new Bootcamp(
                1L,
                "Java Bootcamp",
                "Complete Java training",
                LocalDate.now().plusDays(30),
                90,
                List.of(1L, 2L, 3L)
        );
    }

    @Test
    void createBootcamp_WithValidData_ShouldReturn201() {
        // Arrange
        when(bootcampPersistencePort.existByName(anyString())).thenReturn(Mono.just(false));
        when(capacityExternalServicePort.checkCapacitiesExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(1L, true, 2L, true, 3L, true)));
        when(bootcampPersistencePort.save(any(Bootcamp.class))).thenReturn(Mono.just(savedBootcamp));

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp")
                .header("Authorization", "Bearer test-token")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validBootcampDTO)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Java Bootcamp")
                .jsonPath("$.duration").isEqualTo(90);
    }

    @Test
    void createBootcamp_WithExistingName_ShouldReturn400() {
        // Arrange
        when(bootcampPersistencePort.existByName(anyString())).thenReturn(Mono.just(true));
        when(capacityExternalServicePort.checkCapacitiesExist(anyList(), anyString()))
                .thenReturn(Mono.just(Map.of(1L, true, 2L, true, 3L, true)));

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validBootcampDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Bootcamp with this name already exists");
    }

    @Test
    void createBootcamp_WithInvalidCapacityCount_ShouldReturn400() {
        // Arrange
        BootcampDTO invalidDTO = BootcampDTO.builder()
                .name("Java Bootcamp")
                .description("Description")
                .launchDate(LocalDate.now().plusDays(30))
                .duration(90)
                .capacityIds(List.of(1L, 2L, 3L, 4L, 5L)) // More than max (4)
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Bootcamp cannot have more than 4 capacities");
    }

    @Test
    void listBootcamps_WithDefaultPagination_ShouldReturn200() {
        // Arrange
        Bootcamp bootcamp = new Bootcamp(1L, "Java Bootcamp", "Description",
                LocalDate.now(), 90, List.of(1L, 2L));

        CapacitySummary capacity = new CapacitySummary(1L, "Backend", List.of());

        when(bootcampPersistencePort.count()).thenReturn(Mono.just(1L));
        when(bootcampPersistencePort.findAllPaginated(any())).thenReturn(Flux.just(bootcamp));
        when(bootcampPersistencePort.findCapacityIdsByBootcampId(1L)).thenReturn(Flux.just(1L, 2L));
        when(capacityExternalServicePort.getCapacitiesWithTechnologies(anyList(), anyString()))
                .thenReturn(Flux.just(capacity));

        // Act & Assert
        webTestClient.get()
                .uri("/bootcamp")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].name").isEqualTo("Java Bootcamp")
                .jsonPath("$.totalElements").isEqualTo(1);
    }

    @Test
    void getBootcampById_WithExistingId_ShouldReturn200() {
        // Arrange
        Bootcamp bootcamp = new Bootcamp(1L, "Java Bootcamp", "Description",
                LocalDate.now(), 90, List.of(1L));

        CapacitySummary capacity = new CapacitySummary(1L, "Backend", List.of());

        when(bootcampPersistencePort.findById(1L)).thenReturn(Mono.just(bootcamp));
        when(bootcampPersistencePort.findCapacityIdsByBootcampId(1L)).thenReturn(Flux.just(1L));
        when(capacityExternalServicePort.getCapacitiesWithTechnologies(anyList(), anyString()))
                .thenReturn(Flux.just(capacity));

        // Act & Assert
        webTestClient.get()
                .uri("/bootcamp/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(1)
                .jsonPath("$.name").isEqualTo("Java Bootcamp");
    }

    @Test
    void getBootcampById_WithNonExistingId_ShouldReturn404() {
        // Arrange
        when(bootcampPersistencePort.findById(999L)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.get()
                .uri("/bootcamp/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Bootcamp not found");
    }

    @Test
    void deleteBootcamp_WithExistingId_ShouldReturn200() {
        // Arrange
        Bootcamp bootcamp = new Bootcamp(1L, "Java Bootcamp", "Description",
                LocalDate.now(), 90, List.of(1L, 2L));

        when(bootcampPersistencePort.findById(1L)).thenReturn(Mono.just(bootcamp));
        when(bootcampPersistencePort.findCapacityIdsByBootcampId(1L)).thenReturn(Flux.just(1L, 2L));
        when(bootcampPersistencePort.deleteBootcampCapacitiesByBootcampId(1L)).thenReturn(Mono.empty());
        when(capacityExternalServicePort.deleteCapacitiesByIds(anyList(), anyString())).thenReturn(Mono.empty());
        when(bootcampPersistencePort.deleteById(1L)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.delete()
                .uri("/bootcamp/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("Bootcamp deleted successfully");
    }

    @Test
    void deleteBootcamp_WithNonExistingId_ShouldReturn404() {
        // Arrange
        when(bootcampPersistencePort.findById(999L)).thenReturn(Mono.empty());

        // Act & Assert
        webTestClient.delete()
                .uri("/bootcamp/999")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Bootcamp not found");
    }

    @Test
    void checkBootcampsExist_WithValidIds_ShouldReturn200() {
        // Arrange
        when(bootcampPersistencePort.findExistingIdsByIds(anyList()))
                .thenReturn(Flux.just(1L, 2L));

        Map<String, List<Long>> requestBody = Map.of("ids", List.of(1L, 2L));

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp/checking")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.1").isEqualTo(true)
                .jsonPath("$.2").isEqualTo(true);
    }

    @Test
    void createBootcamp_WithNullName_ShouldReturn400() {
        // Arrange
        BootcampDTO invalidDTO = BootcampDTO.builder()
                .name(null)
                .description("Description")
                .launchDate(LocalDate.now().plusDays(30))
                .duration(90)
                .capacityIds(List.of(1L, 2L, 3L))
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Bootcamp name is required");
    }

    @Test
    void createBootcamp_WithInvalidDuration_ShouldReturn400() {
        // Arrange
        BootcampDTO invalidDTO = BootcampDTO.builder()
                .name("Java Bootcamp")
                .description("Description")
                .launchDate(LocalDate.now().plusDays(30))
                .duration(0) // Invalid
                .capacityIds(List.of(1L, 2L, 3L))
                .build();

        // Act & Assert
        webTestClient.post()
                .uri("/bootcamp")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidDTO)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Bootcamp duration must be at least 1 day");
    }
}
