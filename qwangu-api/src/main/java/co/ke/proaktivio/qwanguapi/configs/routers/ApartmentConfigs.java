package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.ApartmentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ApartmentConfigs {

    @Bean
    RouterFunction<ServerResponse> apartmentRoute(ApartmentHandler handler) {
        return route(
                POST("v1/apartments"), handler::create).andRoute(
                PUT("v1/apartments/{id}"), handler::update).andRoute(
                GET("v1/apartments"), handler::find).andRoute(
                DELETE("v1/apartments/{id}"), handler::delete);
    }
}
