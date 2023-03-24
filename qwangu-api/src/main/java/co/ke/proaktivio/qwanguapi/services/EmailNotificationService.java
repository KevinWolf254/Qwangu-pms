package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmailNotificationService {
	Mono<EmailNotification> update(String emailNotificationId, EmailNotification emailNotification);
	
	Flux<EmailNotification> findAll(NotificationStatus status, String phoneNumber, OrderType order);
}
