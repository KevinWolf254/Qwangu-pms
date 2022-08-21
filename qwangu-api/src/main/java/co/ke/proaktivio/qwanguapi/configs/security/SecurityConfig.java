package co.ke.proaktivio.qwanguapi.configs.security;

import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpHeaders;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
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
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .exceptionHandling()
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


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setMaxAge(8000L);
        configuration.applyPermitDefaultValues();
//        configuration.setAllowedOrigins(List.of());
        configuration.setAllowedOrigins(Collections.singletonList("http://any-origin.com"));
//        configuration.setAllowedOriginPatterns(Collections.singletonList("http://any-origin.com"));
        configuration.setAllowedMethods(
                List.of(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
        );
        configuration.setAllowedHeaders(
                List.of(
                        HttpHeaders.CONTENT_TYPE,
                        HttpHeaders.AUTHORIZATION,
                        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                )
        );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
