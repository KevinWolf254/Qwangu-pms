package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.services.UserAuthorityService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class UserAuthorityHandler {
    private final UserAuthorityService userAuthorityService;

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> name = request.queryParam("name");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        log.info(" Received request for querying user authorities");
        return userAuthorityService.findPaginated(
                        name,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} user authorities", a.size()))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Authorities found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying authorities", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("authorityId");
        return userAuthorityService.findById(id)
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Authority found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying authority by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("apartmentId");
        return request
                .bodyToMono(UserAuthorityDto.class)
                .doOnSuccess(a -> log.info(" Received request to update {}", a))
//                .map(validateUserRoleDtoFunc(new UserRoleDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update authority was successful"))
                .flatMap(dto -> userAuthorityService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated authority {} successfully", a.getName()))
                .doOnError(e -> log.error(" Failed to update authority. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(), true,"Authority updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating authority", a.rawStatusCode()));
    }
}
