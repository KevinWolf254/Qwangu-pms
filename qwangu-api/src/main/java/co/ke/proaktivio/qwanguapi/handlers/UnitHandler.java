package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UnitService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.UnitDtoValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static co.ke.proaktivio.qwanguapi.utils.CustomErrorUtil.handleExceptions;

@Component
@RequiredArgsConstructor
public class UnitHandler {
    private final UnitService unitService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(UnitDto.class)
                .map(validateUnitDtoFunc(new UnitDtoValidator()))
                .flatMap(unitService::create)
                .flatMap(created ->
                        ServerResponse.created(URI.create("v1/units/%s".formatted(created.getId())))
                                .body(Mono.just(new SuccessResponse<>(true, "Unit created successfully.",
                                        created)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UnitDto.class)
                .map(validateUnitDtoFunc(new UnitDtoValidator()))
                .flatMap(dto -> unitService.update(id, dto))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Unit updated successfully.",
                                        updated)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
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

//    private Optional<Unit.Type> convertToType(String param) {
//        if(EnumUtils.isValidEnumIgnoreCase(Unit.Type.class, param)){
//            return Optional.of(EnumUtils.getEnum(Unit.Type.class, param));
//        }
//        throw new CustomBadRequestException("Not a valid Type!");
//    }
    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("id");
        Optional<String> status = request.queryParam("status");
        Optional<String> accountNo = request.queryParam("accountNo");
        Optional<String> type = request.queryParam("type");
        Optional<String> identifier = request.queryParam("identifier");
        Optional<String> floorNo = request.queryParam("floorNo");
        Optional<String> bedrooms = request.queryParam("noOfBedrooms");
        Optional<String> bathrooms = request.queryParam("noOfBathrooms");
        Optional<String> apartmentId = request.queryParam("apartmentId");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        try {
            if (type.isPresent() &&  !EnumUtils.isValidEnum(Unit.Type.class, type.get())) {
                String[] arrayOfState = Stream.of(Unit.Type.values()).map(Unit.Type::getType).toArray(String[]::new);
                String states = String.join(" or ", arrayOfState);
                throw new CustomBadRequestException("Type should be " + states + "!");
            }

            if (status.isPresent() &&  !EnumUtils.isValidEnum(Unit.Status.class, status.get())) {
                String[] arrayOfState = Stream.of(Unit.Status.values()).map(Unit.Status::getState).toArray(String[]::new);
                String states = String.join(" or ", arrayOfState);
                throw new CustomBadRequestException("Status should be " + states + "!");
            }

            Optional<Integer> floorNoResult = convertToInteger(floorNo);
            Optional<Integer> noOfBedrooms = convertToInteger(bedrooms);
            Optional<Integer> noOfBathrooms = convertToInteger(bathrooms);
            Optional<Unit.Identifier> finalIdentifier = identifier.flatMap(this::convertToIdentifier);
            Integer finalPage = page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1);
            Integer finalPageSize = pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10);
            OrderType finalOrder = order.map(OrderType::valueOf).orElse(OrderType.DESC);

            return unitService.findPaginated(
                            id,
                            status.map(Unit.Status::valueOf),
                            accountNo,
                            type.map(Unit.Type::valueOf),
                            finalIdentifier,
                            floorNoResult,
                            noOfBedrooms,
                            noOfBathrooms,
                            apartmentId,
                            finalPage,
                            finalPageSize,
                            finalOrder
                    ).collectList()
                    .flatMap(results ->
                            ServerResponse
                                    .ok()
                                    .body(Mono.just(new SuccessResponse<>(true, "Units found successfully.", results)), SuccessResponse.class)
                                    .log())
                    .onErrorResume(handleExceptions());
        } catch (Exception e) {
            return handleExceptions(e);
        }
    }

    public Mono<ServerResponse> findUnitsWithNotice(ServerRequest request) {
        return request.bodyToMono(UnitsWithNoticeDto.class)
                .filter(dto -> dto.getOccupationIds() != null && !dto.getOccupationIds().isEmpty())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Occupation ids must not be null!")))
                .flatMap(dto -> unitService.findUnitsByOccupationIds(dto.getOccupationIds()).collectList())
                .flatMap(units ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Units found successfully.",
                                        units)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return unitService.deleteById(id)
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Unit with id %s deleted successfully.".formatted(id), null)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    private Function<UnitDto, UnitDto> validateUnitDtoFunc(Validator validator) {
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
