package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OneTimeTokenService {
    Mono<OneTimeToken> create(String userId);

	Mono<OneTimeToken> findByToken(String token);

	Flux<OneTimeToken> findAll(String token, String userId, OrderType order);

    Mono<Void> deleteById(String id);
}
