package co.ke.proaktivio.qwanguapi.configs.security;

import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authorization.AuthorizationContext;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
@EnableReactiveMethodSecurity
public class SecurityConfig {
    private final ReactiveAuthenticationManager authenticationManager;
    private final ServerSecurityContextRepository contextRepository;
    private final MpesaPropertiesConfig mpesaPropertiesConfig;

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .exceptionHandling()
//                .authenticationEntryPoint((exchange, exception) -> Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED)))
//                .accessDeniedHandler((exchange, exception) -> Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN)))
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .formLogin().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(contextRepository)
                .authorizeExchange()
                .pathMatchers(
                        "/v1/signIn",
                        "/v1/users/sendResetPassword",
                        "/v1/token",
                        "/v1/resetPassword"
                ).permitAll()
//                .pathMatchers(HttpMethod.GET, "/v1/users/**").hasRole("ACCOUNTANT")
                .pathMatchers("/v1/mpesa/c2b/**").access(this::whiteListIp)
                .anyExchange().authenticated()
                .and()
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(16);
    }

    private Mono<AuthorizationDecision> whiteListIp(Mono<Authentication> authentication, AuthorizationContext context) {
        String ip = Objects.requireNonNull(context.getExchange().getRequest().getRemoteAddress()).getAddress().toString().replace("/", "");
        boolean contains = mpesaPropertiesConfig.getWhiteListedUrls().contains(ip);
        return Mono.just(new AuthorizationDecision(contains));
    }

//    private Mono<AuthorizationDecision> whiteListIpOfAuthenticatedUsers(Mono<Authentication> authentication, AuthorizationContext context) {
//        String ip = Objects.requireNonNull(context.getExchange().getRequest().getRemoteAddress()).getAddress().toString().replace("/", "");
//        boolean contains = mpesaPropertiesConfig.getWhiteListedUrls().contains(ip);
//        return authentication
//                .map((a) -> new AuthorizationDecision(true))
//                .defaultIfEmpty(new AuthorizationDecision(contains));
//    }
}
