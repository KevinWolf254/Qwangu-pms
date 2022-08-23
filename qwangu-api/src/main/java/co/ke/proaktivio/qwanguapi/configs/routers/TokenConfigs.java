package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.TokenHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class TokenConfigs {

    @Bean
//    @RouterOperations(
//            {
//                    @RouterOperation(
//                            path = "/v1/tokens",
//                            produces = MediaType.APPLICATION_JSON_VALUE,
//                            method = RequestMethod.GET, beanClass = TokenHandler.class, beanMethod = "find",
//                            operation = @Operation(
//                                    operationId = "find",
//                                    responses = {
//                                            @ApiResponse(responseCode = "200", description = "Token found successfully.",
//                                                    content = @Content(schema = @Schema(implementation = Response.class))),
//                                            @ApiResponse(responseCode = "400", description = "Token were not found!",
//                                                    content = @Content(schema = @Schema(implementation = Response.class))),
//                                            @ApiResponse(responseCode = "404", description = "Token was not found!",
//                                                    content = @Content(schema = @Schema(implementation = Response.class)))
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.QUERY, name = "token")
//                                    }
//                            )
//                    )
//            }
//    )
    RouterFunction<ServerResponse> routeToken(TokenHandler handler) {
        return route()
                .path("v1/token", builder -> builder
                        .GET(handler::find)
                ).build();
    }
}
