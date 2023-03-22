package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.SmsNotification;
import reactor.core.publisher.Mono;

public interface SmsService {
	
    Mono<Boolean> send(SmsNotification email);
}
