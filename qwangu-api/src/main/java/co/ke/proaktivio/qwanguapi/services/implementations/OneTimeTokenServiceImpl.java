package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.repositories.OneTimeTokenRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.services.OneTimeTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OneTimeTokenServiceImpl implements OneTimeTokenService {
    private final OneTimeTokenRepository oneTimeTokenRepository;
    private final UserRepository userRepository;
    private static final Integer TOKEN_EXPIRATION_HOURS = 12;

    @Override
    public Mono<OneTimeToken> create(String userId, String uuid) {
        return Mono.just(userId)
                .flatMap(id -> userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("User with id %s could not be found!".formatted(userId))))
                .map(user -> {
                    LocalDateTime now = LocalDateTime.now();
                    return new OneTimeToken(null, uuid, now, now.plusHours(TOKEN_EXPIRATION_HOURS), userId);
                })
                .flatMap(oneTimeTokenRepository::save);
    }

    @Override
    public Mono<OneTimeToken> find(String token, String userId) {
        OneTimeToken oneTimeToken = new OneTimeToken(null, token, null, null, userId);
        return oneTimeTokenRepository
                .findOne(Example.of(oneTimeToken))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Token could not be found!")));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.just(id)
                .flatMap(oneTimeTokenRepository::findById)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Token could not be found!")))
                .flatMap(t -> oneTimeTokenRepository.deleteById(t.getId()));
    }
}
