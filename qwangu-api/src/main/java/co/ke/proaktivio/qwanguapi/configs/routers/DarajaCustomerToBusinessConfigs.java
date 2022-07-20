package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.DarajaCustomerToBusinessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class DarajaCustomerToBusinessConfigs {

    @Bean
    RouterFunction<ServerResponse> customerToBusinessUser(DarajaCustomerToBusinessHandler handler) {
        return route()
                .path("v1", builder -> builder
                        .path("mpesa/c2b", builder1 -> builder1
                                .POST("/validate", handler::validate)
                                .POST("/confirm", handler::confirm)
                        ))
                .build();
    }
}
