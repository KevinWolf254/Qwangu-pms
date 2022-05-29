package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.ApartmentHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.server.RouterFunction;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ApartmentConfigs {

    @Bean
    RouterFunction<ServerResponse> apartmentRoute(ApartmentHandler handler) {
        return route()
                .path("v1/apartments", builder -> builder
                        .GET(handler::find)
                        .POST(handler::create)
                        .PUT("/{id}", handler::update)
                        .DELETE("/{id}", handler::delete)
                ).build();
    }
}
