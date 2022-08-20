package co.ke.proaktivio.qwanguapi.security.jwt;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;

import java.util.List;

public interface JwtGeneratorUtil {
    String generateToken(User user, Role role, List<Authority> authorities);
}
