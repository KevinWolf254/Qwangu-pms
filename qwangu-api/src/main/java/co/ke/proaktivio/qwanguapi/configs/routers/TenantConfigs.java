package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.TenantHandler;
import co.ke.proaktivio.qwanguapi.handlers.UnitHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TenantConfigs {

    @Bean
    RouterFunction<ServerResponse> tenantRoute(TenantHandler handler) {
        return route()
                .path("v1/tenants", builder -> builder
                        .GET(handler::find)
                        .POST(handler::create)
                        .PUT("/{id}", handler::update)
                        .DELETE("/{id}", handler::delete)
                ).build();
    }
}
