package co.ke.proaktivio.qwanguapi.security;

import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomReactiveAuthenticationManagerImpl implements CustomReactiveAuthenticationManager {
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();
        String username = jwtUtil.getUsername(token);
        return Mono
                .just(jwtUtil.validate(token))
                .filter(isValid -> isValid)
                .map(v -> {
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
                .switchIfEmpty(Mono.just(new UsernamePasswordAuthenticationToken(null, null, List.of())));
    }
}
