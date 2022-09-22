package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.pojos.UserWithoutPasswordDto;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;

@Log4j2
@Component
@RequiredArgsConstructor
public class PaymentHandler {
    private final PaymentService paymentService;

    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("paymentId");
        return paymentService.findById(id)
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true,"Payment found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.info(" Sent response with status code {} for querying payment by id", a.rawStatusCode()));
    }

    public Mono<ServerResponse> find(ServerRequest request) {
        Optional<String> status = request.queryParam("status");
        Optional<String> type = request.queryParam("type");
        Optional<String> shortCode = request.queryParam("shortCode");
        Optional<String> referenceNo = request.queryParam("referenceNo");
        Optional<String> mobileNumber = request.queryParam("mobileNumber");
        Optional<String> page = request.queryParam("page");
        Optional<String> pageSize = request.queryParam("pageSize");
        Optional<String> order = request.queryParam("order");

        if (status.isPresent() &&  !EnumUtils.isValidEnum(Payment.Status.class, status.get())) {
            String[] arrayOfState = Stream.of(Payment.Status.values()).map(Payment.Status::getState).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Status should be " + states + "!");
        }

        if (status.isPresent() &&  !EnumUtils.isValidEnum(Payment.Type.class, type.get())) {
            String[] arrayOfState = Stream.of(Payment.Type.values()).map(Payment.Type::getType).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Type should be " + states + "!");
        }
        log.info(" Request for querying payments");
        return paymentService.findPaginated(
                        status.map(Payment.Status::valueOf),
                        type.map(Payment.Type::valueOf),
                        shortCode,
                        referenceNo,
                        mobileNumber,
                        page.map(p -> CustomUtils.convertToInteger(p, "Page")).orElse(1),
                        pageSize.map(ps -> CustomUtils.convertToInteger(ps, "Page size")).orElse(10),
                        order.map(OrderType::valueOf).orElse(OrderType.DESC)
                )
                .collectList()
                .doOnSuccess(a -> log.info(" Query request returned {} payments", a.size()))
                .doOnError(e -> log.error(" Failed to find payments. Error ", e))
                .flatMap(results ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(),true, "Payments found successfully.",
                                        results)), Response.class))
                .doOnSuccess(a -> log.debug(" Sent response with status code {} for querying payments", a.rawStatusCode()));
    }

}
