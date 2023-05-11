package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.OneTimeTokenHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import org.springdoc.core.annotations.RouterOperations;

@Configuration
public class OneTimeTokenConfigs {

	// TODO - Look into why only one end point causes swagger to fail
    @Bean
    @RouterOperations(
            {
//                @RouterOperation(
//                        path = "/v1/tokens",
//                        produces = MediaType.APPLICATION_JSON_VALUE,
//                        method = RequestMethod.GET, beanClass = OneTimeTokenHandler.class, beanMethod = "findAll",
//                        operation = @Operation(
//                                operationId = "findAll",
//                                responses = {
//                                        @ApiResponse(responseCode = "200", description = "Tokens found successfully.",
//                                                content = @Content(schema = @Schema(implementation = Response.class))),
//                                        @ApiResponse(responseCode = "404", description = "Tokens were not found!",
//                                                content = @Content(schema = @Schema(implementation = Response.class)))
//                                },
//                                parameters = {
//                                        @Parameter(in = ParameterIn.QUERY, name = "token"),
//                                        @Parameter(in = ParameterIn.QUERY, name = "userId"),
//                                        @Parameter(in = ParameterIn.QUERY, name = "order")
//                                },
//                                security = @SecurityRequirement(name = "Bearer authentication")
//                        )
//                )
            }
    )
    RouterFunction<ServerResponse> routeToken(OneTimeTokenHandler handler) {
        return route()
                .path("v1/tokens", builder -> builder
                        .GET(handler::findAll)
                ).build();
    }
}
