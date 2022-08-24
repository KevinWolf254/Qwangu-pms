package co.ke.proaktivio.qwanguapi.security.jwt;

public interface JwtValidator {
    Boolean isTokenExpired(String token);
    Boolean isValid(String token);
}
