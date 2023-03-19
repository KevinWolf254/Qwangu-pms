package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.OneTimeTokenHandler;
import co.ke.proaktivio.qwanguapi.handlers.UserHandler;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;

@Configuration
public class OneTimeTokenConfigs {

    @Bean
    @RouterOperations(
            {
                @RouterOperation(
                        path = "/v1/tokens",
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        method = RequestMethod.GET, beanClass = UserHandler.class, beanMethod = "findAll",
                        operation = @Operation(
                                operationId = "findAll",
                                responses = {
                                        @ApiResponse(responseCode = "200", description = "Tokens found successfully.",
                                                content = @Content(schema = @Schema(implementation = Response.class)))
                                },
                                parameters = {
                                		@Parameter(in = ParameterIn.QUERY, name = "token"),
                                        @Parameter(in = ParameterIn.QUERY, name = "userId"),
                                        @Parameter(in = ParameterIn.QUERY, name = "order")
                                },
                                security = @SecurityRequirement(name = "Bearer authentication")
                        )
                )
            }
    )
    RouterFunction<ServerResponse> routeToken(OneTimeTokenHandler handler) {
        return route()
                .path("v1/tokens", builder -> builder
                        .GET(handler::findAll)
                ).build();
    }
}
