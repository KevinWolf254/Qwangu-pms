package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.User;
import reactor.core.publisher.Mono;

public interface AccountActivationEmailNotificationService {

	public Mono<EmailNotification> create(User user, String token);
}
