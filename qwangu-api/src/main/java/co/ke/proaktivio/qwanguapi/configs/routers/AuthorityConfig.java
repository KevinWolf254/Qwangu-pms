package co.ke.proaktivio.qwanguapi.configs.routers;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.ke.proaktivio.qwanguapi.handlers.AuthorityHandler;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.responses.AuthoritiesResponse;
import co.ke.proaktivio.qwanguapi.pojos.responses.AuthorityResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@Configuration
public class AuthorityConfig {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/v1/authorities",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = AuthorityHandler.class, beanMethod = "findAll",
                            operation = @Operation(
                                    operationId = "findAll",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Authority found successfully.",
                                                    content = @Content(schema = @Schema(implementation = AuthoritiesResponse.class))),
                                            @ApiResponse(responseCode = "404", description = "Authority were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "order")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/authorities/{authorityId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = AuthorityHandler.class, beanMethod = "findById",
                            operation = @Operation(
                                    operationId = "findById",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Authority found successfully.",
                                                    content = @Content(schema = @Schema(implementation = AuthorityResponse.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "authorityId")}
                            )
                    )
            }
    )
    RouterFunction<ServerResponse> routeAuthority(AuthorityHandler handler) {
        return route()
                .path("v1", builder -> builder
                        .path("authorities", b -> b
                                .GET("/{authorityId}", handler::findById)
                                .GET(handler::findAll)))
                .build();
    }
}
