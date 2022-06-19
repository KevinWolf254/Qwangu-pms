package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.pojos.Email;
import reactor.core.publisher.Mono;

public interface EmailService {

    Mono<Boolean> send(Email email);
}
