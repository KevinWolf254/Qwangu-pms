package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.UserHandler;
import co.ke.proaktivio.qwanguapi.pojos.*;
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
public class UserConfigs {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/v1/users",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = UserHandler.class, beanMethod = "find",
                            operation = @Operation(
                                    operationId = "find",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Users found successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Users were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "userId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "emailAddress"),
                                            @Parameter(in = ParameterIn.QUERY, name = "page"),
                                            @Parameter(in = ParameterIn.QUERY, name = "pageSize"),
                                            @Parameter(in = ParameterIn.QUERY, name = "order")
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/users",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = UserHandler.class, beanMethod = "create",
                            operation = @Operation(
                                    operationId = "create",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "201", description = "User created successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description = "User already exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "User were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/users/{userId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.PUT, beanClass = UserHandler.class, beanMethod = "update",
                            operation = @Operation(
                                    operationId = "update",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UserDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "User updated successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description = "User already exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "userId")},
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/users/{userId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.DELETE, beanClass = UserHandler.class, beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "User deleted successfully.",
                                                    content = @Content(schema = @Schema(implementation = Boolean.class))),
                                            @ApiResponse(responseCode = "400", description = "User does not exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "User was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "userId")},
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/users/{userId}/activate",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = UserHandler.class, beanMethod = "activate",
                            operation = @Operation(
                                    operationId = "activate",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "User updated successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "User was not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "userId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "token")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/users/sendResetPassword",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = UserHandler.class, beanMethod = "sendResetPassword",
                            operation = @Operation(
                                    operationId = "sendResetPassword",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Email for password reset will be sent if email address exists.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description = "Token is required!",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description = "Token is required!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.QUERY, name = "token")}
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/resetPassword",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = UserHandler.class, beanMethod = "resetPassword",
                            operation = @Operation(
                                    operationId = "resetPassword",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "User password updated successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description = "Token is required!",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description = "Token is required!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.QUERY, name = "token")}
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/users/{userId}/changePassword",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = UserHandler.class, beanMethod = "changePassword",
                            operation = @Operation(
                                    operationId = "changePassword",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = PasswordDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "User updated successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description = "User id not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "userId")},
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/signIn",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = UserHandler.class, beanMethod = "signIn",
                            operation = @Operation(
                                    operationId = "signIn",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = SignInDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Signed in successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description = "User id not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    }
                            )
                    )
            }
    )
    RouterFunction<ServerResponse> routeUser(UserHandler handler) {
        return route()
                .path("v1", builder -> builder
                        .path("users", b -> b
                                .GET("/{userId}/activate", handler::activate)
                                .GET(handler::find)
                                .POST("/sendResetPassword", handler::sendResetPassword)
                                .POST("/{userId}/changePassword", handler::changePassword)
                                .POST(handler::create)
                                .PUT("/{userId}", handler::update)
                                .DELETE("/{userId}", handler::delete)
                        )
                        .path("signIn", b -> b
                                .POST(handler::signIn))
                        .path("resetPassword", b -> b
                                .POST(handler::resetPassword)))
                .build();
    }
}
