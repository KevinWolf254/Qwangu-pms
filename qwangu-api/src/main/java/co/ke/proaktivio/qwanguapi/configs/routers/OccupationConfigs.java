package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.OccupationHandler;
import co.ke.proaktivio.qwanguapi.handlers.UserHandler;
import co.ke.proaktivio.qwanguapi.pojos.OccupationDto;
import co.ke.proaktivio.qwanguapi.pojos.OccupationForNewTenantDto;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.responses.OccupationResponse;
import co.ke.proaktivio.qwanguapi.pojos.responses.OccupationsResponse;
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
public class OccupationConfigs {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/v1/occupations",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = OccupationHandler.class, beanMethod = "create",
                            operation = @Operation(
                                    operationId = "create",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = OccupationForNewTenantDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Occupation created successfully.",
                                                    content = @Content(schema = @Schema(implementation = OccupationResponse.class))),
                                            @ApiResponse(responseCode = "400", description = "Occupation already exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/occupations/{occupationId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.DELETE, beanClass = OccupationHandler.class, beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Occupation deleted successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "occupationId")},
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/occupations/{occupationId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = OccupationHandler.class, beanMethod = "findById",
                            operation = @Operation(
                                    operationId = "findById",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Occupation found successfully.",
                                                    content = @Content(schema = @Schema(implementation = OccupationResponse.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "occupationId")}
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/occupations",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = OccupationHandler.class, beanMethod = "find",
                            operation = @Operation(
                                    operationId = "find",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Occupations found successfully.",
                                                    content = @Content(schema = @Schema(implementation = OccupationsResponse.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "unitId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "tenantId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "order")
                                    }
                            )
                    )
            }
    )
    RouterFunction<ServerResponse> occupationRoute(OccupationHandler handler) {
        return route()
                .path("v1/occupations", builder -> builder
                        .GET(handler::find)
                        .POST(handler::create)
                        .GET("/{occupationId}", handler::findById)
                        .DELETE("/{occupationId}", handler::delete)
                ).build();
    }
}
