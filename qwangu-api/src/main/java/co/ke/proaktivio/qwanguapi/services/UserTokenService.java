package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.UserToken;
import co.ke.proaktivio.qwanguapi.pojos.UserTokenDto;
import reactor.core.publisher.Mono;

public interface UserTokenService {
    Mono<UserToken> create(UserTokenDto dto);
    Mono<Boolean> exists(String username, String token);
}
