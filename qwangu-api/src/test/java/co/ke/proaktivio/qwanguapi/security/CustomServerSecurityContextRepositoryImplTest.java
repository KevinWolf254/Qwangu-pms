package co.ke.proaktivio.qwanguapi.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CustomServerSecurityContextRepositoryImplTest {
    @Mock
    private ReactiveAuthenticationManager authenticationManager;
    @InjectMocks
    private CustomServerSecurityContextRepositoryImpl sscr;
    private final static String TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQURNSU4iLCJzdWIiOiJwZXJzb25AZ21haWwuY29tIiwiaWF0IjoxNjU1MjAwODM1LCJleHAiOjE2NTUyMDQ0MzV9.S1AG9Tgxhufl2Ffd5VeeEBuThvVhlzDneDbiZwl2_kwHI2AS8MVLWeMLMsp7CAs6";

    @Test
    void save() {
    }

    @Test
    void load_returnsMonoOfSecurityContext() {
        // given
        MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest
                .get("/");
        requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer ".concat(TOKEN));
        Authentication authentication = new UsernamePasswordAuthenticationToken(TOKEN, TOKEN);
        Authentication authResult = new UsernamePasswordAuthenticationToken(
                "person@gmail.com",
                null,
                List.of(new SimpleGrantedAuthority("ADMIN")));
        ServerWebExchange exchange = new MockServerWebExchange.Builder(requestBuilder.build()).build();

        // when
        when(authenticationManager.authenticate(authentication)).thenReturn(Mono.just(authResult));

        // then
        StepVerifier
                .create(sscr.load(exchange))
                .expectNext(new SecurityContextImpl(authResult))
                .verifyComplete();
    }

    @Test
    void load_returnsJwtException_whenAuthorizationHeaderDoesNotExist() {
        // given
        MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest
                .get("/");
        ServerWebExchange exchange = new MockServerWebExchange.Builder(requestBuilder.build()).build();

        // when

        // then
        StepVerifier
                .create(sscr.load(exchange))
                .expectErrorMatches(e -> e instanceof JwtException &&
                        e.getMessage().equals("Bearer required!"))
                .verify();
    }

    @Test
    void load_returnsJwtException_whenAuthorizationHeaderHasNoValue() {
        // given
        MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest
                .get("/");
        requestBuilder.header(HttpHeaders.AUTHORIZATION, "");
        ServerWebExchange exchange = new MockServerWebExchange.Builder(requestBuilder.build()).build();

        // when

        // then
        StepVerifier
                .create(sscr.load(exchange))
                .expectErrorMatches(e -> e instanceof JwtException &&
                        e.getMessage().equals("Bearer required!"))
                .verify();
    }

    @Test
    void load_returnsJwtException_whenAuthorizationHeaderHasBearerWithNoToken() {
        // given
        MockServerHttpRequest.BaseBuilder<?> requestBuilder = MockServerHttpRequest
                .get("/");
        requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer ");
        ServerWebExchange exchange = new MockServerWebExchange.Builder(requestBuilder.build()).build();

        // when

        // then
        StepVerifier
                .create(sscr.load(exchange))
                .expectErrorMatches(e -> e instanceof JwtException &&
                        e.getMessage().equals("Bearer required!") || e.getMessage().equals("Token required!"))
                .verify();
    }
}