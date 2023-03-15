package co.ke.proaktivio.qwanguapi.handlers;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class AuthorityHandler {
	private final AuthorityService authorityService;

	public Mono<ServerResponse> findById(ServerRequest request) {
		String id = request.pathVariable("authorityId");
		return authorityService.findById(id)
				.switchIfEmpty(
						Mono.error(new CustomNotFoundException("Authority with id %s does not exist!".formatted(id))))
				.flatMap(results -> {
					return ServerResponse
							.ok().body(
									Mono.just(
											new Response<>(LocalDateTime.now().toString(), request.uri().getPath(),
													HttpStatus.OK.value(), true, "Authority found successfully.",
													results)),
									Response.class);
				}).doOnSuccess(a -> log.debug("Sent response with status code {} for querying Authority by id",
						a.rawStatusCode()));
	}

	public Mono<ServerResponse> findAll(ServerRequest request) {
		Optional<String> nameOptional = request.queryParam("name");
		Optional<String> orderOptional = request.queryParam("order");

		ValidationUtil.vaidateOrderType(orderOptional);
		log.debug("Received request for querying invoices.");
		return authorityService
				.findAll(nameOptional.orElse(null), orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC))
				.collectList()
				.doOnSuccess(a -> log.info("Request returned a list of {} invoices.", a.size()))
				.doOnError(e -> log.error("Failed to find Authorities. Error ", e)).flatMap(results -> {
					var isEmpty = results.isEmpty();
					return ServerResponse
							.ok().body(
									Mono.just(
											new Response<>(LocalDateTime.now().toString(), request.uri().getPath(),
													HttpStatus.OK.value(), !isEmpty,
													!isEmpty ? "Authorities found successfully."
															: "Authorities could not be found!",
													results)),
									Response.class);

				}).doOnSuccess(a -> log.debug("Sent response with status code {} for querying Authorities",
						a.rawStatusCode()));
	}
}
