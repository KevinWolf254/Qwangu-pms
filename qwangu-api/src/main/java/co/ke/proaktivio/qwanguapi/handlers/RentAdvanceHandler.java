package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.RentAdvance;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.RentAdvanceDto;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.UpdateRentAdvanceDto;
import co.ke.proaktivio.qwanguapi.services.RentAdvanceService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.RentAdvanceDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.UpdateRentAdvanceDtoValidator;
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
public class RentAdvanceHandler {
    private final RentAdvanceService rentAdvanceService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(RentAdvanceDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(validateRentAdvanceDtoFunc(new RentAdvanceDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create advance was successful"))
                .flatMap(rentAdvanceService::create)
                .doOnSuccess(a -> log.info(" Created advance {} successfully for payment", a.getPaymentId()))
                .doOnError(e -> log.error(" Failed to create advance. Error ", e))
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/advances/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(), true, "Advance created successfully.",
                                created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating advance", a.rawStatusCode()));
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("advanceId");
        return request
                .bodyToMono(UpdateRentAdvanceDto.class)
                .doOnSuccess(a -> log.info(" Received request to update {}", a))
                .map(validateUpdateRentAdvanceDtoFunc(new UpdateRentAdvanceDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to update user was successful"))
                .flatMap(dto -> rentAdvanceService.update(id, dto))
                .doOnSuccess(a -> log.info(" Updated advance {} successfully for payment", a.getPaymentId()))
                .doOnError(e -> log.error(" Failed to update advance. Error ", e))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Advance updated successfully.",
                                        updated)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for updating advance", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("advanceId");
        Optional<String> status = request.queryParam("status");
        Optional<String> occupationId = request.queryParam("occupationId");
        Optional<String> paymentId = request.queryParam("paymentId");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        if (status.isPresent() && !EnumUtils.isValidEnum(RentAdvance.Status.class, status.get())) {
            String[] arrayOfState = Stream.of(RentAdvance.Status.values()).map(RentAdvance.Status::getState).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Status should be " + states + "!");
        }
        log.info(" Received request for querying advances");
        return rentAdvanceService.findPaginated(
                        id,
                        status.map(RentAdvance.Status::valueOf),
                        occupationId,
                        paymentId,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} users", a.size()))
                .doOnError(e -> log.error(" Failed to find users. Error ", e))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Advances found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying advances", a.rawStatusCode()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("advanceId");
        log.info(" Received request to delete advance with id {}", id);
        return rentAdvanceService
                .deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted user successfully"))
                .doOnError(e -> log.error(" Failed to delete user. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Advance with id %s deleted successfully."
                                        .formatted(id), null)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting advance", a.rawStatusCode()));
    }

    private Function<RentAdvanceDto, RentAdvanceDto> validateRentAdvanceDtoFunc(Validator validator) {
        return rentAdvanceDto -> {
            Errors errors = new BeanPropertyBindingResult(rentAdvanceDto, RentAdvanceDto.class.getName());
            validator.validate(rentAdvanceDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return rentAdvanceDto;
        };
    }

    private Function<UpdateRentAdvanceDto, UpdateRentAdvanceDto> validateUpdateRentAdvanceDtoFunc(Validator validator) {
        return rentAdvanceDto -> {
            Errors errors = new BeanPropertyBindingResult(rentAdvanceDto, RentAdvanceDto.class.getName());
            validator.validate(rentAdvanceDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return rentAdvanceDto;
        };
    }
}
