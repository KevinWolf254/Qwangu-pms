package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UserService;
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

@Log4j2
@Component
@RequiredArgsConstructor
public class UserHandler {
    private final UserService userService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(UserDto.class)
                .doOnSuccess(a -> log.info("Request create: {}", a))
                .map(ValidationUtil.validateUserDto(new UserDtoValidator()))
                .doOnSuccess(a -> log.debug("Validation of request to create user was successful"))
                .flatMap(userService::createAndNotify)
                .doOnError(e -> log.error("Failed to create user. Error ", e))
                .map(UserWithoutPasswordDto::new)
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/users/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "User created successfully.", created)),
                                Response.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for creating user", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("userId");
        return request
                .bodyToMono(UpdateUserDto.class)
                .doOnSuccess(a -> log.info(" Request to update {}", a))
                .map(ValidationUtil.validateUpdateUserDto(new UpdateUserDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update user was successful"))
                .flatMap(dto -> userService.update(id, dto))
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
                .switchIfEmpty(Mono.error(new CustomNotFoundException("User with id %S was not found!"
                        .formatted(id))))
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

	public Mono<ServerResponse> findAll(ServerRequest request) {
		Optional<String> emailAddressOptional = request.queryParam("emailAddress");
		Optional<String> orderOptional = request.queryParam("order");

		ValidationUtil.vaidateOrderType(orderOptional);
		log.debug("Request for querying users");
		return userService
				.findAll(emailAddressOptional.orElse(null),
						orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC))
				.map(UserWithoutPasswordDto::new).collectList()
				.doOnSuccess(a -> log.info("Request returned {} users", a.size()))
				.doOnError(e -> log.error("Failed to find users. Error ", e)).flatMap(results -> {
					var isEmpty = results.isEmpty();
					var message = !isEmpty ? "Users found successfully." : "Users were not found!";
					return ServerResponse.ok().body(Mono.just(new Response<>(LocalDateTime.now().toString(),
							request.uri().getPath(), HttpStatus.OK.value(), !isEmpty, message, results)),
							Response.class);

				})
				.doOnSuccess(
						a -> log.debug(" Sent response with status code {} for querying users", a.rawStatusCode()));
	}

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("userId");
        log.info(" Request to delete user with id {}", id);
        return userService
                .deleteById(id)
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
                .doOnSuccess(a -> log.debug("Request to change password"))
                .map(ValidationUtil.validatePasswordDto(new PasswordDtoValidator()))
                .doOnSuccess(a -> log.debug("Validation of request to change password was successful"))
                .flatMap(dto -> userService.changePassword(id, dto))
                .doOnSuccess(u -> log.info("Changed password successfully for {}", u.getEmailAddress()))
                .doOnError(e -> log.error("Failed to change password. Error ", e))
                .map(UserWithoutPasswordDto::new)
                .flatMap(user ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "User password updated successfully.",
                                        user)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for changing password", a.rawStatusCode()));
    }
}
