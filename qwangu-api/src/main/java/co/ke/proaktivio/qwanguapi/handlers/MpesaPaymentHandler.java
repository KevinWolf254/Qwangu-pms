package co.ke.proaktivio.qwanguapi.handlers;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentResponse;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.MpesaPaymentService;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class MpesaPaymentHandler {
	private final MpesaPaymentService mpesaPaymentService;

    public Mono<ServerResponse> validate(ServerRequest request) {
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

    public Mono<ServerResponse> create(ServerRequest request) {
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
    
    public Mono<ServerResponse> findById(ServerRequest request) {
        String id = request.pathVariable("mpesaPaymentId");
        return mpesaPaymentService.findById(id)
        		.switchIfEmpty(Mono.error(new CustomNotFoundException("Payment with id %s does not exist!".formatted(id))))
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
		Optional<String> transactionIdOptional = request.queryParam("transactionId");
		Optional<String> referenceNumberOptional = request.queryParam("referenceNumber");
		Optional<String> shortCodeOptional = request.queryParam("shortCode");
		Optional<String> orderOptional = request.queryParam("order");

		ValidationUtil.vaidateOrderType(orderOptional);
		return ServerResponse.ok()
				.body(mpesaPaymentService.findAll(transactionIdOptional.orElse(null),
						referenceNumberOptional.orElse(null), shortCodeOptional.orElse(null),
						orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC)).collectList()
						.flatMap(payments -> {
							boolean isEmpty = payments.isEmpty();
							var message = !isEmpty ? "Payments found successfully."
									: "Payments with those parameters do  not exist!";

							return Mono.just(new Response<>(LocalDateTime.now().toString(), request.uri().getPath(),
									HttpStatus.OK.value(), !isEmpty, message, payments));
						}), Response.class);
	}
}
