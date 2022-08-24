package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.AuthorityHandler;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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
public class AuthorityConfigs {

    @Bean
//    @RouterOperations(
//            {
//                    @RouterOperation(
//                            path = "/v1/authorities",
//                            produces = MediaType.APPLICATION_JSON_VALUE,
//                            method = RequestMethod.GET, beanClass = AuthorityHandler.class, beanMethod = "find",
//                            operation = @Operation(
//                                    operationId = "find",
//                                    responses = {
//                                            @ApiResponse(responseCode = "200", description = "Authorities found successfully.",
//                                                    content = @Content(schema = @Schema(implementation = Response.class))),
//                                            @ApiResponse(responseCode = "404", description = "Authorities were not found!",
//                                                    content = @Content(schema = @Schema(implementation = Response.class)))
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.QUERY, name = "id"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "name"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "page"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "pageSize"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "order")
//                                    }
//                            )
//                    )
//            }
//    )
    RouterFunction<ServerResponse> userAuthorityRoute(AuthorityHandler handler) {
        return route()
                .path("v1/authorities", builder -> builder
                        .GET(handler::find)
                ).build();
    }
}
