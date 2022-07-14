package co.ke.proaktivio.qwanguapi.utils;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.ErrorCode;
import co.ke.proaktivio.qwanguapi.pojos.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.function.Function;

public class CustomErrorUtil {

    public static Function<Throwable, Mono<? extends ServerResponse>> handleExceptions() {
        return e -> handleExceptions(e);
    }

    public static Mono<ServerResponse> handleExceptions(Throwable e) {
        if (e instanceof CustomAlreadyExistsException || e instanceof CustomBadRequestException) {
            return ServerResponse.badRequest()
                    .body(Mono.just(
                            new ErrorResponse<>(false, ErrorCode.BAD_REQUEST_ERROR, "Bad request.", e.getMessage())), ErrorResponse.class)
                    .log();
        }
        if (e instanceof CustomNotFoundException) {
            return ServerResponse.status(HttpStatus.NOT_FOUND)
                    .body(Mono.just(
                            new ErrorResponse<>(false, ErrorCode.NOT_FOUND_ERROR, "Not found!", e.getMessage())), ErrorResponse.class)
                    .log();
        }
        if (e instanceof UsernameNotFoundException) {
            return ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .body(Mono.just(
                            new ErrorResponse<>(false, ErrorCode.UNAUTHORIZED_ERROR, "Unauthorised", e.getMessage())), ErrorResponse.class)
                    .log();
        }
        if (e instanceof MailException) {
            return ServerResponse.status(HttpStatus.BAD_REQUEST)
                    .body(Mono.just(
                            new ErrorResponse<>(false, ErrorCode.BAD_REQUEST_ERROR, "Bad request.", "Mail could not be sent!")), ErrorResponse.class)
                    .log();
        }
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Mono.just(
                        new ErrorResponse<>(false, ErrorCode.INTERNAL_SERVER_ERROR, "Something happened!", "Something happened!")), ErrorResponse.class)
                .log();
    }
}
