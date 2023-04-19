package co.ke.proaktivio.qwanguapi.configs.security;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Component
@RequiredArgsConstructor
public class MpesaIPAddressWhiteListFilter implements WebFilter {
	private final MpesaPropertiesConfig mpesaPropertiesConfig;

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
		var targetedResourcePath = exchange.getRequest().getPath().value();

		return Mono.defer(() -> {
			var targetsMpesaAPi = targetedResourcePath.contains("mpesa");

			if (!targetsMpesaAPi || (targetsMpesaAPi && !exchange.getRequest().getMethod().equals(HttpMethod.POST))) {
				return chain.filter(exchange);
			}
			
			var remoteHost = exchange.getRequest().getRemoteAddress().getHostName();
			return Mono.just(remoteHost);
		})
		.flatMap(remoteHost -> {
			var isFromSafaricom = mpesaPropertiesConfig.getWhiteListedUrls().contains(remoteHost);
			if (!isFromSafaricom) {
				String errorMessage = "Remote address %s is not authorized to access the resource %s!"
						.formatted(remoteHost, targetedResourcePath);
				log.error(errorMessage);
				return Mono.error(new CustomAccessDeniedException(errorMessage));
			}
			return chain.filter(exchange);
		})
		.onErrorMap(CustomAccessDeniedException.class, t -> {
			return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Remote address is not authorized to access resource!");
		});
	}
}
