package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.handlers.NoticeHandler;
import co.ke.proaktivio.qwanguapi.pojos.CreateNoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.UpdateNoticeDto;
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
public class NoticeConfigs {

    @Bean
    @RouterOperations(
            {
                @RouterOperation(
                        path = "/v1/notices/{noticeId}",
                        produces = MediaType.APPLICATION_JSON_VALUE,
                        method = RequestMethod.GET, beanClass = NoticeHandler.class, beanMethod = "findById",
                        operation = @Operation(
                                operationId = "findById",
                                responses = {
                                        @ApiResponse(responseCode = "200", description = "Notice found successfully.",
                                                content = @Content(schema = @Schema(implementation = Boolean.class))),
                                        @ApiResponse(responseCode = "404", description = "Notice was not found!",
                                        content = @Content(schema = @Schema(implementation = Response.class)))
                                },
                                parameters = {@Parameter(in = ParameterIn.PATH, name = "noticeId")},
                                security = @SecurityRequirement(name = "Bearer authentication")
                        )
                ),
                    @RouterOperation(
                            path = "/v1/notices",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.GET, beanClass = NoticeHandler.class, beanMethod = "findAll",
                            operation = @Operation(
                                    operationId = "findAll",
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Notices found successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Notices were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "noticeId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "isActive"),
                                            @Parameter(in = ParameterIn.QUERY, name = "occupationId"),
                                            @Parameter(in = ParameterIn.QUERY, name = "pageSize"),
                                            @Parameter(in = ParameterIn.QUERY, name = "order")
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/notices",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.POST, beanClass = NoticeHandler.class, beanMethod = "create",
                            operation = @Operation(
                                    operationId = "create",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = CreateNoticeDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Notices created successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description = "Notices already exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Notices were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    ),
                    @RouterOperation(
                            path = "/v1/notices/{noticeId}",
                            produces = MediaType.APPLICATION_JSON_VALUE,
                            method = RequestMethod.PUT, beanClass = NoticeHandler.class, beanMethod = "update",
                            operation = @Operation(
                                    operationId = "update",
                                    requestBody = @RequestBody(content = @Content(schema = @Schema(implementation = UpdateNoticeDto.class))),
                                    responses = {
                                            @ApiResponse(responseCode = "200", description = "Notice updated successfully.",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "400", description =
                                                    "Notice does not exists!/Notice already exists!",
                                                    content = @Content(schema = @Schema(implementation = Response.class))),
                                            @ApiResponse(responseCode = "404", description = "Notice were not found!",
                                                    content = @Content(schema = @Schema(implementation = Response.class)))
                                    },
                                    parameters = {@Parameter(in = ParameterIn.PATH, name = "noticeId")},
                                    security = @SecurityRequirement(name = "Bearer authentication")
                            )
                    )
            }
    )
    RouterFunction<ServerResponse> noticeRoute(NoticeHandler handler) {
        return route()
                .path("v1/notices", builder -> builder
                		.GET("/{noticeId}", handler::findById)
                        .GET(handler::findAll)
                        .POST(handler::create)
                        .PUT("/{noticeId}", handler::update)
                ).build();
    }
}
