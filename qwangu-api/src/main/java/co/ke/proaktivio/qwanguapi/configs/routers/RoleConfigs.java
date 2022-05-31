package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.RoleHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class RoleConfigs {

    @Bean
    RouterFunction<ServerResponse> roleRoute(RoleHandler handler) {
        return route()
                .path("v1/roles", builder -> builder
                        .GET(handler::find)
                ).build();
    }
}
