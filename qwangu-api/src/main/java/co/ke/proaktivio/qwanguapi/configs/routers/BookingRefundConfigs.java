package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.BookingRefundHandler;
import co.ke.proaktivio.qwanguapi.handlers.RentAdvanceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BookingRefundConfigs {

    @Bean
    RouterFunction<ServerResponse> refundRoute(BookingRefundHandler handler) {
        return route()
                .path("v1/refunds", builder -> builder
                        .GET(handler::find)
                        .POST(handler::create)
                        .DELETE("/{id}", handler::delete)
                ).build();
    }
}
