package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.TokenHandler;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
//                                            @ApiResponse(responseCode = "404", description = "Token was not found!",
//                                                    content = @Content(schema = @Schema(implementation = Response.class)))
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.QUERY, name = "token")
//                                    },
//                                    security = @SecurityRequirement(name = "")
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
