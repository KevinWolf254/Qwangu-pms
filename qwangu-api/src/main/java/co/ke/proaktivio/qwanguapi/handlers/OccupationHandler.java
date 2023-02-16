package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.validators.*;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Log4j2
@Component
@RequiredArgsConstructor
public class OccupationHandler {
    private final OccupationService occupationService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(OccupationForNewTenantDto.class)
                .doOnSuccess(dto -> log.debug(" Received request to create {}", dto))
                .map(ValidatorUtil.validateOccupationForNewTenantDto(new OccupationForNewTenantDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create user was successful"))
                .flatMap(occupationService::create)
                .doOnSuccess(a -> log.info(" Created occupation for tenant {} and unit {} successfully",
                        a.getTenantId(), a.getUnitId()))
                .doOnError(e -> log.error(" Failed to create occupation. Error {}", e.getMessage()))
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/occupations/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "Occupation created successfully.",
                                created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating occupation", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("occupationId");
        return occupationService.findById(id)
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Occupation found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying occupation by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> status = request.queryParam("status");
        Optional<String> number = request.queryParam("number");
        Optional<String> unitId = request.queryParam("unitId");
        Optional<String> tenantId = request.queryParam("tenantId");
        Optional<String> order = request.queryParam("order");
        if (status.isPresent() && StringUtils.hasText(status.get()) && !EnumUtils.isValidEnum(Occupation.Status.class, status.get())) {
            String[] arrayOfState = Stream.of(Occupation.Status.values()).map(Occupation.Status::getState).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Status should be " + states + "!");
        }
        return ServerResponse
                .ok()
                .body(occupationService.findAll(
                                StringUtils.hasText(status.get()) ? status.map(Occupation.Status::valueOf): Optional.empty(),
                                number,
                                unitId,
                                tenantId,
                                order.map(OrderType::valueOf).orElse(OrderType.DESC)
                        ).collectList()
                        .flatMap(occupations -> {
                            var success = !occupations.isEmpty();
                            var message = occupations.isEmpty() ? "Occupations with those parameters do not exist!" : "Occupations found successfully.";
                            return Mono.just(new Response<>(
                                    LocalDateTime.now().toString(),
                                    request.uri().getPath(),
                                    HttpStatus.OK.value(), success, message,
                                    occupations));
                        }), Response.class);
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
}
