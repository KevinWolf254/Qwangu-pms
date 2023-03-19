package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.UserToken;
import co.ke.proaktivio.qwanguapi.pojos.UserTokenDto;
import co.ke.proaktivio.qwanguapi.repositories.UserTokenRepository;
import co.ke.proaktivio.qwanguapi.services.UserTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserTokenServiceImpl implements UserTokenService {
    private final ReactiveMongoTemplate template;
    private final UserTokenRepository userTokenRepository;

    @Override
    public Mono<UserToken> create(UserTokenDto dto) {
        String emailAddress = dto.getEmailAddress();
		return userTokenRepository.findByEmailAddress(emailAddress)
                .switchIfEmpty(Mono.just(new UserToken.UserTokenBuilder().emailAddress(emailAddress).build()))
                .doOnSuccess(userToken -> userToken.setToken(dto.getToken()))
                .flatMap(userTokenRepository::save)
                .doOnSuccess(ut -> log.info("Created: {}", ut));
    }

    @Override
    public Mono<Boolean> exists(String username, String token) {
        return template.exists(new Query()
                .addCriteria(Criteria
                        .where("emailAddress").is(username)
                        .and("token").is(token)), UserToken.class);
    }
}
