package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.OccupationDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
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
public class OccupationHandler {
    private final OccupationService occupationService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(OccupationDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(validateOccupationDtoFunc(new OccupationDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create user was successful"))
                .flatMap(occupationService::create)
                .doOnSuccess(a -> log.info(" Created occupation for tenant {} and unit {} successfully",
                        a.getTenantId(), a.getUnitId()))
                .doOnError(e -> log.error(" Failed to create occupation. Error ", e))
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/occupations/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "Occupation created successfully.",
                                created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating occupation", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("occupationId");
        return request
                .bodyToMono(OccupationDto.class)
                .doOnSuccess(a -> log.info(" Received request to update {}", a))
                .map(validateOccupationDtoFunc(new OccupationDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update occupation was successful"))
                .flatMap(dto -> occupationService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated occupation for tenant {} and unit {} successfully",
                        a.getTenantId(), a.getUnitId()))
                .doOnError(e -> log.error(" Failed to update occupation. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Occupation updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating tenant", a.rawStatusCode()));
    }

    // TODO - ADD FIND_BY_ID HANDLER

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> status = request.queryParam("status");
        Optional<String> unitId = request.queryParam("unitId");
        Optional<String> tenantId = request.queryParam("tenantId");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
            if (status.isPresent() &&  !EnumUtils.isValidEnum(Occupation.Status.class, status.get())) {
                String[] arrayOfState = Stream.of(Occupation.Status.values()).map(Occupation.Status::getState).toArray(String[]::new);
                String states = String.join(" or ", arrayOfState);
                throw new CustomBadRequestException("Status should be " + states + "!");
            }
        log.info(" Received request for querying occupations");
            return occupationService.findPaginated(
                        status.map(Occupation.Status::valueOf),
                        unitId,
                        tenantId,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                    .doOnSuccess(a -> log.info(" Query request returned {} occupation", a.size()))
                    .doOnError(e -> log.error(" Failed to find occupations. Error ", e))
                    .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Occupations found successfully.",
                                        results)), Response.class))
                    .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying occupations",
                            a.rawStatusCode()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("occupationId");
        log.info(" Received request to delete occupation with id {}", id);
        return occupationService
                .deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted occupation successfully"))
                .doOnError(e -> log.error(" Failed to delete occupation. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Occupation with id %s deleted successfully."
                                        .formatted(id), null)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting user", a.rawStatusCode()));
    }

    private Function<OccupationDto, OccupationDto> validateOccupationDtoFunc(Validator validator) {
        return createOccupationDto -> {
            Errors errors = new BeanPropertyBindingResult(createOccupationDto, OccupationDto.class.getName());
            validator.validate(createOccupationDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return createOccupationDto;
        };
    }
}
