package co.ke.proaktivio.qwanguapi.handlers;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.ReceiptService;
import co.ke.proaktivio.qwanguapi.validators.ReceiptDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class ReceiptHandler {
	private final ReceiptService receiptService;

    public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(ReceiptDto.class)
                .doOnSuccess(a -> log.debug("Received request to create {}", a))
                .map(ValidationUtil.validateReceiptDto(new ReceiptDtoValidator()))
                .doOnSuccess(a -> log.debug("Validation of request to create receipt was successful."))
                .flatMap(receiptService::create)
                .doOnError(e -> log.error("Failed to create receipt. Error ", e))
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/receipts/%s".formatted(created.getId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "Receipt created successfully.",
                                created)), Response.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for creating invoice", a.rawStatusCode()));
    }
    
    // TODO CHANGE ALL TO FIND TO BE LIKE THIS RETURNS CUSTOMNOTFOUNDEXCEPTION WHEN NOT FOUND
	public Mono<ServerResponse> findById(ServerRequest request) {
		String receiptId = request.pathVariable("receiptId");
		return receiptService.findById(receiptId)
				.switchIfEmpty(Mono.error(
						new CustomNotFoundException("Receipt with id %s does not exist!".formatted(receiptId))))
				.flatMap(results -> {
					var isEmpty = results == null;
					System.out.println("IsEmpty: " + isEmpty);
					return ServerResponse.ok().body(
							Mono.just(new Response<>(LocalDateTime.now().toString(), request.uri().getPath(),
									HttpStatus.OK.value(), !isEmpty,
									!isEmpty ? "Receipt found successfully."
											: "Receipt with id %s could not be found!".formatted(receiptId),
									results)),
							Response.class);
				});
	}
    
    public Mono<ServerResponse> findAll(ServerRequest request) {
		Optional<String> occupationIdOptional = request.queryParam("occupationId");
		Optional<String> paymentIdOptional = request.queryParam("paymentId");
		Optional<String> orderOptional = request.queryParam("order");
		log.debug("Received request for querying receipts.");
		return receiptService
				.findAll(
						occupationIdOptional.map(occupationId -> occupationId).orElse(null),
						paymentIdOptional.map(paymentId -> paymentId).orElse(null),
						orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC))
				.collectList()
				.doOnSuccess(a -> log.info("Query request returned {} receipts.", a.size()))
				.doOnError(e -> log.error("Failed to find receipts. Error ", e))
				.flatMap(results -> {
					var isEmpty = results.isEmpty();
					return ServerResponse.ok().body(Mono.just(new Response<>(LocalDateTime.now().toString(),
							request.uri().getPath(), HttpStatus.OK.value(), !isEmpty,
							!isEmpty ? "Receipts found successfully." : "Receipts could not be found!", results)),
							Response.class);

				})
				.doOnSuccess(
						a -> log.debug("Sent response with status code {} for querying receipts", a.rawStatusCode()));
	}
}
