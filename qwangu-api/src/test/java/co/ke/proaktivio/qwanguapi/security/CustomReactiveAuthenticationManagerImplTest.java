package co.ke.proaktivio.qwanguapi.security;

import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class CustomReactiveAuthenticationManagerImplTest {
    @Mock
    JwtUtil util;
    private final static String TOKEN = "eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQURNSU4iLCJzdWIiOiJwZXJzb25AZ21haWwuY29tIiwiaWF0IjoxNjU1MjAwODM1LCJleHAiOjE2NTUyMDQ0MzV9.S1AG9Tgxhufl2Ffd5VeeEBuThvVhlzDneDbiZwl2_kwHI2AS8MVLWeMLMsp7CAs6";
    @InjectMocks
    CustomReactiveAuthenticationManagerImpl authenticationManager;

    @Test
    void authenticate_returnsAMonoOfAnAuthenticatedAuthentication_whenAuthenticationIsValid() {
        // given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("role", "ADMIN");
        DefaultClaims claims = new DefaultClaims(claimsMap);
        Authentication authentication = new UsernamePasswordAuthenticationToken(TOKEN, TOKEN);
        // when
        when(util.getUsername(TOKEN)).thenReturn("person@gmail.com");
        when(util.validate(TOKEN)).thenReturn(true);
        when(util.getClaims(TOKEN)).thenReturn(claims);
        // then
        StepVerifier
                .create(authenticationManager.authenticate(authentication))
                .expectNextMatches(auth -> ((String) auth.getPrincipal()).equalsIgnoreCase("person@gmail.com") &&
                        auth.isAuthenticated() && !auth.getAuthorities().isEmpty())
                .verifyComplete();
    }

    @Test
    void authenticate_returnsAMonoOfAnNullAuthentication_whenTokenIsNotValid() {
        // given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("role", "ADMIN");
        DefaultClaims claims = new DefaultClaims(claimsMap);
        Authentication authentication = new UsernamePasswordAuthenticationToken(TOKEN, TOKEN);
        // when
        when(util.getUsername(TOKEN)).thenReturn("person@gmail.com");
        when(util.validate(TOKEN)).thenReturn(false);
        // then
        StepVerifier
                .create(authenticationManager.authenticate(authentication))
                .expectNextMatches(auth -> auth.getPrincipal() == null &&
                        auth.isAuthenticated() && auth.getAuthorities().isEmpty())
                .verifyComplete();
    }

    @Test
    void authenticate_returnsAMonoOfANullAuthentication_whenRoleIsEmptyInClaims() {
        // given
        Map<String, Object> claimsMap = new HashMap<>();
        DefaultClaims claims = new DefaultClaims(claimsMap);
        Authentication authentication = new UsernamePasswordAuthenticationToken(TOKEN, TOKEN);
        // when
        when(util.getUsername(TOKEN)).thenReturn("person@gmail.com");
        when(util.validate(TOKEN)).thenReturn(true);
        when(util.getClaims(TOKEN)).thenReturn(claims);
        // then
        StepVerifier
                .create(authenticationManager.authenticate(authentication))
                .expectNextMatches(auth -> auth.getPrincipal() == null &&
                        auth.isAuthenticated() && auth.getAuthorities().isEmpty())
                .verifyComplete();
    }

    @Test
    void authenticate_returnsAMonoOfANullAuthenticatedAuthentication_whenUsernameIsEmpty() {
        // given
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("role", "ADMIN");
        DefaultClaims claims = new DefaultClaims(claimsMap);
        Authentication authentication = new UsernamePasswordAuthenticationToken(TOKEN, TOKEN);
        // when
        when(util.getUsername(TOKEN)).thenReturn("");
        when(util.validate(TOKEN)).thenReturn(true);
        when(util.getClaims(TOKEN)).thenReturn(claims);
        // then
        StepVerifier
                .create(authenticationManager.authenticate(authentication))
                .expectNextMatches(auth -> auth.getPrincipal() == null &&
                        auth.isAuthenticated() && auth.getAuthorities().isEmpty())
                .verifyComplete();
    }
}