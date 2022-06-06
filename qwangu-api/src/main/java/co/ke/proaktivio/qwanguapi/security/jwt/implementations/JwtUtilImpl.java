package co.ke.proaktivio.qwanguapi.security.jwt.implementations;

import co.ke.proaktivio.qwanguapi.configs.JwtPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtilImpl implements JwtUtil {
    private final JwtPropertiesConfig jwtProperties;

    private final String expirationTime;
    private final String secret;
    private Key key;

    public JwtUtilImpl(JwtPropertiesConfig jwtProperties) {
        this.jwtProperties = jwtProperties;
        expirationTime = jwtProperties.getExpiration();
        secret = jwtProperties.getSecret();
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Override
    public Claims getClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    @Override
    public LocalDateTime getExpirationDate(String token) {
        return getClaims(token).getExpiration()
                .toInstant()
                .atZone(ZoneId.of("EAT"))
                .toLocalDateTime();
    }

    @Override
    public Boolean isTokenExpired(String token) {
        return getExpirationDate(token).isBefore(LocalDateTime.now());
    }

    @Override
    public Boolean validate(String token) {
        return !isTokenExpired(token);
    }

    @Override
    public String generateToken(User user, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.getName());
        return generate(claims, user.getEmailAddress());
    }

    private String generate(Map<String, Object> claims, String username) {
        Long expiration = Long.parseLong(expirationTime); //in second
        final LocalDateTime createdDate = LocalDateTime.now(ZoneId.of("EAT"));
        final LocalDateTime expirationDate = createdDate.plusSeconds(expiration);
//                new Date(createdDate.getTime() + expiration * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(createdDate.atZone(ZoneId.of("EAT")).toInstant()))
                .setExpiration(Date.from(expirationDate.atZone(ZoneId.of("EAT")).toInstant()))
                .signWith(key)
                .compact();
    }
}
