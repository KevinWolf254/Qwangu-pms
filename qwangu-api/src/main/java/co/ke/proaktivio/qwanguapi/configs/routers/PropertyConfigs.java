package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.PropertyHandler;
import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.pojos.PropertyDto;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PropertyConfigs {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/v1/properties",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = PropertyHandler.class, beanMethod = "create",
                            operation = @Operation(
                                    operationId = "create",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = PropertyDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Properties created successfully.",
                                                    content = @Content(schema = @Schema(implementation = Property.class))),
                                            @ApiResponse(responseCode = "400", description = "Property already exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Properties were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/properties/{propertyId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.PUT, beanClass = PropertyHandler.class, beanMethod = "update",
                            operation = @Operation(
                                    operationId = "update",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = PropertyDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Properties updated successfully.",
                                                    content = @Content(schema = @Schema(implementation = Property.class))),
                                            @ApiResponse(responseCode = "400", description = "Property already exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Property was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "propertyId")},
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/properties/{propertyId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = PropertyHandler.class, beanMethod = "findById",
                            operation = @Operation(
                                    operationId = "findById",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Property found successfully.",
                                                    content = @Content(schema = @Schema(implementation = Property.class))),
                                            @ApiResponse(responseCode = "400", description = "Property does not exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Property was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "propertyId")},
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/properties",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = PropertyHandler.class, beanMethod = "findAll",
                            operation = @Operation(
                                    operationId = "findAll",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Properties found successfully.",
                                    				content = @Content(array = @ArraySchema(schema = @Schema(implementation = Property.class)))),
                                            @ApiResponse(responseCode = "404", description = "Properties were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "type"),
                                            @Parameter(in = ParameterIn.QUERY, name = "name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "order")
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    )
            }
    )
    RouterFunction<ServerResponse> apartmentRoute(PropertyHandler handler) {
        return route()
                .path("v1/properties", builder -> builder
                        .GET(handler::findAll)
                        .POST(handler::create)
                        .PUT("/{propertyId}", handler::update)
                        .GET("/{propertyId}", handler::findById)
                ).build();
    }
}
