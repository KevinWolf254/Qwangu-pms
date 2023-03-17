package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.AuthenticationHandler;
import co.ke.proaktivio.qwanguapi.handlers.UserHandler;
import co.ke.proaktivio.qwanguapi.pojos.EmailDto;
import co.ke.proaktivio.qwanguapi.pojos.ResetPasswordDto;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.SignInDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
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
public class AuthenticationConfig {

    @Bean
    @RouterOperations(
            {
                @RouterOperation(
                        path = "/v1/sign-in",
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        method = RequestMethod.POST, beanClass = UserHandler.class, beanMethod = "signIn",
                        operation = @Operation(
                                operationId = "signIn",
                                requestBody = @RequestBody(content = @Content(
                                        schema = @Schema(implementation = SignInDto.class)
                                )),
                                responses = {
                                        @ApiResponse(responseCode = "200", description = "Signed in successfully.",
                                                content = @Content(schema = @Schema(implementation = Response.class))),
                                        @ApiResponse(responseCode = "400", description = "User id not found!",
                                                content = @Content(schema = @Schema(implementation = Response.class)))
                                }
                        )
                ),
                @RouterOperation(
                        path = "/v1/forgot-password",
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        method = RequestMethod.POST, beanClass = UserHandler.class, beanMethod = "sendForgotPasswordEmail",
                        operation = @Operation(
                                operationId = "sendForgotPasswordEmail",
                                requestBody = @RequestBody(content = @Content(
                                        schema = @Schema(implementation = EmailDto.class)
                                )),
                                responses = {
                                        @ApiResponse(responseCode = "200", description = "Email for password reset will be sent if email address exists.",
                                                content = @Content(schema = @Schema(implementation = Response.class)))
                                }
                        )
                ),
                @RouterOperation(
                        path = "/v1/password",
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        method = RequestMethod.POST, beanClass = UserHandler.class, beanMethod = "password",
                        operation = @Operation(
                                operationId = "setPassword",
                                requestBody = @RequestBody(content = @Content(
                                        schema = @Schema(implementation = ResetPasswordDto.class)
                                )),
                                responses = {
                                        @ApiResponse(responseCode = "200", description = "User password updated successfully.",
                                                content = @Content(schema = @Schema(implementation = Response.class))),
                                        @ApiResponse(responseCode = "400", description = "Token is required!",
                                        content = @Content(schema = @Schema(implementation = Response.class)))
                                },
                                parameters = {
                                        @Parameter(in = ParameterIn.QUERY, name = "token")
                                }
                        )
                ),
                @RouterOperation(
                        path = "/v1/activate",
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        method = RequestMethod.GET, beanClass = UserHandler.class, beanMethod = "activate",
                        operation = @Operation(
                                operationId = "activate",
                                responses = {
                                        @ApiResponse(responseCode = "200", description = "User activated successfully.",
                                                content = @Content(schema = @Schema(implementation = Response.class))),
                                        @ApiResponse(responseCode = "404", description = "User was not found!",
                                                content = @Content(schema = @Schema(implementation = Response.class)))
                                },
                                parameters = {
                                        @Parameter(in = ParameterIn.QUERY, name = "token")
                                },
                                security = @SecurityRequirement(name = "Bearer token")
                        )
                )
            	
            }
    )                
    RouterFunction<ServerResponse> authenticationRoute(AuthenticationHandler handler) {
        return route()
                .path("v1", builder -> builder
		                .path("activate", b -> b
		                        .GET(handler::activate))
                        .path("sign-in", b -> b
                                .POST(handler::signIn))
                        .path("forgot-password", b -> b
                                .POST(handler::sendForgotPasswordEmail))
                        .path("password", b -> b
                                .POST(handler::createPassword)))
                .build();
    }
}
