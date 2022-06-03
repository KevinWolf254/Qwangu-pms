package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.ErrorCode;
import co.ke.proaktivio.qwanguapi.pojos.ErrorResponse;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.SuccessResponse;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import co.ke.proaktivio.qwanguapi.services.RoleService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class RoleHandler {
    private final RoleService roleService;


    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("id");
        Optional<String> name = request.queryParam("name");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        return roleService.findPaginated(
                        id,
                        name,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(0),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true,"Roles found successfully.",results)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    private Function<Throwable, Mono<? extends ServerResponse>> handleExceptions() {
        return e -> {
            if (e instanceof CustomNotFoundException) {
                return ServerResponse.status(HttpStatus.NOT_FOUND)
                        .body(Mono.just(
                                new ErrorResponse<>(false, ErrorCode.NOT_FOUND_ERROR, "Not found!", List.of(e.getMessage()))), ErrorResponse.class)
                        .log();
            }
            return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Mono.just(
                            new ErrorResponse<>(false, ErrorCode.INTERNAL_SERVER_ERROR, "Something happened!", List.of("Something happened!"))), ErrorResponse.class)
                    .log();
        };
    }
}
