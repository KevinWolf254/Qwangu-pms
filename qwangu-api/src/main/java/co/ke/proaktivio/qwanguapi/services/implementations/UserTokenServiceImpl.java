package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.UserToken;
import co.ke.proaktivio.qwanguapi.pojos.UserTokenDto;
import co.ke.proaktivio.qwanguapi.repositories.UserTokenRepository;
import co.ke.proaktivio.qwanguapi.services.UserTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserTokenServiceImpl implements UserTokenService {
    private final ReactiveMongoTemplate template;
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<UserToken> create(UserTokenDto dto) {
        LocalDateTime now = LocalDateTime.now();
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("emailAddress").is(dto.getEmailAddress())), UserToken.class)
                .switchIfEmpty(Mono.just(new UserToken(null, dto.getEmailAddress(), null, now, null)))
                .map(userToken -> {
                    userToken.setToken(dto.getToken());
                    userToken.setLastSignIn(now);
                    return userToken;
                })
                .flatMap(userTokenRepository::save);
    }

    @Override
    public Mono<Boolean> exists(String username, String token) {
        return template.exists(new Query()
                .addCriteria(Criteria
                        .where("emailAddress").is(username)
                        .and("token").is(token)), UserToken.class);
    }
}
