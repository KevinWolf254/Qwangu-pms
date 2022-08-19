package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.RentAdvanceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RentAdvanceConfigs {

    @Bean
    RouterFunction<ServerResponse> advanceRoute(RentAdvanceHandler handler) {
        return route()
                .path("v1/advances", builder -> builder
                        .GET(handler::find)
                        .POST(handler::create)
                        .PUT("/{id}", handler::update)
                        .DELETE("/{id}", handler::delete)
                ).build();
    }
}
