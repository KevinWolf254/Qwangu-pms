package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.PropertyService;
import co.ke.proaktivio.qwanguapi.validators.PropertyDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
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
import java.util.stream.Stream;

@Log4j2
@Component
@RequiredArgsConstructor
public class PropertyHandler {
    private final PropertyService propertyService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(PropertyDto.class)
                .doOnSuccess(a -> log.debug("Received request to create {}", a))
                .map(validateApartmentDtoFunc(new PropertyDtoValidator()))
                .doOnSuccess(a -> log.debug("Validation of request to create apartment was successful"))
                .flatMap(propertyService::create)
                .doOnError(e -> log.error("Failed to create apartment. Error ", e))
                .flatMap(created ->
                        ServerResponse
                                .created(URI.create("v1/properties/%s".formatted(created.getId())))
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.CREATED.value(), true, "Property created successfully.",
                                        created)), Response.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for creating apartment",
                        a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("propertyId");
        return request
                .bodyToMono(PropertyDto.class)
                .doOnSuccess(a -> log.debug("Received request to update {}", a))
                .map(validateApartmentDtoFunc(new PropertyDtoValidator()))
                .doOnSuccess(a -> log.debug("Validation of request to update apartment was successful"))
                .flatMap(dto -> propertyService.update(id, dto))
                .doOnSuccess(a -> log.info("Updated successfully: {}", a))
                .doOnError(e -> log.error("Failed to update apartment. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(), true,"Property updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating apartment",
                        a.rawStatusCode()));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("propertyId");
        return propertyService.findById(id)
                .flatMap(results -> {
                    if(results == null)
                        return ServerResponse
                                        .ok()
                                        .body(Mono.just(new Response<>(
                                                LocalDateTime.now().toString(),
                                                request.uri().getPath(),
                                                HttpStatus.OK.value(),true,"Property with id %s does not exist!"
                                                .formatted(id),
                                                null)), Response.class);
                    return ServerResponse
                                    .ok()
                                    .body(Mono.just(new Response<>(
                                            LocalDateTime.now().toString(),
                                            request.uri().getPath(),
                                            HttpStatus.OK.value(),true,"Property found successfully.",
                                            results)), Response.class);
                })
                .doOnSuccess(a -> log.debug("Sent response with status code {} for querying user by id",
                        a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> type = request.queryParam("type");
        Optional<String> name = request.queryParam("name");
        Optional<String> order = request.queryParam("order");

        if (type.isPresent() && !EnumUtils.isValidEnum(Property.PropertyType.class, type.get())) {
            String[] arrayOfState = Stream.of(Property.PropertyType.values()).map(Property.PropertyType::getName)
                    .toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Property type should be " + states + "!");
        }
        log.debug(" Validation of request param Unit.Type was successful");

        return ServerResponse
                .ok()
                .body(propertyService
                        .find(
                                name.orElse(null),
                                type.map(Property.PropertyType::valueOf).orElse(null),
                                order.map(OrderType::valueOf).orElse(OrderType.DESC)
                        )
                        .collectList()
                        .flatMap(apartments -> {
                            if(apartments.isEmpty())
                                return Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Properties with those parameters do not exist!",
                                        apartments));
                            return Mono.just(new Response<>(
                                    LocalDateTime.now().toString(),
                                    request.uri().getPath(),
                                    HttpStatus.OK.value(),true,"Properties found successfully.",
                                    apartments));
                        }), Response.class);
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("propertyId");
        log.debug("Received request to delete apartment with id {}", id);
        return propertyService.deleteById(id)
                .doOnSuccess($ -> log.debug("Deleted apartment with id {} successfully", id))
                .doOnError(e -> log.error("Failed to delete apartment. Error ", e))
                .flatMap(success -> {
                    if(!success)
                        return ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        "",
                                        HttpStatus.OK.value(),
                                        false, "Property with id %s does not exist!".formatted(id),
                                        null)), Response.class);
                    return ServerResponse
                            .ok()
                            .body(Mono.just(new Response<>(
                                    LocalDateTime.now().toString(),
                                    "",
                                    HttpStatus.OK.value(),
                                    true, "Property with id %s deleted successfully.".formatted(id),
                                    null)), Response.class);
                })
                .doOnSuccess(a -> log.debug("Sent response with status code {} for deleting apartment", a.rawStatusCode()));
    }

    private Function<PropertyDto, PropertyDto> validateApartmentDtoFunc(Validator validator) {
        return apartmentDto -> {
            Errors errors = new BeanPropertyBindingResult(apartmentDto, PropertyDto.class.getName());
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
