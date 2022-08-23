package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.pojos.BookingRefundDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.BookingRefundService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.BookingRefundDtoValidator;
import lombok.RequiredArgsConstructor;
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

@Component
@RequiredArgsConstructor
public class BookingRefundHandler {
    private final BookingRefundService bookingRefundService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(BookingRefundDto.class)
                .map(validateBookingRefundDtoFunc(new BookingRefundDtoValidator()))
                .flatMap(bookingRefundService::create)
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/refunds/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "Refund created successfully.",
                                created)), Response.class));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("id");
        Optional<String> receivableId = request.queryParam("receivableId");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");
        return bookingRefundService.findPaginated(
                        id,
                        receivableId,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                ).collectList()
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Refunds found successfully.",
                                        results)), Response.class));
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return bookingRefundService
                .deleteById(id)
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Refund with id %s deleted successfully."
                                        .formatted(id), null)), Response.class));
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