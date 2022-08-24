package co.ke.proaktivio.qwanguapi.security;

import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import co.ke.proaktivio.qwanguapi.services.UserTokenService;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CustomReactiveAuthenticationManagerImplTest {
    @Mock
    JwtUtil util;
    @Mock
    UserTokenService userTokenService;

    private final static String TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQURNSU4iLCJzdWIiOiJwZXJzb25AZ21haWwuY29tIiwiaWF0IjoxNjU1MjAwODM1LCJleHAiOjE2NTUyMDQ0MzV9.S1AG9Tgxhufl2Ffd5VeeEBuThvVhlzDneDbiZwl2_kwHI2AS8MVLWeMLMsp7CAs6";
    private final List<String> authorities = List.of("ROLE_ADMIN", "APARTMENT_CREATE", "APARTMENT_UPDATE",
            "APARTMENT_READ");

    @InjectMocks
    CustomReactiveAuthenticationManagerImpl authenticationManager;

    @Test
    void authenticate_returnsAuthentication_whenSuccessful() {
        // given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("authorities", authorities);
        DefaultClaims claims = new DefaultClaims(claimsMap);
        Authentication authentication = new UsernamePasswordAuthenticationToken(TOKEN, TOKEN);
        // when
        when(util.getUsername(TOKEN)).thenReturn("person@gmail.com");
        when(util.isValid(TOKEN)).thenReturn(true);
        when(userTokenService.exists(anyString(), anyString())).thenReturn(Mono.just(true));
        when(util.getClaims(TOKEN)).thenReturn(claims);
        // then
        StepVerifier
                .create(authenticationManager.authenticate(authentication))
                .expectNextMatches(auth -> ((String) auth.getPrincipal()).equalsIgnoreCase("person@gmail.com") &&
                        auth.isAuthenticated() && !auth.getAuthorities().isEmpty())
                .verifyComplete();
    }

    @Test
    void authenticate_returnsJwtException_whenTokenIsNotValid() {
        // given
        Authentication authentication = new UsernamePasswordAuthenticationToken(TOKEN, TOKEN);
        // when
        when(util.getUsername(TOKEN)).thenReturn("person@gmail.com");
        when(util.isValid(TOKEN)).thenReturn(false);
        // then
        StepVerifier
                .create(authenticationManager.authenticate(authentication))
                .expectErrorMatches(e -> e instanceof JwtException &&
                        e.getMessage().equals("Unauthorised token!"))
                .verify();
    }

    @Test
    void authenticate_returnsJwtException_whenRoleIsEmptyInClaims() {
        // given
        Map<String, Object> claimsMap = new HashMap<>();
        DefaultClaims claims = new DefaultClaims(claimsMap);
        Authentication authentication = new UsernamePasswordAuthenticationToken(TOKEN, TOKEN);
        // when
        when(util.getUsername(TOKEN)).thenReturn("person@gmail.com");
        when(userTokenService.exists(anyString(), anyString())).thenReturn(Mono.empty());
        when(util.isValid(TOKEN)).thenReturn(true);
        when(util.getClaims(TOKEN)).thenReturn(claims);
        // then
        StepVerifier
                .create(authenticationManager.authenticate(authentication))
                .expectErrorMatches(e -> e instanceof JwtException &&
                        e.getMessage().equals("Unauthorised token!"))
                .verify();
    }

    @Test
    void authenticate_returnsJwtException_whenUsernameIsEmpty() {
        // given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("authorities", authorities);
        DefaultClaims claims = new DefaultClaims(claimsMap);
        Authentication authentication = new UsernamePasswordAuthenticationToken(TOKEN, TOKEN);
        // when
        when(util.getUsername(TOKEN)).thenReturn("");
        when(util.isValid(TOKEN)).thenReturn(true);
        when(userTokenService.exists(anyString(), anyString())).thenReturn(Mono.empty());
        when(util.getClaims(TOKEN)).thenReturn(claims);
        // then
        StepVerifier
                .create(authenticationManager.authenticate(authentication))
                .expectErrorMatches(e -> e instanceof JwtException &&
                        e.getMessage().equals("Unauthorised token!"))
                .verify();
    }
}