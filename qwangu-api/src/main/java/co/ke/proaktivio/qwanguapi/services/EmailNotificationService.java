package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.SmsNotification;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.SmsNotificationDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface EmailNotificationService {
	Mono<SmsNotification> create(SmsNotificationDto dto);
	
	Flux<SmsNotification> findAll(String phoneNumber, OrderType order);
}
