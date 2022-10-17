package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.AuthenticationHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class AuthenticationConfig {

    @Bean
    RouterFunction<ServerResponse> authenticationRoute(AuthenticationHandler handler) {
        return route()
                .path("v1", builder -> builder
                        .path("signIn", b -> b
                                .POST(handler::signIn))
                        .path("/{userId}/activate", b -> b
                                .GET( handler::activate))
                        .path("forgotPassword", b -> b
                                .POST(handler::sendForgotPasswordEmail))
                        .path("setPassword", b -> b
                                .POST(handler::setFirstTimePassword)))
                .build();
    }
}
