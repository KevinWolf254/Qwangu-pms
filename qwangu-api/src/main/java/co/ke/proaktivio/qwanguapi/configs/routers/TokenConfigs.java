package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.TokenHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

// TODO ADD SWAGGER DOCUMENTATION

@Configuration
public class TokenConfigs {

	@Bean
    RouterFunction<ServerResponse> routeToken(TokenHandler handler) {
        return route()
                .path("v1/tokens", builder -> builder
                        .GET(handler::find)
                ).build();
    }
}
