package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UserService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                .map(tOpt -> tOpt.orElse(null))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token is required!")))
                .flatMap(token -> userService.activate(tokenOpt, Optional.of(id)))
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
                .flatMap(none ->
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

    public Mono<ServerResponse> findToken(ServerRequest request) {
        String id = request.pathVariable("id");
        Optional<String> tokenOpt = request.queryParam("token");
        return Mono.just(tokenOpt)
                .map(tOpt -> tOpt.orElse(null))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token is required!")))
                .flatMap(token -> userService.findToken(Optional.of(token), Optional.of(id)))
                .flatMap(token ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Token was found successfully.", token)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> resetPassword(ServerRequest request) {
        String id = request.pathVariable("id");
        Optional<String> tokenOpt = request.queryParam("token");
        return Mono.just(tokenOpt)
                .map(tOpt -> tOpt.orElse(null))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token is required!")))
                .flatMap(token -> request.bodyToMono(ResetPasswordDto.class)
                        .map(validateResetPasswordDtoFunc(new ResetPasswordDtoValidator()))
                        .flatMap(dto -> userService.resetPassword(id, token, dto.getPassword())))
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

    private Function<UserDto, UserDto> validateUserDtoFunc(Validator validator) {
        return userDto -> {
            Errors errors = new BeanPropertyBindingResult(userDto, UserDto.class.getName());
            validator.validate(userDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return userDto;
        };
    }

    private Function<PasswordDto, PasswordDto> validatePasswordDtoFunc(Validator validator) {
        return passwordDto -> {
            Errors errors = new BeanPropertyBindingResult(passwordDto, PasswordDto.class.getName());
            validator.validate(passwordDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return passwordDto;
        };
    }

    private Function<SignInDto, SignInDto> validateSignInDtoFunc(Validator validator) {
        return signInDto -> {
            Errors errors = new BeanPropertyBindingResult(signInDto, SignInDto.class.getName());
            validator.validate(signInDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return signInDto;
        };
    }

    private Function<EmailDto, EmailDto> validateEmailDtoFunc(Validator validator) {
        return emailDto -> {
            Errors errors = new BeanPropertyBindingResult(emailDto, EmailDto.class.getName());
            validator.validate(emailDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return emailDto;
        };
    }

    private Function<ResetPasswordDto, ResetPasswordDto> validateResetPasswordDtoFunc(Validator validator) {
        return passwordDto -> {
            Errors errors = new BeanPropertyBindingResult(passwordDto, ResetPasswordDto.class.getName());
            validator.validate(passwordDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return passwordDto;
        };
    }

    private Function<Throwable, Mono<? extends ServerResponse>> handleExceptions() {
        return e -> {
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
        };
    }
}
