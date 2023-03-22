package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import reactor.core.publisher.Mono;

public interface EmailService {

    Mono<Boolean> send(EmailNotification email);
}
