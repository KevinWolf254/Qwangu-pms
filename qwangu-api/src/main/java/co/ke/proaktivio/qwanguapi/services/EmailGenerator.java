package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Email;

public interface EmailGenerator {
    Email generateAccountActivationEmail(User user, String token);
    Email generatePasswordForgottenEmail(User user, String token);
}
