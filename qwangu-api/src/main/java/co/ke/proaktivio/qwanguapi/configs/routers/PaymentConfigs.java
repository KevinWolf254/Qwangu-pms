package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.OccupationHandler;
import co.ke.proaktivio.qwanguapi.handlers.PaymentHandler;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class PaymentConfigs {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/v1/payments/{paymentId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = OccupationHandler.class, beanMethod = "findById",
                            operation = @Operation(
                                    operationId = "findById",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Payment found successfully.",
                                                    content = @Content(schema = @Schema(implementation = Boolean.class))),
                                            @ApiResponse(responseCode = "404", description = "Payment was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "paymentId")}
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/payments",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = OccupationHandler.class, beanMethod = "find",
                            operation = @Operation(
                                    operationId = "find",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Payments found successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Payments were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "type"),
                                            @Parameter(in = ParameterIn.QUERY, name = "shortCode"),
                                            @Parameter(in = ParameterIn.QUERY, name = "referenceNo"),
                                            @Parameter(in = ParameterIn.QUERY, name = "mobileNumber"),
                                            @Parameter(in = ParameterIn.QUERY, name = "page"),
                                            @Parameter(in = ParameterIn.QUERY, name = "pageSize"),
                                            @Parameter(in = ParameterIn.QUERY, name = "order")
                                    }
                            )
                    ),
            }
    )
    RouterFunction<ServerResponse> paymentRoute(PaymentHandler handler) {
        return route()
                .path("v1/payments", builder -> builder
                        .GET(handler::find)
                        .GET("/{paymentId}", handler::findById)
                ).build();
    }
}
