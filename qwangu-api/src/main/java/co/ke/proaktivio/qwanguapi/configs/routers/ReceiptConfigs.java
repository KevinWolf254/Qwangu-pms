package co.ke.proaktivio.qwanguapi.configs.routers;

import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.ke.proaktivio.qwanguapi.handlers.ReceiptHandler;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.responses.ReceiptResponse;
import co.ke.proaktivio.qwanguapi.pojos.responses.ReceiptsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;

@Configuration
public class ReceiptConfigs {

	@Bean
    @RouterOperations(
    		{
                    @RouterOperation(
                            path = "/v1/receipts/{receiptId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = ReceiptHandler.class, beanMethod = "findById",
                            operation = @Operation(
                                    operationId = "findById",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Receipt found successfully.",
                                                    content = @Content(schema = @Schema(implementation = ReceiptResponse.class))),
                                            @ApiResponse(responseCode = "404", description = "Receipt was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "receiptId")},
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/receipts",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = ReceiptHandler.class, beanMethod = "findAll",
                            operation = @Operation(
                                    operationId = "findAll",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Receipts found successfully.",
                                                    content = @Content(schema = @Schema(implementation = ReceiptsResponse.class))),
                                            @ApiResponse(responseCode = "404", description = "Receipts were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "occupationId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "paymentId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "order")
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/receipts",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = ReceiptHandler.class, beanMethod = "create",
                            operation = @Operation(
                                    operationId = "create",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = ReceiptDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Receipt created successfully.",
                                                    content = @Content(schema = @Schema(implementation = ReceiptResponse.class))),
                                            @ApiResponse(responseCode = "400", description = "Receipts already exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    )
            }
    )
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
