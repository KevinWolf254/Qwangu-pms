package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.AuthorityHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthorityConfigs {

    @Bean
    RouterFunction<ServerResponse> authorityRoute(AuthorityHandler handler) {
        return route()
                .path("v1/authorities", builder -> builder
                        .GET(handler::find)
                ).build();
    }
}
