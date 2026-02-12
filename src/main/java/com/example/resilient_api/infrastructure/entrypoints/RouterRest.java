package com.example.resilient_api.infrastructure.entrypoints;

import com.example.resilient_api.infrastructure.entrypoints.handler.BootcampHandlerImpl;
import com.example.resilient_api.infrastructure.entrypoints.handler.EnrollmentHandlerImpl;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RouterRest {

    @Bean
    @RouterOperations({
        // Bootcamps
        @RouterOperation(path = "/bootcamp", method = RequestMethod.POST, beanClass = BootcampHandlerImpl.class, beanMethod = "createBootcamp"),
        @RouterOperation(path = "/bootcamp/checking", method = RequestMethod.POST, beanClass = BootcampHandlerImpl.class, beanMethod = "checkBootcampsExist"),
        @RouterOperation(path = "/bootcamp", method = RequestMethod.GET, beanClass = BootcampHandlerImpl.class, beanMethod = "listBootcamps"),
        @RouterOperation(path = "/bootcamp/{id}", method = RequestMethod.GET, beanClass = BootcampHandlerImpl.class, beanMethod = "getBootcampById"),
        @RouterOperation(path = "/bootcamp/{id}", method = RequestMethod.DELETE, beanClass = BootcampHandlerImpl.class, beanMethod = "deleteBootcamp"),
        // Inscripciones
        @RouterOperation(path = "/bootcamp/{id}/users", method = RequestMethod.GET, beanClass = EnrollmentHandlerImpl.class, beanMethod = "getUserIdsByBootcampId"),
        @RouterOperation(path = "/bootcamp/enroll", method = RequestMethod.POST, beanClass = EnrollmentHandlerImpl.class, beanMethod = "enrollUser"),
        @RouterOperation(path = "/bootcamp/{bootcampId}/user/{userId}", method = RequestMethod.DELETE, beanClass = EnrollmentHandlerImpl.class, beanMethod = "unenrollUser"),
        @RouterOperation(path = "/bootcamp/user/{userId}", method = RequestMethod.GET, beanClass = EnrollmentHandlerImpl.class, beanMethod = "getUserBootcamps")
    })
    public RouterFunction<ServerResponse> routerFunction(
            BootcampHandlerImpl bootcampHandler,
            EnrollmentHandlerImpl enrollmentHandler) {
        return route(POST("/bootcamp"), bootcampHandler::createBootcamp)
            .andRoute(POST("/bootcamp/checking"), bootcampHandler::checkBootcampsExist)
            .andRoute(GET("/bootcamp"), bootcampHandler::listBootcamps)
            .andRoute(GET("/bootcamp/{id}"), bootcampHandler::getBootcampById)
            .andRoute(GET("/bootcamp/{id}/users"), enrollmentHandler::getUserIdsByBootcampId)
            .andRoute(DELETE("/bootcamp/{id}"), bootcampHandler::deleteBootcamp)
            .andRoute(POST("/bootcamp/enroll"), enrollmentHandler::enrollUser)
            .andRoute(DELETE("/bootcamp/{bootcampId}/user/{userId}"), enrollmentHandler::unenrollUser)
            .andRoute(GET("/bootcamp/user/{userId}"), enrollmentHandler::getUserBootcamps);
    }

}
