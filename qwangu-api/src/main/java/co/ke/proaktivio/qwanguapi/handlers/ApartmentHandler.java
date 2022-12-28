package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Log4j2
@Component
@RequiredArgsConstructor
public class ApartmentHandler {
    private final ApartmentService apartmentService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(ApartmentDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(validateApartmentDtoFunc(new ApartmentDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create apartment was successful"))
                .flatMap(apartmentService::create)
                .doOnSuccess(a -> log.info(" Created apartment {} successfully", a.getName()))
                .doOnError(e -> log.error(" Failed to create apartment. Error ", e))
                .flatMap(created ->
                        ServerResponse
                                .created(URI.create("v1/apartments/%s".formatted(created.getId())))
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.CREATED.value(), true, "Apartment created successfully.",
                                        created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating apartment", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("apartmentId");
        return request
                .bodyToMono(ApartmentDto.class)
                .doOnSuccess(a -> log.info(" Received request to update {}", a))
                .map(validateApartmentDtoFunc(new ApartmentDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update apartment was successful"))
                .flatMap(dto -> apartmentService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated apartment {} successfully", a.getName()))
                .doOnError(e -> log.error(" Failed to update apartment. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(), true,"Apartment updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating apartment", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("apartmentId");
        return apartmentService.findById(id)
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Apartment found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying user by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> name = request.queryParam("name");
        Optional<String> order = request.queryParam("order");

        return ServerResponse
                .ok()
                .body(apartmentService
                        .find(name, order.map(OrderType::valueOf).orElse(OrderType.DESC))
                        .collectList()
                        .flatMap(apartments -> Mono.just(new Response<List<Apartment>>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.OK.value(),true,"Apartments found successfully.",
                                apartments))), Response.class);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("apartmentId");
        log.info(" Received request to delete apartment with id {}", id);
        return apartmentService.deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted apartment successfully"))
                .doOnError(e -> log.error(" Failed to delete apartment. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(),
                                        true, "Apartment with id %s deleted successfully.".formatted(id),
                                        null)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting apartment", a.rawStatusCode()));
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
