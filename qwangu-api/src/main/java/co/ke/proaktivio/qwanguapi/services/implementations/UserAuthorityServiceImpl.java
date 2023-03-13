package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.repositories.UserAuthorityRepository;
import co.ke.proaktivio.qwanguapi.services.UserAuthorityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

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
        Query query = new Query().addCriteria(Criteria.where("id").is(authorityId));
        return template.findOne(query, UserAuthority.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Authority with id %S was not found!"
                        .formatted(authorityId))));
    }

    @Override
    public Mono<UserAuthority> findByIdAndName(String id, String name) {
        return template.findOne(new Query()
                .addCriteria(Criteria.where("name").is(name).and("id").is(id)), UserAuthority.class);
    }

    @Override
    public Mono<UserAuthority> update(String id, UserAuthorityDto dto) {
        return userAuthorityRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Authority with id %s does not exists!"
                        .formatted(id))))
                .map(authority -> {
                    authority.setCreate(dto.getCreate());
                    authority.setRead(dto.getRead());
                    authority.setUpdate(dto.getUpdate());
                    authority.setDelete(dto.getDelete());
                    authority.setAuthorize(dto.getAuthorize());
                    return authority;
                })
                .doOnSuccess(a -> log.debug(" Checks for authority {} was successful", a.getName()))
                .flatMap(userAuthorityRepository::save)
                .doOnSuccess(a -> log.debug(" Authority {} was updated on database successfully", a.getName()));
    }

    @Override
    public Flux<UserAuthority> findPaginated(Optional<String> optionalApartmentName, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        optionalApartmentName.ifPresent(s -> query.addCriteria(Criteria.where("name").is(s)));
        query.with(pageable)
                .with(sort);
        return template.find(query, UserAuthority.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Authorities were not found!")));
    }
}
