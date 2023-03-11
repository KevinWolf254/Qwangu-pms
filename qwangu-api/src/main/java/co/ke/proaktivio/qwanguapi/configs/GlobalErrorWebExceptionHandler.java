package co.ke.proaktivio.qwanguapi.configs;

import co.ke.proaktivio.qwanguapi.exceptions.*;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.mail.MailException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler  extends AbstractErrorWebExceptionHandler {

    public GlobalErrorWebExceptionHandler(ErrorAttributes errorAttributes, WebProperties.Resources resources,
                                          ApplicationContext applicationContext, ServerCodecConfigurer serverCodecConfigurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageReaders(serverCodecConfigurer.getReaders());
        this.setMessageWriters(serverCodecConfigurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(
                RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Map<String, Object> errorPropertiesMap = getErrorAttributes(request, ErrorAttributeOptions.defaults());
        String path = (String) errorPropertiesMap.get("path");

        Throwable e = this.getError(request);
        if (e instanceof CustomAlreadyExistsException || e instanceof CustomBadRequestException ||
                e instanceof MailException) {
            return ServerResponse
                    .status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(
                            new Response<>(
                                    LocalDateTime.now().toString(),
                                    path,
                                    HttpStatus.BAD_REQUEST.value(),
                                    false,
                                    e instanceof MailException ? "Mail could not be sent!" : e.getMessage(), null)
                    ), Response.class)
                    .doOnSuccess(a -> log.debug("Sent response with status code {}", a.rawStatusCode()));
        }
        // TODO FOR ALL HANDLERS FIND BY ID RETURNS 404 WHEN RESULT IS NULL/EMPTY
        if (e instanceof CustomNotFoundException) {
            return ServerResponse
                    .status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(
                            new Response<>(
                                    LocalDateTime.now().toString(),
                                    path,
                                    HttpStatus.NOT_FOUND.value(),
                                    false,
                                    e.getMessage(), null)
                    ), Response.class)
                    .doOnSuccess(a -> log.debug("Sent response with status code {}", a.rawStatusCode()));
        }
        if (e instanceof UsernameNotFoundException || e instanceof JwtException) {
            return ServerResponse
                    .status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(
                            new Response<>(
                                    LocalDateTime.now().toString(),
                                    path,
                                    HttpStatus.UNAUTHORIZED.value(),
                                    false,
                                    e.getMessage(), null)
                    ), Response.class)
                    .doOnSuccess(a -> log.debug("Sent response with status code {}", a.rawStatusCode()));
        }
        if (e instanceof AccessDeniedException || e instanceof CustomAccessDeniedException) {
            return ServerResponse
                    .status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(
                            new Response<>(
                                    LocalDateTime.now().toString(),
                                    path,
                                    HttpStatus.FORBIDDEN.value(),
                                    false,
                                    e.getMessage(), null)
                    ), Response.class)
                    .doOnSuccess(a -> log.debug("Sent response with status code {}", a.rawStatusCode()));
        }
        if (e instanceof ResponseStatusException) {
            return ServerResponse
                    .status(((ResponseStatusException) e).getStatus())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Mono.just(
                            new Response<>(
                                    LocalDateTime.now().toString(),
                                    path,
                                    ((ResponseStatusException) e).getStatus().value(),
                                    false,
                                    e.getMessage(), null)
                    ), Response.class)
                    .doOnSuccess(a -> log.debug("Sent response with status code {}", a.rawStatusCode()));
        }
        return ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(
                        new Response<>(
                                LocalDateTime.now().toString(),
                                path,
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                false,
                                e.getMessage(), null)
                ), Response.class)
                .doOnSuccess(a -> log.debug("Sent response with status code {}", a.rawStatusCode()));
    }
}
