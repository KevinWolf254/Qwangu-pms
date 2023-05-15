package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.PropertyService;
import co.ke.proaktivio.qwanguapi.validators.PropertyDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
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
                .map(ValidationUtil.validatePropertyDto(new PropertyDtoValidator()))
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
                .map(ValidationUtil.validatePropertyDto(new PropertyDtoValidator()))
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
        		.switchIfEmpty(Mono.error(new CustomNotFoundException("Property with id %s does not exist!"
                        .formatted(id))))
                .flatMap(results -> {
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

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Optional<String> typeOptional = request.queryParam("type");
        Optional<String> nameOptional = request.queryParam("name");
        Optional<String> orderOptional = request.queryParam("order");

        if (typeOptional.isPresent() && StringUtils.hasText(typeOptional.get()) && !EnumUtils.isValidEnum(Property.PropertyType.class, typeOptional.get())) {
            String[] arrayOfState = Stream.of(Property.PropertyType.values()).map(Property.PropertyType::getName)
                    .toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Property type should be " + states + "!");
        }
        ValidationUtil.vaidateOrderType(orderOptional);
        
        return ServerResponse
                .ok()
                .body(propertyService
                        .find(
                                nameOptional.orElse(null),
                                typeOptional.isPresent() && StringUtils.hasText(typeOptional.get()) ? 
                                		typeOptional.map(Property.PropertyType::valueOf).orElse(null) : 
                                			null,
                                orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC)
                        )
                        .collectList()
                        .flatMap(apartments -> {
                        	var isEmpty = apartments.isEmpty();
                        	var message = isEmpty ? "Properties with those parameters do not exist!" : "Properties found successfully.";
                            return Mono.just(new Response<>(
                                    LocalDateTime.now().toString(),
                                    request.uri().getPath(),
                                    HttpStatus.OK.value(),!isEmpty, message,
                                    apartments));
                        }), Response.class);
    }
}
