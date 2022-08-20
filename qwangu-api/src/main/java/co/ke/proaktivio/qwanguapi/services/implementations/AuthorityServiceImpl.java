package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.AuthorityRepository;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorityServiceImpl implements AuthorityService {
    private final AuthorityRepository authorityRepository;
    private final ReactiveMongoTemplate template;
    @Override
    public Flux<Authority> findPaginated(Optional<String> id, Optional<String> name, int page, int pageSize,
                                         OrderType order) {
        return authorityRepository.findPaginated(id, name, page, pageSize, order);
    }

    @Override
    public Flux<Authority> findByRoleId(String roleId) {
        return template.find(new Query()
                .addCriteria(Criteria.where("roleId").is(roleId)), Authority.class);
    }
}
