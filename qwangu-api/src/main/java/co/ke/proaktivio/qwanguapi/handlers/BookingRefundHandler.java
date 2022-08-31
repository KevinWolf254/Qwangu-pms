package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.BookingRefundDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.BookingRefundService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.BookingRefundDtoValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Component
@RequiredArgsConstructor
public class BookingRefundHandler {
    private final BookingRefundService bookingRefundService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(BookingRefundDto.class)
                .doOnSuccess(a -> log.info(" Received request to create {}", a))
                .map(validateBookingRefundDtoFunc(new BookingRefundDtoValidator()))
                .doOnSuccess(a -> log.debug(" Validation of request to create refund was successful"))
                .flatMap(bookingRefundService::create)
                .doOnSuccess(a -> log.info(" Created refund for receivable {} successfully", a.getReceivableId()))
                .doOnError(e -> log.error(" Failed to create refund. Error ", e))
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/refunds/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "Refund created successfully.",
                                created)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for creating refund", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("refundId");
        Optional<String> receivableId = request.queryParam("receivableId");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        log.info(" Received request for querying refunds");
        return bookingRefundService.findPaginated(
                        id,
                        receivableId,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} refunds", a.size()))
                .doOnError(e -> log.error(" Failed to find refunds. Error ", e))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Refunds found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying refunds", a.rawStatusCode()));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("refundId");
        log.info(" Received request to delete refund with id {}", id);
        return bookingRefundService
                .deleteById(id)
                .doOnSuccess($ -> log.info(" Deleted refund successfully"))
                .doOnError(e -> log.error(" Failed to delete refund. Error ", e))
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Refund with id %s deleted successfully."
                                        .formatted(id), null)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for deleting refund", a.rawStatusCode()));
    }

    private Function<BookingRefundDto, BookingRefundDto> validateBookingRefundDtoFunc(Validator validator) {
        return bookingRefundDto -> {
            Errors errors = new BeanPropertyBindingResult(bookingRefundDto, BookingRefundDto.class.getName());
            validator.validate(bookingRefundDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return bookingRefundDto;
        };
    }
}
