package com.example.resilient_api.application.config;

import com.example.resilient_api.domain.api.BootcampServicePort;
import com.example.resilient_api.domain.api.EnrollmentServicePort;
import com.example.resilient_api.domain.spi.BootcampPersistencePort;
import com.example.resilient_api.domain.spi.CapacityExternalServicePort;
import com.example.resilient_api.domain.spi.EnrollmentPersistencePort;
import com.example.resilient_api.domain.spi.UserExternalServicePort;
import com.example.resilient_api.domain.usecase.BootcampUseCase;
import com.example.resilient_api.domain.usecase.EnrollmentUseCase;
import com.example.resilient_api.infrastructure.adapters.externalservice.CapacityExternalServiceAdapter;
import com.example.resilient_api.infrastructure.adapters.externalservice.UserExternalServiceAdapter;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.BootcampPersistenceAdapter;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.EnrollmentPersistenceAdapter;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.mapper.BootcampEntityMapper;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampCapacityRepository;
import com.example.resilient_api.infrastructure.adapters.persistenceadapter.repository.BootcampUserRepository;
import com.example.resilient_api.infrastructure.adapters.webclient.CapacityWebClient;
import com.example.resilient_api.infrastructure.adapters.webclient.UserWebClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {
    private final BootcampRepository bootcampRepository;
    private final BootcampCapacityRepository bootcampCapacityRepository;
    private final BootcampUserRepository bootcampUserRepository;
    private final BootcampEntityMapper bootcampEntityMapper;
    private final CapacityWebClient capacityWebClient;
    private final UserWebClient userWebClient;
    private final DatabaseClient databaseClient;

    @Bean
    public BootcampPersistencePort bootcampPersistencePort() {
        return new BootcampPersistenceAdapter(bootcampRepository, bootcampCapacityRepository,
                bootcampEntityMapper, databaseClient);
    }

    @Bean
    public CapacityExternalServicePort capacityExternalServicePort() {
        return new CapacityExternalServiceAdapter(capacityWebClient);
    }

    @Bean
    public UserExternalServicePort userExternalServicePort() {
        return new UserExternalServiceAdapter(userWebClient);
    }

    @Bean
    public EnrollmentPersistencePort enrollmentPersistencePort() {
        return new EnrollmentPersistenceAdapter(bootcampUserRepository, bootcampRepository, bootcampEntityMapper);
    }

    @Bean
    public BootcampServicePort bootcampServicePort(BootcampPersistencePort bootcampPersistencePort,
                                                    CapacityExternalServicePort capacityExternalServicePort) {
        return new BootcampUseCase(bootcampPersistencePort, capacityExternalServicePort);
    }

    @Bean
    public EnrollmentServicePort enrollmentServicePort(EnrollmentPersistencePort enrollmentPersistencePort,
                                                        BootcampPersistencePort bootcampPersistencePort,
                                                        UserExternalServicePort userExternalServicePort) {
        return new EnrollmentUseCase(enrollmentPersistencePort, bootcampPersistencePort, userExternalServicePort);
    }
}
