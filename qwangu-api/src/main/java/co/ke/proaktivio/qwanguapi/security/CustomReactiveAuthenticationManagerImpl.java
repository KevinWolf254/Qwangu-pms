package co.ke.proaktivio.qwanguapi.security;

import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
                    String role = claims.get("role", String.class);
                    return role == null ? Optional.empty() : Optional.of(role);
                })
                .filter(Optional::isPresent)
                .map(role -> username == null || username.isEmpty() || username.isBlank() ? Optional.empty() : role)
                .filter(Optional::isPresent)
                .flatMap(optRole -> Mono.just((Authentication) new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority((String) optRole.get())))
                ))
                .switchIfEmpty(Mono.just(new UsernamePasswordAuthenticationToken(null, null, List.of())));
    }
}
