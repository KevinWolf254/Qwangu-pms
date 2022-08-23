package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.BookingRefundHandler;
import co.ke.proaktivio.qwanguapi.handlers.DarajaCustomerToBusinessHandler;
import co.ke.proaktivio.qwanguapi.pojos.BookingRefundDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
public class DarajaCustomerToBusinessConfigs {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/v1/mpesa/c2b/validate",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = DarajaCustomerToBusinessHandler.class, beanMethod = "validate",
                            operation = @Operation(
                                    operationId = "validate",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = DarajaCustomerToBusinessDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Validate successfully.",
                                                    content = @Content(schema = @Schema(implementation = DarajaCustomerToBusinessResponse.class)))
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/mpesa/c2b/confirm",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = DarajaCustomerToBusinessHandler.class, beanMethod = "confirm",
                            operation = @Operation(
                                    operationId = "confirm",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = DarajaCustomerToBusinessDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Created successfully.",
                                                    content = @Content(schema = @Schema(implementation = DarajaCustomerToBusinessResponse.class)))
                                    }
                            )
                    ),
            }
    )
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
