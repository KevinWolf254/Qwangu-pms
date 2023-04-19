package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import co.ke.proaktivio.qwanguapi.validators.InvoiceDtoValidator;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.EnumUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
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
public class InvoiceHandler {
    private final InvoiceService invoiceService;

	public Mono<ServerResponse> create(ServerRequest request) {
        return request
                .bodyToMono(InvoiceDto.class)
                .doOnSuccess(i -> log.debug("Received request to create {}", i))
                .map(ValidationUtil.validateInvoiceDto(new InvoiceDtoValidator()))
                .doOnSuccess(i -> log.debug("Validation of request to create invoice was successful {}", i))
                .flatMap(invoiceService::create)
                .doOnError(e -> log.error("Failed to create invoice. Error ", e))
                .flatMap(created -> ServerResponse
                        .created(URI.create("v1/invoices/%s".formatted(created.getOccupationId())))
                        .body(Mono.just(new Response<>(
                                LocalDateTime.now().toString(),
                                request.uri().getPath(),
                                HttpStatus.CREATED.value(),true, "Invoice created successfully.",
                                created)), Response.class))
                .doOnSuccess(a -> log.debug("Sent response with status code {} for creating invoice", a.rawStatusCode()));
    }

	public Mono<ServerResponse> findById(ServerRequest request) {
		String id = request.pathVariable("invoiceId");
		return invoiceService.findById(id)
				.switchIfEmpty(
						Mono.error(new CustomNotFoundException("Invoice with id %s does not exist!".formatted(id))))
				.flatMap(results -> {
					var isEmpty = results == null;
					return ServerResponse.ok()
							.body(Mono.just(new Response<>(LocalDateTime.now().toString(), request.uri().getPath(),
									HttpStatus.OK.value(), !isEmpty,
									!isEmpty ? "Invoice found successfully." : "Invoice could not be found!", results)),
									Response.class);
				}).doOnSuccess(a -> log.debug("Sent response with status code {} for querying invoice by id",
						a.rawStatusCode()));
	}

	public Mono<ServerResponse> findAll(ServerRequest request) {
		Optional<String> typeOptional = request.queryParam("type");
		Optional<String> invoiceNoOptional = request.queryParam("invoiceNo");
		Optional<String> occupationIdOptional = request.queryParam("occupationId");
		Optional<String> orderOptional = request.queryParam("order");
		if (typeOptional.isPresent() && !EnumUtils.isValidEnum(Invoice.Type.class, typeOptional.get())) {
			String[] arrayOfState = Stream.of(Invoice.Type.values()).map(Invoice.Type::getName).toArray(String[]::new);
			String states = String.join(" or ", arrayOfState);
			throw new CustomBadRequestException("Type should be " + states + "!");
		}
		
		ValidationUtil.vaidateOrderType(orderOptional);
		log.debug("Received request for querying invoices.");
		return invoiceService
				.findAll(typeOptional.map(Invoice.Type::valueOf).orElse(null),
						invoiceNoOptional.map(invoiceNo -> invoiceNo).orElse(null),
						occupationIdOptional.map(occupationId -> occupationId).orElse(null),
						orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC))
				.collectList()
				.doOnSuccess(a -> log.info("Request returned a list of {} invoices.", a.size()))
				.doOnError(e -> log.error("Failed to find invoices. Error ", e))
				.flatMap(results -> {
					var isEmpty = results.isEmpty();
					return ServerResponse.ok().body(Mono.just(new Response<>(LocalDateTime.now().toString(),
							request.uri().getPath(), HttpStatus.OK.value(), !isEmpty,
							!isEmpty ? "Invoices found successfully." : "Invoices could not be found!", results)),
							Response.class);

				})
				.doOnSuccess(
						a -> log.debug("Sent response with status code {} for querying invoices", a.rawStatusCode()));
	}
}
