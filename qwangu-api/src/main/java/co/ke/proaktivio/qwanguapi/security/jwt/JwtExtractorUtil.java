package co.ke.proaktivio.qwanguapi.security.jwt;

import io.jsonwebtoken.Claims;

import java.time.LocalDateTime;

public interface JwtExtractorUtil {

    Claims getClaims(String token);

    String getUsername(String token);

    LocalDateTime getExpirationDate(String token);
}
