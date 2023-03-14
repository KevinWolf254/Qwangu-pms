package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.repositories.UserAuthorityRepository;
import co.ke.proaktivio.qwanguapi.services.UserAuthorityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.mongodb.client.result.DeleteResult;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserAuthorityServiceImpl implements UserAuthorityService {
    private final ReactiveMongoTemplate template;
    private final UserAuthorityRepository userAuthorityRepository;

    @Override
    public Flux<UserAuthority> findByRoleId(String roleId) {
        return template.find(new Query()
                .addCriteria(Criteria.where("roleId").is(roleId)), UserAuthority.class);
    }

    @Override
    public Mono<UserAuthority> findById(String authorityId) {
    	return userAuthorityRepository.findById(authorityId);
    }

    @Override
    public Mono<UserAuthority> findByIdAndName(String id, String name) {
        return template.findOne(new Query()
                .addCriteria(Criteria.where("name").is(name).and("id").is(id)), UserAuthority.class);
    }

    @Override
    public Mono<UserAuthority> update(String id, UserAuthorityDto dto) {
        return findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("UserAuthority with id %s does not exists!"
                        .formatted(id))))
                .map(authority -> {
                    authority.setCreate(dto.getCreate());
                    authority.setRead(dto.getRead());
                    authority.setUpdate(dto.getUpdate());
                    authority.setDelete(dto.getDelete());
                    authority.setAuthorize(dto.getAuthorize());
                    return authority;
                })
                .flatMap(userAuthorityRepository::save)
                .doOnSuccess(a -> log.info("Updated: {}", a));
    }

    @Override
    public Flux<UserAuthority> findAll(String name, String userRoleId, OrderType order) {
        Query query = new Query();
        if(StringUtils.hasText(name))
        	 query.addCriteria(Criteria.where("name").is(name.trim()));
        if(StringUtils.hasText(userRoleId))
       	 query.addCriteria(Criteria.where("roleId").is(userRoleId.trim()));
        	 
        Sort sort = order != null ? order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id")) :
            	Sort.by(Sort.Order.desc("id")) ;
        query.with(sort);
        return template.find(query, UserAuthority.class);
    }

	@Override
	public Mono<Boolean> deleteById(String id) {
		return findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("UserAuthority with id %s does not exists!"
                        .formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged)
                .doOnSuccess($ -> log.info("Delete UserAuthority with id {} successfully.", id));
   	}
}
