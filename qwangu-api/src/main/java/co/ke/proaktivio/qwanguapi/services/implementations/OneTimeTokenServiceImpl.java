package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.OneTimeTokenRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.services.OneTimeTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class OneTimeTokenServiceImpl implements OneTimeTokenService {
    private final OneTimeTokenRepository oneTimeTokenRepository;
    private final UserRepository userRepository;
    private final ReactiveMongoTemplate template;
    
    @Override
    public Mono<OneTimeToken> create(String userId) {
        return Mono.just(userId)
                .flatMap(id -> userRepository.findById(userId))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("User with id %s could not be found!".formatted(userId))))
                .map(user -> new OneTimeToken.OneTimeTokenBuilder().userId(userId).build())
                .flatMap(oneTimeTokenRepository::save)
                .doOnSuccess(a -> log.info("Created: {}", a));
    }
    
    @Override
    public Mono<OneTimeToken> findByToken(String token) {
    	return oneTimeTokenRepository.findByToken(token);
    }
    
    @Override
    public Flux<OneTimeToken> findAll(String token, String userId, OrderType order) {
        Query query = new Query();
        if(StringUtils.hasText(token))
        	query.addCriteria(Criteria.where("token").is(token.trim()));
        if(StringUtils.hasText(userId))
        	query.addCriteria(Criteria.where("userId").is(userId.trim()));

        Sort sort = order != null ? order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id")) :
                    Sort.by(Sort.Order.desc("id"));
        query.with(sort);
        return template.find(query, OneTimeToken.class);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return Mono.just(id)
                .flatMap(oneTimeTokenRepository::findById)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Token could not be found!")))
                .flatMap(t -> oneTimeTokenRepository.deleteById(t.getId()));
    }
}
