package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
import co.ke.proaktivio.qwanguapi.services.UserRoleService;
import co.ke.proaktivio.qwanguapi.validators.UserRoleDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
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
public class UserRoleHandler {
    private final UserRoleService userRoleService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(UserRoleDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(ValidationUtil.validateUserRoleDto(new UserRoleDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create user role was successful"))
                .flatMap(userRoleService::create)
                .doOnSuccess(a -> log.info(" Created role {} successfully", a.getName()))
                .doOnError(e -> log.error(" Failed to create role. Error ", e))
                .flatMap(created ->
                        ServerResponse
                                .created(URI.create("v1/roles/%s".formatted(created.getId())))
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.CREATED.value(), true, "UserRole created successfully.",
                                        created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating role", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String roleId = request.pathVariable("roleId");
        return userRoleService.findById(roleId)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("UserRole with id %s was not found!"
                        .formatted(roleId))))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"UserRole found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying role by id", a.rawStatusCode()));
    }

	public Mono<ServerResponse> findAll(ServerRequest request) {
		Optional<String> nameOptional = request.queryParam("name");
		Optional<String> orderOptional = request.queryParam("order");
		
		ValidationUtil.vaidateOrderType(orderOptional);
		return userRoleService
				.findAll(nameOptional.orElse(null), orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC))
				.collectList()
				.flatMap(results -> {
					var isEmpty = results.isEmpty();
					return ServerResponse.ok()
							.body(Mono.just(new Response<>(LocalDateTime.now().toString(), request.uri().getPath(),
									HttpStatus.OK.value(), !isEmpty,
									!isEmpty ? "UserRoles found successfully." : "UserRoles were not found!", results)),
									Response.class);
				})
				.doOnSuccess(a -> log.info(" Sent response with status code {} for querying roles", a.rawStatusCode()));
	}
}
