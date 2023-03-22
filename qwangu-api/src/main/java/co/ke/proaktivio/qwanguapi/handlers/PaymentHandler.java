package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
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

	public Mono<ServerResponse> findById(ServerRequest request) {
		String id = request.pathVariable("paymentId");
		return paymentService.findById(id)
				.switchIfEmpty(
						Mono.error(new CustomNotFoundException("Payment with id %s does not exist!".formatted(id))))
				.flatMap(results -> ServerResponse.ok()
						.body(Mono.just(new Response<>(LocalDateTime.now().toString(), request.uri().getPath(),
								HttpStatus.OK.value(), true, "Payment found successfully.", results)), Response.class))
				.doOnSuccess(a -> log.info(" Sent response with status code {} for querying payment by id",
						a.rawStatusCode()));
	}

	public Mono<ServerResponse> findAll(ServerRequest request) {
		Optional<String> statusOptional = request.queryParam("status");
		Optional<String> typeOptional = request.queryParam("type");
		Optional<String> referenceNumberOptional = request.queryParam("referenceNumber");
		Optional<String> orderOptional = request.queryParam("order");

		if (statusOptional.isPresent() && StringUtils.hasText(statusOptional.get())
				&& !EnumUtils.isValidEnum(PaymentStatus.class, statusOptional.get())) {
			String[] arrayOfState = Stream.of(PaymentStatus.values()).map(PaymentStatus::getState)
					.toArray(String[]::new);
			String states = String.join(" or ", arrayOfState);
			throw new CustomBadRequestException("Status should be " + states + "!");
		}
		if (typeOptional.isPresent() && StringUtils.hasText(typeOptional.get())
				&& !EnumUtils.isValidEnum(PaymentType.class, typeOptional.get())) {
			String[] arrayOfState = Stream.of(PaymentType.values()).map(PaymentType::getType).toArray(String[]::new);
			String states = String.join(" or ", arrayOfState);
			throw new CustomBadRequestException("Type should be " + states + "!");
		}

		ValidationUtil.vaidateOrderType(orderOptional);
		return ServerResponse.ok()
				.body(paymentService.findAll(statusOptional.map(PaymentStatus::valueOf).orElse(null),
						typeOptional.map(PaymentType::valueOf).orElse(null), referenceNumberOptional.orElse(null),
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
