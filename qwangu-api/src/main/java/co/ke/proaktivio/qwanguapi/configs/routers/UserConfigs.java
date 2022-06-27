package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.UserHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class UserConfigs {

    @Bean
    RouterFunction<ServerResponse> routeUser(UserHandler handler) {
        return route()
                .path("v1", builder -> builder
                        .path("users", builder1 -> builder1
                                .GET(handler::find)
                                .GET("/{id}/activate", handler::activate)
                                .POST(handler::create)
                                .PUT("/{id}", handler::update)
                                .DELETE("/{id}", handler::delete))
                        .path("signIn", builder2 -> builder2
                                .POST(handler::signIn)))

                .build();
    }
}
