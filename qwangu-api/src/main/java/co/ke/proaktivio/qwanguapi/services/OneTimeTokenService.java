package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface OneTimeTokenService {
    Mono<OneTimeToken> create(String userId, String token);
    Mono<OneTimeToken> find(Optional<String> token, Optional<String> userId);
    Mono<Void> deleteById(String id);
}
