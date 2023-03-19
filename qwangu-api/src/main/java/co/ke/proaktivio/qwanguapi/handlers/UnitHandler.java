package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.models.Unit.Identifier;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UnitService;
import co.ke.proaktivio.qwanguapi.validators.UnitDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
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
public class UnitHandler {
    private final UnitService unitService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(UnitDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(ValidationUtil.validateUnitDto(new UnitDtoValidator()))
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
                .map(ValidationUtil.validateUnitDto(new UnitDtoValidator()))
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

    private Integer convertToInteger(Optional<String> paramOpt) {
        if (paramOpt.isPresent()) {
            String value = paramOpt.get();
            if (NumberUtils.isParsable(value))
                return NumberUtils.createInteger(value);
        }
        return null;
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("unitId");
        return unitService.findById(id)
        		.switchIfEmpty(Mono.error(new CustomNotFoundException("Unit with id %s does not exist!".formatted(id))))
                .flatMap(results -> {
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

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Optional<String> propertyIdOptional = request.queryParam("propertyId");
        Optional<String> statusOptional = request.queryParam("status");
        Optional<String> accountNoOPtional = request.queryParam("accountNo");
        Optional<String> typeOPtional = request.queryParam("type");
        Optional<String> identifierOptional = request.queryParam("identifier");
        Optional<String> floorNoOptional = request.queryParam("floorNo");
        Optional<String> bedroomsOptional = request.queryParam("noOfBedrooms");
        Optional<String> bathroomsOptional = request.queryParam("noOfBathrooms");
        Optional<String> orderOptional = request.queryParam("order");

        if (typeOPtional.isPresent() &&  !EnumUtils.isValidEnum(Unit.UnitType.class, typeOPtional.get())) {
            String[] arrayOfState = Stream.of(Unit.UnitType.values()).map(Unit.UnitType::getType).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Unit type should be " + states + "!");
        }
        
        ValidationUtil.vaidateOrderType(orderOptional);
        log.debug(" Validation of request param Unit.Type was successful");

        if (statusOptional.isPresent() &&  !EnumUtils.isValidEnum(Unit.Status.class, statusOptional.get())) {
            String[] arrayOfState = Stream.of(Unit.Status.values()).map(Unit.Status::getState).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Status should be " + states + "!");
        }
        log.debug(" Validation of request param Unit.Status was successful");

            Integer floorNoResult = convertToInteger(floorNoOptional);
            Integer noOfBedrooms = convertToInteger(bedroomsOptional);
            Integer noOfBathrooms = convertToInteger(bathroomsOptional);

            return unitService.findAll(
                            propertyIdOptional.map(propertyId -> propertyId).orElse(null),
                            statusOptional.map(Unit.Status::valueOf).orElse(null),
                            accountNoOPtional.map(accountNo -> accountNo).orElse(null),
                            typeOPtional.map(Unit.UnitType::valueOf).orElse(null),
                            identifierOptional.map(Identifier::valueOf).orElse(null),
                            floorNoResult,
                            noOfBedrooms,
                            noOfBathrooms,
                            orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC)
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
}
