package com.example.resilient_api.infrastructure.entrypoints;

import com.example.resilient_api.infrastructure.entrypoints.handler.BootcampHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {
    @Bean
    public RouterFunction<ServerResponse> routerFunction(BootcampHandlerImpl bootcampHandler) {
        return route(POST("/bootcamp"), bootcampHandler::createBootcamp)
            .andRoute(POST("/bootcamp/checking"), bootcampHandler::checkBootcampsExist)
            .andRoute(GET("/bootcamp"), bootcampHandler::listBootcamps)
            .andRoute(DELETE("/bootcamp/{id}"), bootcampHandler::deleteBootcamp);
    }

}
