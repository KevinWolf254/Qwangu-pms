package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.MpesaPaymentHandler;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentResponse;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
public class MpesaPaymentConfigs {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/v1/payments/mpesa/validate",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = MpesaPaymentHandler.class, beanMethod = "validate",
                            operation = @Operation(
                                    operationId = "validate",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = MpesaPaymentDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Validated successfully.",
                                                    content = @Content(schema = @Schema(implementation = MpesaPaymentResponse.class)))
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/payments/mpesa",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = MpesaPaymentHandler.class, beanMethod = "create",
                            operation = @Operation(
                                    operationId = "confirm",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = MpesaPaymentDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Created successfully.",
                                                    content = @Content(schema = @Schema(implementation = MpesaPaymentResponse.class)))
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/payments/mpesa/{mpesaPaymentId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = MpesaPaymentHandler.class, beanMethod = "findById",
                            operation = @Operation(
                                    operationId = "findById",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Payment found successfully.",
                                                    content = @Content(schema = @Schema(implementation = Boolean.class))),
                                            @ApiResponse(responseCode = "404", description = "Payment was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "mpesaPaymentId")},
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/payments/mpesa",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = MpesaPaymentHandler.class, beanMethod = "findAll",
                            operation = @Operation(
                                    operationId = "findAll",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Payments found successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Payments were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "transactionId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "referenceNumber"),
                                            @Parameter(in = ParameterIn.QUERY, name = "shortCode"),
                                            @Parameter(in = ParameterIn.QUERY, name = "order")
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    )
            }
    )
	RouterFunction<ServerResponse> mpesaPaymentRoute(MpesaPaymentHandler handler) {
		return route()
				.path("v1/payments/mpesa", builder -> builder
					.GET("/{mpesaPaymentId}",handler::findById)
					.GET(handler::findAll)
					.POST("/validate", handler::validate)
					.POST(handler::create)
				)
				.build();
	}
}
