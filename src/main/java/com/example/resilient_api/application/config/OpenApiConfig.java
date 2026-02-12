package com.example.resilient_api.application.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8082}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bootcamp Management API")
                        .version("1.0.0")
                        .description("""
                                API principal para la gestión de bootcamps.
                                
                                **Funcionalidades principales:**
                                - Registrar bootcamps
                                - Listar bootcamps paginados
                                - Consultar bootcamp por ID
                                - Eliminar bootcamps
                                - Inscripción y desinscripción de usuarios
                                - Consultar usuarios inscritos en un bootcamp
                                - Consultar bootcamps de un usuario
                                
                                **Autenticación:**
                                Algunos endpoints requieren autenticación JWT.
                                Usa el endpoint de login del microservicio users-api (puerto 8080) para obtener un token.
                                
                                **Nota:**
                                Este es el microservicio PRINCIPAL de bootcamps. 
                                El microservicio capacity-api (puerto 8082) actúa como API Gateway/BFF.
                                """)
                        .contact(new Contact()
                                .name("Bootcamp Development Team")
                                .email("support@bootcamp.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Development Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Introduce el token JWT obtenido del endpoint de login (users-api:8080)")));
    }
}
