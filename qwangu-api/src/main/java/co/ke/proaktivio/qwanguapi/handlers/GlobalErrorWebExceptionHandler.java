package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.Response;
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
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends AbstractErrorWebExceptionHandler {

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
                    ), Response.class);
        }
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
                    ), Response.class);
        }
        if (e instanceof UsernameNotFoundException || e instanceof AccessDeniedException) {
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
                    ), Response.class);
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
                ), Response.class);
    }
}
