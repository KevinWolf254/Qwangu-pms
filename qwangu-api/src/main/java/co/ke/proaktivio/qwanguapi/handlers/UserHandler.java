package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UserService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;

import static co.ke.proaktivio.qwanguapi.utils.CustomErrorUtil.handleExceptions;
import static co.ke.proaktivio.qwanguapi.utils.CustomUserHandlerValidatorUtil.*;

@Component
@RequiredArgsConstructor
public class UserHandler {
    private final UserService userService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(UserDto.class)
                .map(validateUserDtoFunc(new UserDtoValidator()))
                .flatMap(userService::createAndNotify)
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/users/%s".formatted(created.getId())))
                        .body(Mono.just(new SuccessResponse<>(true, "User created successfully.", created)), SuccessResponse.class)
                        .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> activate(ServerRequest request) {
        String id = request.pathVariable("id");
        Optional<String> tokenOpt = request.queryParam("token");
        return Mono.just(tokenOpt)
                .filter(t -> t.isPresent() && !t.get().trim().isEmpty() && !t.get().trim().isBlank())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token is required!")))
                .map(Optional::get)
                .flatMap(token -> userService.activate(token, id))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "User updated successfully.", updated)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());

    }

    public Mono<ServerResponse> changePassword(ServerRequest request) {
        String id = request.pathVariable("id");
        return request
                .bodyToMono(PasswordDto.class)
                .map(validatePasswordDtoFunc(new PasswordDtoValidator()))
                .flatMap(dto -> userService.changePassword(id, dto))
                .flatMap(user ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "User updated successfully.", user)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> sendResetPassword(ServerRequest request) {
        return request
                .bodyToMono(EmailDto.class)
                .map(validateEmailDtoFunc(new EmailDtoValidator()))
                .flatMap(userService::sendResetPassword)
                .then(
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Email for password reset will be sent if email address exists.", null)), SuccessResponse.class)
                                .log())
                .onErrorResume(e -> {
                    if (e instanceof CustomNotFoundException) {
                        return ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Email for password reset will be sent if email address exists.", null)), SuccessResponse.class)
                                .log();
                    }
                    return Mono.error(e);
                })
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> resetPassword(ServerRequest request) {
        Optional<String> tokenOpt = request.queryParam("token");
        return Mono.just(tokenOpt)
                .filter(t -> t.isPresent() && !t.get().trim().isEmpty() && !t.get().trim().isBlank())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token is required!")))
                .map(Optional::get)
                .flatMap(token -> request
                        .bodyToMono(ResetPasswordDto.class)
                        .map(validateResetPasswordDtoFunc(new ResetPasswordDtoValidator()))
                        .flatMap(dto -> userService.resetPassword(token, dto.getPassword())))
                .flatMap(user ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "User password updated successfully.", user)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        return request
                .bodyToMono(UserDto.class)
                .map(validateUserDtoFunc(new UserDtoValidator()))
                .flatMap(dto -> userService.update(id, dto))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "User updated successfully.", updated)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("id");
        Optional<String> emailAddress = request.queryParam("emailAddress");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        return userService.findPaginated(
                        id,
                        emailAddress,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Users found successfully.", results)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return userService
                .deleteById(id)
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "User with id %s deleted successfully."
                                        .formatted(id), null)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> signIn(ServerRequest request) {
        return request
                .bodyToMono(SignInDto.class)
                .map(validateSignInDtoFunc(new SignInDtoValidator()))
                .flatMap(userService::signIn)
                .flatMap(tokenDto ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Signed in successfully.", tokenDto)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

}
