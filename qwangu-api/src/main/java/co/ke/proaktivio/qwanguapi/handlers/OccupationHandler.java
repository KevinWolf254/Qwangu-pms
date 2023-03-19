package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
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
                .doOnSuccess(dto -> log.info("Request: {}", dto))
                .map(ValidationUtil.validateOccupationForNewTenantDto(new OccupationForNewTenantDtoValidator()))
                .doOnSuccess(a -> log.debug("Validation of request to create user was successful"))
                .flatMap(occupationService::create)
                .doOnError(e -> log.error("Failed to create occupation. Error {}", e.getMessage()))
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/occupations/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "Occupation created successfully.",
                                created)), Response.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for creating occupation", a.rawStatusCode()));
    }

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("occupationId");
        return occupationService.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(id))))
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

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Optional<String> statusOptional = request.queryParam("status");
        Optional<String> occupationNoOptional = request.queryParam("occupationNo");
        Optional<String> unitIdOptional = request.queryParam("unitId");
        Optional<String> tenantIdOptional = request.queryParam("tenantId");
        Optional<String> orderOptional = request.queryParam("order");
        if (statusOptional.isPresent() && StringUtils.hasText(statusOptional.get()) && !EnumUtils.isValidEnum(Occupation.Status.class, statusOptional.get())) {
            String[] arrayOfState = Stream.of(Occupation.Status.values()).map(Occupation.Status::getState).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Status should be " + states + "!");
        }
        
        ValidationUtil.vaidateOrderType(orderOptional);
        return ServerResponse
                .ok()
                .body(occupationService.findAll(
                		statusOptional.map(Occupation.Status::valueOf).orElse(null),
                		occupationNoOptional.map(occupationNo -> occupationNo).orElse(null),
                        unitIdOptional.map(unitId -> unitId).orElse(null),
                        tenantIdOptional.map(tenantId -> tenantId).orElse(null),
                        orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC)
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
}
