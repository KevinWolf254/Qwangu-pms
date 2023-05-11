package co.ke.proaktivio.qwanguapi.security;

import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class CustomServerSecurityContextRepositoryImpl implements CustomServerSecurityContextRepository {
    private final ReactiveAuthenticationManager authenticationManager;

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono
                .justOrEmpty(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                .flatMap(authHeader -> {                	
                	if(authHeader.startsWith("Bearer ")) {
                		var token = authHeader.substring(7);
                		if(StringUtils.hasText(token)) {
                			return this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(token, token))
                	                .map(SecurityContextImpl::new);
                		}
                		return Mono.error(new JwtException("Token required!"));
                	}
                	return Mono.empty();
                });
    }
}
