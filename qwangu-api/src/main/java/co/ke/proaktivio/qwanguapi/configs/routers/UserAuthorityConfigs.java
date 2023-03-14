package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.UserAuthorityHandler;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
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
public class UserAuthorityConfigs {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/v1/authorities",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = UserAuthorityHandler.class, beanMethod = "findAll",
                            operation = @Operation(
                                    operationId = "findAll",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Authorities found successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Authorities were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "userRoleId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "order")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/authorities/{userAuthorityId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = UserAuthorityHandler  .class, beanMethod = "findById",
                            operation = @Operation(
                                    operationId = "findById",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Authority found successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Authority was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "userAuthorityId")}
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/authorities/{userAuthorityId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.PUT, beanClass = UserAuthorityHandler.class, beanMethod = "update",
                            operation = @Operation(
                                    operationId = "update",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserRoleDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Authority updated successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Authority was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "userAuthorityId")}
//                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/authorities/{userAuthorityId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.DELETE, beanClass = UserAuthorityHandler.class, beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Authority deleted successfully.",
                                                    content = @Content(schema = @Schema(implementation = Boolean.class))),
                                            @ApiResponse(responseCode = "404", description = "Authority was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "userAuthorityId")}
//                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    )
            }
    )
    RouterFunction<ServerResponse> userAuthorityRoute(UserAuthorityHandler handler) {
        return route()
                .path("v1/authorities", builder -> builder
                		.GET("/{userAuthorityId}", handler::findById)
                        .GET(handler::findAll)
                        .PUT("/{userAuthorityId}", handler::update)
                        .DELETE("/{userAuthorityId}", handler::delete)
                ).build();
    }
}
