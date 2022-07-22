package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Booking;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.BookingService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import co.ke.proaktivio.qwanguapi.validators.CreateBookingDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.CreateNoticeDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.UpdateBookingDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.UpdateNoticeDtoValidator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.EnumUtils;
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

import static co.ke.proaktivio.qwanguapi.utils.CustomErrorUtil.handleExceptions;

@Component
@RequiredArgsConstructor
public class BookingHandler {
    private final BookingService bookingService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(CreateBookingDto.class)
                .map(validateCreateBookingDtoFunc(new CreateBookingDtoValidator()))
                .flatMap(bookingService::create)
                .flatMap(created ->
                        ServerResponse.created(URI.create("v1/bookings/%s".formatted(created.getId())))
                                .body(Mono.just(new SuccessResponse<>(true, "Booking created successfully.", created)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        String id = request.pathVariable("id");
        return request.bodyToMono(UpdateBookingDto.class)
                .map(validateUpdateBookingDtoFunc(new UpdateBookingDtoValidator()))
                .flatMap(dto -> bookingService.update(id, dto))
                .flatMap(updated ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Booking updated successfully.", updated)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> id = request.queryParam("id");
        Optional<String> status = request.queryParam("status");
        Optional<String> unitId = request.queryParam("unitId");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");

        try {
            if (status.isPresent() &&  !EnumUtils.isValidEnum(Booking.Status.class, status.get()))
                throw new CustomBadRequestException("Status is not valid!");

            return bookingService.findPaginated(
                            id,
                            status.map(Booking.Status::valueOf),
                            unitId,
                            page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                            pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                            order.map(OrderType::valueOf).orElse(OrderType.DESC)
                    ).collectList()
                    .flatMap(results ->
                            ServerResponse
                                    .ok()
                                    .body(Mono.just(new SuccessResponse<>(true, "Bookings found successfully.", results)), SuccessResponse.class)
                                    .log())
                    .onErrorResume(handleExceptions());
        } catch (Exception e) {
            return handleExceptions(e);
        }
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        String id = request.pathVariable("id");
        return bookingService.deleteById(id)
                .flatMap(result ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new SuccessResponse<>(true, "Booking with id %s deleted successfully.".formatted(id), null)), SuccessResponse.class)
                                .log())
                .onErrorResume(handleExceptions());
    }

    private Function<CreateBookingDto, CreateBookingDto> validateCreateBookingDtoFunc(Validator validator) {
        return createUpdateDto -> {
            Errors errors = new BeanPropertyBindingResult(createUpdateDto, CreateBookingDto.class.getName());
            validator.validate(createUpdateDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return createUpdateDto;
        };
    }

    private Function<UpdateBookingDto, UpdateBookingDto> validateUpdateBookingDtoFunc(Validator validator) {
        return updateUpdateDto -> {
            Errors errors = new BeanPropertyBindingResult(updateUpdateDto, UpdateBookingDto.class.getName());
            validator.validate(updateUpdateDto, errors);
            if (!errors.getAllErrors().isEmpty()) {
                String errorMessage = errors.getAllErrors().stream()
                        .map(DefaultMessageSourceResolvable::getDefaultMessage)
                        .collect(Collectors.joining(" "));
                throw new CustomBadRequestException(errorMessage);
            }
            return updateUpdateDto;
        };
    }
}
