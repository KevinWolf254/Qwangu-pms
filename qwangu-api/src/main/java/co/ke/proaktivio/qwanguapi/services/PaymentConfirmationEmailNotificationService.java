package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import reactor.core.publisher.Mono;

public interface PaymentConfirmationEmailNotificationService {

	public Mono<EmailNotification> create(Tenant tenant);
}
