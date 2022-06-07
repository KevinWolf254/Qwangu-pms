package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import co.ke.proaktivio.qwanguapi.services.UserService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.UserDtoValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.Example;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
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
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(UserDto.class)
                .map(validateUserDtoFunc(new UserDtoValidator()))
                .flatMap(userService::create)
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/users/%s".formatted(created.getId())))
                        .body(Mono.just(new SuccessResponse<>(true, "User created successfully.", created)), SuccessResponse.class)
                        .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UserDto.class)
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
        return userService.deleteById(id)
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "User with id %s deleted successfully.".formatted(id), null)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> signIn(ServerRequest request) {
        return request.bodyToMono(SignUpDto.class)
//                .map(validateUserDtoFunc(new UserDtoValidator()))
                .flatMap(dto -> userRepository
                        .findOne(Example.of(new User(dto.getUsername())))
                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                        .flatMap(user -> {
                            boolean passwordsMatch = encoder.encode(dto.getPassword()).equals(user.getPassword());
                            if(passwordsMatch) {
                                return Mono.just(new TokenDto(jwtUtil.generateToken(user, new Role())));
                            }
                            return Mono.error(new UsernameNotFoundException("Invalid username or password!"));
                        }))
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
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Mono.just(
                            new ErrorResponse<>(false, ErrorCode.INTERNAL_SERVER_ERROR, "Something happened!", "Something happened!")), ErrorResponse.class)
                    .log();
        };
    }
}
