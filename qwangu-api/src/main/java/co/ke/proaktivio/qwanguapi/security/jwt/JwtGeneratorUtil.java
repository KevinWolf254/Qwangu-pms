package co.ke.proaktivio.qwanguapi.security.jwt;

import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.models.User;

import java.util.List;

public interface JwtGeneratorUtil {
    String generateToken(User user, UserRole role, List<UserAuthority> authorities);
}
