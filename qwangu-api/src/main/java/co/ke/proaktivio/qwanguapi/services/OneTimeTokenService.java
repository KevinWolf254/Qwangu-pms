package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import reactor.core.publisher.Mono;

public interface OneTimeTokenService {
    Mono<OneTimeToken> create(String userId, String token);

	Mono<OneTimeToken> findByToken(String token);

	Mono<OneTimeToken> findAll(String token, String userId);

    Mono<Void> deleteById(String id);
}
