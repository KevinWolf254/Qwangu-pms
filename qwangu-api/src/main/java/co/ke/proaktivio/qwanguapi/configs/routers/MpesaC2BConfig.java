package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.MpesaC2BHandler;
import co.ke.proaktivio.qwanguapi.pojos.MpesaC2BDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaC2BResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
public class MpesaC2BConfig {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/v1/mpesa/v2/c2b/validate",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = MpesaC2BHandler.class, beanMethod = "validate",
                            operation = @Operation(
                                    operationId = "validate",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = MpesaC2BDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Validate successfully.",
                                                    content = @Content(schema = @Schema(implementation = MpesaC2BResponse.class)))
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/mpesa/v2/c2b/confirm",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = MpesaC2BHandler.class, beanMethod = "confirm",
                            operation = @Operation(
                                    operationId = "confirm",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = MpesaC2BDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Created successfully.",
                                                    content = @Content(schema = @Schema(implementation = MpesaC2BResponse.class)))
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
            }
    )
    RouterFunction<ServerResponse> customerToBusinessUser(MpesaC2BHandler handler) {
        return route()
                .path("v1/mpesa/v2", builder -> builder
                        .path("c2b", builder1 -> builder1
                                .POST("/validate", handler::validate)
                                .POST("/confirm", handler::confirm))
                        )
                .build();
    }
}
