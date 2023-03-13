package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentResponse;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.MpesaPaymentService;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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
	private final MpesaPaymentService mpesaPaymentService;

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

    public Mono<ServerResponse> findAll(ServerRequest request) {
        Optional<String> statusOptional = request.queryParam("status");
        Optional<String> typeOptional = request.queryParam("type");
        Optional<String> referenceNumberOptional = request.queryParam("referenceNumber");
        Optional<String> mpesaPaymentIdOptional = request.queryParam("mpesaPaymentId");
        Optional<String> orderOptional = request.queryParam("order");

        if (statusOptional.isPresent() && StringUtils.hasText(statusOptional.get()) && !EnumUtils.isValidEnum(PaymentStatus.class, statusOptional.get())) {
            String[] arrayOfState = Stream.of(PaymentStatus.values()).map(PaymentStatus::getState).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Status should be " + states + "!");
        }
        if (typeOptional.isPresent() && StringUtils.hasText(typeOptional.get()) && !EnumUtils.isValidEnum(PaymentType.class, typeOptional.get())) {
            String[] arrayOfState = Stream.of(PaymentType.values()).map(PaymentType::getType).toArray(String[]::new);
            String states = String.join(" or ", arrayOfState);
            throw new CustomBadRequestException("Type should be " + states + "!");
        }
        return ServerResponse
                .ok()
                .body(paymentService.findAll(
                		statusOptional.map(PaymentStatus::valueOf).orElse(null),
                		typeOptional.map(PaymentType::valueOf).orElse(null),
                		referenceNumberOptional.orElse(null),
                		mpesaPaymentIdOptional.orElse(null),
                		orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC))
                        .collectList()
                        .flatMap(payments -> {
                            if(payments.isEmpty())
                                return Mono.just(new Response<>(
                                        LocalDateTime.now().toString(),
                                        request.uri().getPath(),
                                        HttpStatus.OK.value(), true, "Payments with those parameters do  not exist!",
                                        payments));
                            return Mono.just(new Response<>(
                                    LocalDateTime.now().toString(),
                                    request.uri().getPath(),
                                    HttpStatus.OK.value(), true, "Payments found successfully.",
                                    payments));
                        }), Response.class);
    }

    public Mono<ServerResponse> validateMpesa(ServerRequest request) {
        return request
        		.bodyToMono(MpesaPaymentDto.class)
                .doOnSuccess(a -> log.info("Received request to validate {}", a))
                .flatMap(mpesaPaymentService::validate)
                .doOnSuccess(a -> log.info("Validated successfully"))
                .doOnError(e -> log.error("Failed to validate. Error ", e))
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), MpesaPaymentResponse.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for validating", a.rawStatusCode()));
    }

    public Mono<ServerResponse> createMpesa(ServerRequest request) {
        return request.bodyToMono(MpesaPaymentDto.class)
                .doOnSuccess(a -> log.info("Received request to create {}", a))
                .flatMap(mpesaPaymentService::create)
                .doOnSuccess(a -> log.info("Created c2b successfully"))
                .doOnError(e -> log.error("Failed to create c2b. Error ", e))
                .flatMap(response ->
                        ServerResponse
                                .ok()
                                .body(Mono.just(response), MpesaPaymentResponse.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for creating c2b", a.rawStatusCode()));
    }

}
