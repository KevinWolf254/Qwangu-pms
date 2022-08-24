package co.ke.proaktivio.qwanguapi.security.jwt.implementations;

import co.ke.proaktivio.qwanguapi.configs.properties.JwtPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
public class JwtUtilImpl implements JwtUtil {
    private final JwtPropertiesConfig properties;
    private final Key key;

    private final static String EAT = "Africa/Nairobi";
    private final static ZoneId NAIROBI = ZoneId.of(EAT);

    public JwtUtilImpl(JwtPropertiesConfig properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(properties.getSecret().getBytes());
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
                .atZone(NAIROBI)
                .toLocalDateTime();
    }

    @Override
    public Boolean isTokenExpired(String token) {
        return getExpirationDate(token).isBefore(LocalDateTime.now());
    }

    @Override
    public Boolean isValid(String token) {
        return !isTokenExpired(token);
    }

    @Override
    public String generateToken(User user, Role role, List<Authority> authorities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", generateAuthorities(role, authorities));
        return generate(claims, user.getEmailAddress());
    }

    private Set<String> generateAuthorities(Role role, List<Authority> authorities) {
        Set<String> userAuthorities = new HashSet<>();
        userAuthorities.add("ROLE_" + role.getName());
        authorities.forEach(authority -> {
            String name = authority.getName();
            if(authority.getCreate()) {
                userAuthorities.add(name.toUpperCase().concat("_CREATE"));
            }
            if(authority.getRead()) {
                userAuthorities.add(name.toUpperCase().concat("_READ"));
            }
            if(authority.getUpdate()) {
                userAuthorities.add(name.toUpperCase().concat("_UPDATE"));
            }
            if(authority.getAuthorize()) {
                userAuthorities.add(name.toUpperCase().concat("_AUTHORIZE"));
            }
        });
        return userAuthorities;
    }

    private String generate(Map<String, Object> claims, String username) {
        final LocalDateTime createdDate = LocalDateTime.now(NAIROBI);
        final LocalDateTime expirationDate = createdDate.plusSeconds(properties.getExpiration());

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(Date.from(createdDate.atZone(NAIROBI).toInstant()))
                .setExpiration(Date.from(expirationDate.atZone(NAIROBI).toInstant()))
                .signWith(key)
                .compact();
    }
}
