package co.ke.proaktivio.qwanguapi.services.implementations;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.AuthorityRepository;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AuthorityServiceImpl implements AuthorityService {
	private final AuthorityRepository authorityRepository;
	private final ReactiveMongoTemplate template;

	@Override
	public Mono<Authority> findById(String authorityId) {
		return authorityRepository.findById(authorityId);
	}

	@Override
	public Flux<Authority> findAll(String name, OrderType order) {
		Query query = new Query();
		if (StringUtils.hasText(name))
			query.addCriteria(Criteria.where("name").is(name.trim()));

		Sort sort = order != null
				? order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) : Sort.by(Sort.Order.desc("id"))
				: Sort.by(Sort.Order.desc("id"));
        query.with(sort);
        return template.find(query, Authority.class);
	}

}
