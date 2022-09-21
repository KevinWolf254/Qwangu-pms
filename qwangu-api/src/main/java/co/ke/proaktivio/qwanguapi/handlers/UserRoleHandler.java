package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
import co.ke.proaktivio.qwanguapi.services.UserRoleService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.ApartmentDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRoleHandler {
    private final UserRoleService userRoleService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(UserRoleDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
//                .map(validateUserRoleDtoFunc(new UserRoleDtoValidator()))
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
                                        HttpStatus.CREATED.value(), true, "Role created successfully.",
                                        created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating role", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("roleId");
        return userRoleService.findById(id)
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Role found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying role by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> name = request.queryParam("name");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        return userRoleService.findPaginated(
                        name,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Roles found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying roles", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("apartmentId");
        return request
                .bodyToMono(UserRoleDto.class)
                .doOnSuccess(a -> log.info(" Received request to update {}", a))
//                .map(validateUserRoleDtoFunc(new UserRoleDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update role was successful"))
                .flatMap(dto -> userRoleService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated role {} successfully", a.getName()))
                .doOnError(e -> log.error(" Failed to update role. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(), true,"Role updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating role", a.rawStatusCode()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("roleId");
        log.info(" Received request to delete role with id {}", id);
        return userRoleService.deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted role successfully"))
                .doOnError(e -> log.error(" Failed to delete role. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(),
                                        true, "Role with id %s deleted successfully.".formatted(id),
                                        null)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting role", a.rawStatusCode()));
    }
}
