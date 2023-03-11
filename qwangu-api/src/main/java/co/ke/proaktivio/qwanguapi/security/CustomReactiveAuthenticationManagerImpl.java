package co.ke.proaktivio.qwanguapi.security;

import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import co.ke.proaktivio.qwanguapi.services.UserTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomReactiveAuthenticationManagerImpl implements CustomReactiveAuthenticationManager {
    private final JwtUtil jwtUtil;
    private final UserTokenService userTokenService;

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        String username = jwtUtil.getUsername(token);
        return Mono
                .just(jwtUtil.isValid(token))
                .filter(isValid -> isValid)
                .flatMap($ -> userTokenService.exists(username, token))
                .filter(isCurrent -> isCurrent)
                .map($ -> {
                    Claims claims = jwtUtil.getClaims(token);
					List<String> authoritiesResult = claims.get("authorities", List.class);
                    List<String> authorities = authoritiesResult != null && !authoritiesResult.isEmpty()
                            ? authoritiesResult : new ArrayList<>();
                    return authorities.isEmpty() || username == null || username.isEmpty() || username.isBlank()
                            ? Optional.empty() : Optional.of(authorities);
                })
                .filter(Optional::isPresent)
                .flatMap(authorities -> Mono.just((Authentication) new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities.isPresent()
                                ? ((List<String>) authorities.get()).stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toSet())
                                : Set.of()
                        )
                ))
                .switchIfEmpty(Mono.error(new JwtException("Unauthorised token!")));
    }
}
