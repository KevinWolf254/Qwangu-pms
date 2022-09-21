package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UserService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import static co.ke.proaktivio.qwanguapi.utils.CustomUserHandlerValidatorUtil.*;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserHandler {
    private final UserService userService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(UserDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(validateUserDtoFunc(new UserDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create user was successful"))
                .flatMap(userService::createAndNotify)
                .doOnSuccess(a -> log.info(" Created user {} successfully", a.getEmailAddress()))
                .doOnError(e -> log.error(" Failed to create user. Error ", e))
                .map(UserWithoutPasswordDto::new)
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/users/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "User created successfully.", created)),
                                Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating user", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("userId");
        return request
                .bodyToMono(UpdateUserDto.class)
                .doOnSuccess(a -> log.info(" Request to update {}", a))
                .map(validateUpdateUserDtoFunc(new UpdateUserDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update user was successful"))
                .flatMap(dto -> userService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated user {} successfully", a.getEmailAddress()))
                .doOnError(e -> log.error(" Failed to update user. Error ", e))
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

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("userId");
        return userService.findById(id)
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"User found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying user by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("userId");
        Optional<String> emailAddress = request.queryParam("emailAddress");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        log.info(" Request for querying users");
        return userService.findPaginated(
                        id,
                        emailAddress,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                )
                .map(UserWithoutPasswordDto::new)
                .collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} users", a.size()))
                .doOnError(e -> log.error(" Failed to find users. Error ", e))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Users found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying users", a.rawStatusCode()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("userId");
        log.info(" Request to delete user with id {}", id);
        return userService
                .deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted user successfully"))
                .doOnError(e -> log.error(" Failed to delete user. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "User with id %s deleted successfully."
                                        .formatted(id), null)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting user", a.rawStatusCode()));
    }

    public Mono<ServerResponse> changePassword(ServerRequest request) {
        String id = request.pathVariable("userId");
        return request
                .bodyToMono(PasswordDto.class)
                .doOnSuccess(a -> log.info(" Request to change password"))
                .map(validatePasswordDtoFunc(new PasswordDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to change password was successful"))
                .flatMap(dto -> userService.changePassword(id, dto))
                .doOnSuccess(u -> log.info(" Changed password successfully for {}", u.getEmailAddress()))
                .doOnError(e -> log.error(" Failed to change password. Error ", e))
                .map(UserWithoutPasswordDto::new)
                .flatMap(user ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "User updated successfully.",
                                        user)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for changing password", a.rawStatusCode()));
    }
}
