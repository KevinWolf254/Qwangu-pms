package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UserService;
import co.ke.proaktivio.qwanguapi.validators.EmailDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.ResetPasswordDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.SignInDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

import static co.ke.proaktivio.qwanguapi.utils.CustomUserHandlerValidatorUtil.*;

@Log4j2
@Component
@RequiredArgsConstructor
public class AuthenticationHandler {
    private final UserService userService;

    public Mono<ServerResponse> signIn(ServerRequest request) {
        return request
                .bodyToMono(SignInDto.class)
                .doOnSuccess(a -> log.info(" Request to sign in {}", a.getUsername()))
                .map(validateSignInDtoFunc(new SignInDtoValidator()))
                .flatMap(userService::signIn)
                .doOnSuccess(t -> log.info("Signed in successfully"))
                .doOnError(e -> log.error(" Failed to sign in. Error ", e))
                .flatMap(tokenDto ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Signed in successfully.",
                                        tokenDto)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for signing in", a.rawStatusCode()));
    }

    public Mono<ServerResponse> sendForgotPasswordEmail(ServerRequest request) {
        return request
                .bodyToMono(EmailDto.class)
                .doOnSuccess(a -> log.info(" Request to create {}", a))
                .map(validateEmailDtoFunc(new EmailDtoValidator()))
                .flatMap(userService::sendForgotPasswordEmail)
                .doOnSuccess($ -> log.info(" Reset password request sent"))
                .doOnError(e -> log.error(" Failed to send reset password request. Error ", e))
                .then(
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,
                                        "Email for password reset will be sent if email address exists.",
                                        null)), Response.class))
                .onErrorResume(e -> {
                    if (e instanceof CustomNotFoundException) {
                        return ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.BAD_REQUEST.value(),true,
                                        "Email for password reset will be sent if email address exists.",
                                        null)), Response.class);
                    }
                    return Mono.error(e);
                })
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for resetting password", a.rawStatusCode()));
    }

    public Mono<ServerResponse> setFirstTimePassword(ServerRequest request) {
        Optional<String> tokenOpt = request.queryParam("token");
        return Mono.just(tokenOpt)
                .doOnSuccess(a -> log.info(" Request to reset password"))
                .filter(t -> t.isPresent() && !t.get().trim().isEmpty() && !t.get().trim().isBlank())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token is required!")))
                .map(Optional::get)
                .flatMap(token -> request
                        .bodyToMono(ResetPasswordDto.class)
                        .map(validateResetPasswordDtoFunc(new ResetPasswordDtoValidator()))
                        .flatMap(dto -> userService.resetPassword(token, dto.getPassword())))
                .doOnSuccess(u -> log.info(" Reset password successful for {}", u.getEmailAddress()))
                .doOnError(e -> log.error(" Failed to reset password. Error ", e))
                .map(UserWithoutPasswordDto::new)
                .flatMap(user ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,
                                        "User password updated successfully.", user)), Response.class));
    }

    public Mono<ServerResponse> activate(ServerRequest request) {
        String id = request.pathVariable("userId");
        Optional<String> tokenOpt = request.queryParam("token");
        log.info(" Request to activate user with id {}", id);
        return Mono.just(tokenOpt)
                .filter(t -> t.isPresent() && !t.get().trim().isEmpty() && !t.get().trim().isBlank())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token is required!")))
                .map(Optional::get)
                .flatMap(token -> userService.activate(token, id))
                .doOnSuccess(u -> log.info(" Activated {} successfully", u))
                .doOnError(e -> log.error(" Failed to activate user. Error ", e))
                .map(UserWithoutPasswordDto::new)
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "User updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating user", a.rawStatusCode()));

    }
}
