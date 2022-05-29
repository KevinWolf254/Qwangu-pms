package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.ApartmentService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.lang.reflect.Array;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ApartmentHandler {
    private final ApartmentService apartmentService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(ApartmentDto.class)
                .flatMap(apartmentService::create)
                .flatMap(created ->
                        ServerResponse.created(URI.create("v1/apartments/%s".formatted(created.getId())))
                                .body(Mono.just(new SuccessResponse<>(true,"Apartment created successfully.",created)), SuccessResponse.class)
                                .log())
                .onErrorResume(e -> {
                    if (e instanceof CustomAlreadyExistsException) {
                        return ServerResponse.badRequest()
                                .body(Mono.just(
                                        new ErrorResponse<>(false, ErrorCode.BAD_REQUEST_ERROR, "Bad request", List.of(e.getMessage()))), ErrorResponse.class)
                                .log();
                    }
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Mono.just(
                                    new ErrorResponse<>(false, ErrorCode.INTERNAL_SERVER_ERROR, "Something happened!",List.of("Something happened!"))), ErrorResponse.class)
                            .log();
                });
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(ApartmentDto.class)
                .flatMap(dto -> apartmentService.update(id, dto))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true,"Apartment updated successfully.",updated)), SuccessResponse.class)
                                .log())
                .onErrorResume(e -> {
                    if (e instanceof CustomAlreadyExistsException) {
                        return ServerResponse.badRequest()
                                .body(Mono.just(
                                        new ErrorResponse<>(false, ErrorCode.BAD_REQUEST_ERROR, "Bad request", List.of(e.getMessage()))), ErrorResponse.class)
                                .log();
                    }
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Mono.just(
                                    new ErrorResponse<>(false, ErrorCode.INTERNAL_SERVER_ERROR, "Something happened!",List.of("Something happened!"))), ErrorResponse.class)
                            .log();
                });
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("id");
        Optional<String> name = request.queryParam("name");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        return apartmentService.findPaginated(
                        id,
                        name,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page") - 1).orElse(0),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true,"Apartments found successfully.",results)), SuccessResponse.class)
                                .log())
                .onErrorResume(e -> {
                    if (e instanceof CustomNotFoundException) {
                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                                .body(Mono.just(
                                        new ErrorResponse<>(false, ErrorCode.NOT_FOUND_ERROR, "Not found!", List.of(e.getMessage()))), ErrorResponse.class)
                                .log();
                    }
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Mono.just(
                                    new ErrorResponse<>(false, ErrorCode.INTERNAL_SERVER_ERROR, "Something happened!",List.of("Something happened!"))), ErrorResponse.class)
                            .log();
                });
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return apartmentService.deleteById(id)
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Apartment with id %s deleted successfully.".formatted(id), null)), SuccessResponse.class)
                                .log())
                .onErrorResume(e -> {
                    if (e instanceof CustomNotFoundException) {
                        return ServerResponse.status(HttpStatus.NOT_FOUND)
                                .body(Mono.just(
                                        new ErrorResponse<>(false, ErrorCode.NOT_FOUND_ERROR, "Not found!", List.of(e.getMessage()))), ErrorResponse.class)
                                .log();
                    }
                    return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(Mono.just(
                                    new ErrorResponse<>(false, ErrorCode.INTERNAL_SERVER_ERROR, "Something happened!",List.of("Something happened!"))), ErrorResponse.class)
                            .log();
                });
    }
}
