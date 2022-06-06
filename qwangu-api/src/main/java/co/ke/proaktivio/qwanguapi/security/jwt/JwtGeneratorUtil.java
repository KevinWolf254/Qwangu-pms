package co.ke.proaktivio.qwanguapi.security.jwt;

import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;

public interface JwtGeneratorUtil {

    String generateToken(User user, Role role);
}
