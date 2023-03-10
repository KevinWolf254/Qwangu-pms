package co.ke.proaktivio.qwanguapi.configs.routers;

import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.ke.proaktivio.qwanguapi.handlers.ReceiptHandler;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// TODO GENERATE SWAGGER DOCUMENTATION

@Configuration
public class ReceiptConfigs {

	@Bean
	RouterFunction<ServerResponse> routeReceipt(ReceiptHandler handler) {
        return route()
                .path("v1", builder -> builder
                        .path("receipts", b -> b
                                .GET("/{receiptId}", handler::findById)
                                .GET(handler::findAll)
                                .POST(handler::create)))
                .build();
    }
}
