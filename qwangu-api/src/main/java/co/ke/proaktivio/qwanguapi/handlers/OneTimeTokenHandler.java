package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Response;
import co.ke.proaktivio.qwanguapi.services.OneTimeTokenService;
import co.ke.proaktivio.qwanguapi.validators.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Component
@RequiredArgsConstructor
public class OneTimeTokenHandler {
    private final OneTimeTokenService oneTimeTokenService;
    
    public Mono<ServerResponse> findAll(ServerRequest request) {
        Optional<String> tokenOptional = request.queryParam("token");
        Optional<String> userIdOptional = request.queryParam("userId");
		Optional<String> orderOptional = request.queryParam("order");
		
		ValidationUtil.vaidateOrderType(orderOptional);
        log.debug("Received request for querying tokens");
        
        return oneTimeTokenService
				.findAll(tokenOptional.orElse(null), userIdOptional.orElse(null),
						orderOptional.map(OrderType::valueOf).orElse(OrderType.DESC))
				.collectList()
				.doOnSuccess(a -> log.info("Request returned {} tokens", a.size()))
				.doOnError(e -> log.error("Failed to find tokens. Error ", e))
				.flatMap(results -> {
					var isEmpty = results.isEmpty();
					var message = !isEmpty ? "Tokens found successfully." : "Tokens were not found!";
					return ServerResponse.ok().body(Mono.just(new Response<>(LocalDateTime.now().toString(),
							request.uri().getPath(), HttpStatus.OK.value(), !isEmpty, message, results)),
							Response.class);

				})
				.doOnSuccess(
						a -> log.debug("Sent response with status code {} for querying users", a.rawStatusCode()));
	}
}
