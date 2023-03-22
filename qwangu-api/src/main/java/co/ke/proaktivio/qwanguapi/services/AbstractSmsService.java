package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.SmsNotification;
import reactor.core.publisher.Mono;

public abstract interface AbstractSmsService {
	abstract Mono<Boolean> send(SmsNotification smsNotification);
}
