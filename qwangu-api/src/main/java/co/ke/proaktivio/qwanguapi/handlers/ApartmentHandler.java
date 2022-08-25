package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.ApartmentService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.ApartmentDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class ApartmentHandler {
    private final ApartmentService apartmentService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(ApartmentDto.class)
                .map(validateApartmentDtoFunc(new ApartmentDtoValidator()))
                .flatMap(apartmentService::create)
                .doOnSuccess(a -> log.info(" Created {}", a))
                .doOnError(e -> log.error(" Failed to create apartment. Error ", e))
                .flatMap(created ->
                        ServerResponse
                                .created(URI.create("v1/apartments/%s".formatted(created.getId())))
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.CREATED.value(), true, "Apartment created successfully.",
                                        created)), Response.class));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("apartmentId");
        return request.bodyToMono(ApartmentDto.class)
                .map(validateApartmentDtoFunc(new ApartmentDtoValidator()))
                .flatMap(dto -> apartmentService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated {}", a))
                .doOnError(e -> log.error(" Failed to update apartment. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(), true,"Apartment updated successfully.",
                                        updated)), Response.class));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("apartmentId");
        Optional<String> name = request.queryParam("name");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        return apartmentService.findPaginated(
                        id,
                        name,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .doOnSuccess(a -> log.info(" Found {} apartments", a.size()))
                .doOnError(e -> log.error(" Failed to find apartment. Error ", e))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(),true,"Apartments found successfully.",
                                        results)), Response.class));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("apartmentId");
        return apartmentService.deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted apartment"))
                .doOnError(e -> log.error(" Failed to delete apartment. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(),
                                        true, "Apartment with id %s deleted successfully.".formatted(id),
                                        null)), Response.class));
    }

    private Function<ApartmentDto, ApartmentDto> validateApartmentDtoFunc(Validator validator) {
        return apartmentDto -> {
            Errors errors = new BeanPropertyBindingResult(apartmentDto, ApartmentDto.class.getName());
            validator.validate(apartmentDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return apartmentDto;
        };
    }
}
