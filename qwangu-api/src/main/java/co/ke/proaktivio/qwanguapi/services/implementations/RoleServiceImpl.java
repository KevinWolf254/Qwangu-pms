package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.AuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import co.ke.proaktivio.qwanguapi.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final ReactiveMongoTemplate template;

    @Override
    public Flux<Role> findPaginated(Optional<String> optionalId, Optional<String> optionalApartmentName, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        optionalId.ifPresent(s -> query.addCriteria(Criteria.where("id").is(s)));
        optionalApartmentName.ifPresent(s -> query.addCriteria(Criteria.where("name").is(s)));
        query.with(pageable)
                .with(sort);
        return template.find(query, Role.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Roles were not found!")));
    }
}
