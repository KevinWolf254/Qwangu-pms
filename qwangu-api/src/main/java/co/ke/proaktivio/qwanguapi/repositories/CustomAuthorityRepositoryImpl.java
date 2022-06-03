package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;

import java.util.Optional;

@RequiredArgsConstructor
public class CustomAuthorityRepositoryImpl implements CustomAuthorityRepository {
    private final ReactiveMongoTemplate template;

    @Override
    public Flux<Authority> findPaginated(Optional<String> optionalId, Optional<String> optionalApartmentName, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        optionalId.ifPresent(s -> query.addCriteria(Criteria.where("id").is(s)));
        optionalApartmentName.ifPresent(s -> query.addCriteria(Criteria.where("name").is(s)));
        query.with(pageable)
                .with(sort);
        return template.find(query, Authority.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Authorities were not found!")));
    }
}
