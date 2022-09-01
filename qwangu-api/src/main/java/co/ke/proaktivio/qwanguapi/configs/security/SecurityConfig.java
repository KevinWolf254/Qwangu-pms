package co.ke.proaktivio.qwanguapi.configs.security;

import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
                .cors(Customizer.withDefaults())
                .exceptionHandling()
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .formLogin().disable()
                .authenticationManager(authenticationManager)
                .securityContextRepository(contextRepository)
                .authorizeExchange()
                .pathMatchers(
                        "/v3/api-docs/**",
                        "/configuration/ui",
                        "/swagger-resources/**",
                        "/configuration/security",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/webjars/**",

                        "/v1/signIn",
                        "/v1/users/sendResetPassword",
                        "/v1/tokens",
                        "/v1/resetPassword"
                ).permitAll()
                .pathMatchers( "/v1/apartments/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers( "/v1/units/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers( "/v1/occupations/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers( "/v1/tenants/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers( "/v1/notices/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers( "/v1/users/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers( "/v1/roles/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers( "/v1/authorities/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers( "/v1/refunds/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers( "/v1/advances/**").hasAnyRole("SUPER_ADMIN")
                .pathMatchers("/v1/mpesa/c2b/**").access(this::whiteListIp)
                .anyExchange().authenticated()
                .and()
                .build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(6);
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
        configuration.setAllowedOrigins(List.of("*"));
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
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN
                )
        );
        configuration.setExposedHeaders(List.of(HttpHeaders.CONTENT_TYPE));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
