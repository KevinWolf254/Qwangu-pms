package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UnitService;
import co.ke.proaktivio.qwanguapi.validators.UnitDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j2
@Component
@RequiredArgsConstructor
public class UnitHandler {
    private final UnitService unitService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(UnitDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(validateUnitDtoFunc(new UnitDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create unit was successful"))
                .flatMap(unitService::create)
                .doOnSuccess(a -> log.info(" Created unit {} successfully", a.getNumber()))
                .doOnError(e -> log.error(" Failed to create unit. Error ", e))
                .flatMap(created ->
                        ServerResponse.created(URI.create("v1/units/%s".formatted(created.getId())))
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.CREATED.value(),true, "Unit created successfully.",
                                        created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating unit", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("unitId");
        return request.bodyToMono(UnitDto.class)
                .doOnSuccess(a -> log.info(" Received request to update {}", a))
                .map(validateUnitDtoFunc(new UnitDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update unit was successful"))
                .flatMap(dto -> unitService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated unit {} successfully", a.getNumber()))
                .doOnError(e -> log.error(" Failed to update unit. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Unit updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating unit", a.rawStatusCode()));
    }

    private Optional<Integer> convertToInteger(Optional<String> paramOpt) {
        Optional<Integer> result = Optional.empty();
        if (paramOpt.isPresent()) {
            String value = paramOpt.get();
            if (NumberUtils.isParsable(value))
                result = Optional.of(NumberUtils.createInteger(value));
        }
        return result;
    }

    private Optional<Unit.Identifier> convertToIdentifier(String param) {
        if(EnumUtils.isValidEnumIgnoreCase(Unit.Identifier.class, param)){
            return Optional.of(EnumUtils.getEnum(Unit.Identifier.class, param));
        }
        throw new CustomBadRequestException("Not a valid Identifier!");
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("unitId");
        return unitService.findById(id)
                .flatMap(results -> {
                    if (results == null) {
                        return ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Unit with id %s does not exist!".formatted(id),
                                        null)), Response.class);
                    }
                    return ServerResponse
                            .ok()
                            .body(Mono.just(new Response<>(
                                    LocalDateTime.now().toString(),
                                    request.uri().getPath(),
                                    HttpStatus.OK.value(), true, "Unit found successfully.",
                                    results)), Response.class);
                })
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying unit by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> apartmentId = request.queryParam("apartmentId");
        Optional<String> status = request.queryParam("status");
        Optional<String> accountNo = request.queryParam("accountNo");
        Optional<String> type = request.queryParam("type");
        Optional<String> identifier = request.queryParam("identifier");
        Optional<String> floorNo = request.queryParam("floorNo");
        Optional<String> bedrooms = request.queryParam("noOfBedrooms");
        Optional<String> bathrooms = request.queryParam("noOfBathrooms");
        Optional<String> order = request.queryParam("order");

        if (type.isPresent() &&  !EnumUtils.isValidEnum(Unit.UnitType.class, type.get())) {
            String[] arrayOfState = Stream.of(Unit.UnitType.values()).map(Unit.UnitType::getType).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Type should be " + states + "!");
        }
        log.debug(" Validation of request param Unit.Type was successful");

        if (status.isPresent() &&  !EnumUtils.isValidEnum(Unit.Status.class, status.get())) {
            String[] arrayOfState = Stream.of(Unit.Status.values()).map(Unit.Status::getState).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Status should be " + states + "!");
        }
        log.debug(" Validation of request param Unit.Status was successful");

            Optional<Integer> floorNoResult = convertToInteger(floorNo);
            Optional<Integer> noOfBedrooms = convertToInteger(bedrooms);
            Optional<Integer> noOfBathrooms = convertToInteger(bathrooms);
            Optional<Unit.Identifier> finalIdentifier = identifier.flatMap(this::convertToIdentifier);
            OrderType finalOrder = order.map(OrderType::valueOf).orElse(OrderType.DESC);

            return unitService.find(
                            apartmentId,
                            status.map(Unit.Status::valueOf),
                            accountNo,
                            type.map(Unit.UnitType::valueOf),
                            finalIdentifier,
                            floorNoResult,
                            noOfBedrooms,
                            noOfBathrooms,
                            finalOrder
                    ).collectList()
                    .doOnSuccess(a -> log.info(" Query request returned {} units", a.size()))
                    .doOnError(e -> log.error(" Failed to find units. Error ", e))
                    .flatMap(results ->{
                        if(results.isEmpty())
                            return ServerResponse
                                    .ok()
                                    .body(Mono.just(new Response<>(
                                            LocalDateTime.now().toString(),
                                            request.uri().getPath(),
                                            HttpStatus.OK.value(),true, "Units with those parameters do not exist!",
                                            results)), Response.class);
                        return ServerResponse
                                        .ok()
                                        .body(Mono.just(new Response<>(
                                                LocalDateTime.now().toString(),
                                                request.uri().getPath(),
                                                HttpStatus.OK.value(),true, "Units found successfully.",
                                                results)), Response.class);
                            });
    }

    // TODO FIND IF ITS IMPORTANT
    public Mono<ServerResponse> findByOccupationIds(ServerRequest request) {
        return request.bodyToMono(FindUnitsDto.class)
                .filter(dto -> dto.getOccupationIds() != null && !dto.getOccupationIds().isEmpty())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Occupation ids must not be null!")))
                .flatMap(dto -> unitService.findByOccupationIds(dto.getOccupationIds()).collectList())
                .flatMap(units ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Units found successfully.",
                                        units)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying units", a.rawStatusCode()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("unitId");
        log.info(" Received request to delete unit with id {}", id);
        return unitService.deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted unit successfully"))
                .doOnError(e -> log.error(" Failed to delete unit. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,
                                        "Unit with id %s deleted successfully.".formatted(id), null)),
                                        Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting unit", a.rawStatusCode()));
    }

    private Function<UnitDto, UnitDto> validateUnitDtoFunc(Validator validator) {
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
