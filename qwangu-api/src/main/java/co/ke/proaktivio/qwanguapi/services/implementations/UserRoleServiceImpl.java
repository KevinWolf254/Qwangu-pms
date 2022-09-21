package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
import co.ke.proaktivio.qwanguapi.repositories.UserAuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import co.ke.proaktivio.qwanguapi.services.UserAuthorityService;
import co.ke.proaktivio.qwanguapi.services.UserRoleService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRoleServiceImpl implements UserRoleService {
    private final ReactiveMongoTemplate template;
    private final RoleRepository roleRepository;
    private final UserAuthorityRepository userAuthorityRepository;
    private final UserAuthorityService userAuthorityService;

    @Override
    public Mono<UserRole> create(UserRoleDto dto) {
        String name = dto.getName();
        return this.exists(name)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Role %s already exists!".formatted(name))))
                .doOnSuccess(a -> log.debug(" Checks for Role {} was successful", name))
                .map($ -> new UserRole(name))
                .flatMap(template::save)
                .doOnSuccess(r -> log.debug(" {} was created on database successfully", r))
                .flatMap(userRole -> Flux.fromIterable(dto.getAuthorities())
                        .map(authority -> new UserAuthority.AuthorityBuilder()
                                .create(authority.getCreate())
                                .authorize(authority.getAuthorize())
                                .delete(authority.getDelete())
                                .update(authority.getUpdate())
                                .read(authority.getRead())
                                .name(authority.getName())
                                .roleId(userRole.getId())
                                .build())
                        .flatMap(userAuthorityRepository::save)
                        .doOnNext(a -> log.debug(" {} was created on database successfully", a))
                        .then()
                        .map($ -> userRole));
    }

    public Mono<Boolean> exists(String name) {
        return template.exists(new Query()
                .addCriteria(Criteria.where("name").is(name)), UserRole.class);
    }

    public Mono<UserRole> findByIdAndName(String id, String name) {
        return template.findOne(new Query()
                .addCriteria(Criteria.where("name").is(name).and("id").is(id)), UserRole.class);
    }

    @Override
    public Flux<UserRole> findPaginated(Optional<String> optionalApartmentName, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        optionalApartmentName.ifPresent(s -> query.addCriteria(Criteria.where("name").is(s)));
        query.with(pageable)
                .with(sort);
        return template.find(query, UserRole.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Roles were not found!")));
    }

    @Override
    public Mono<UserRole> findById(String roleId) {
        Query query = new Query().addCriteria(Criteria.where("id").is(roleId));
        return template.findOne(query, UserRole.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role with id %S was not found!"
                        .formatted(roleId))));
    }

    @Override
    public Mono<UserRole> update(String id, UserRoleDto dto) {
        String name = dto.getName();
        return findByIdAndName(id, name)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role with id %s and name %s does not exist!"
                        .formatted(id, name))))
                .map(role -> {
                    role.setName(name);
                    return role;
                })
                .doOnSuccess(a -> log.debug(" Checks for role {} was successful", name))
                .flatMap(roleRepository::save)
                .doOnSuccess(a -> log.debug(" Role {} was updated on database successfully", name))
                .flatMap(userRole -> Flux.fromIterable(dto.getAuthorities())
                        .flatMap(userAuthorityDto -> userAuthorityService.findByIdAndName(userAuthorityDto.getId(),
                                        userAuthorityDto.getName())
                                .switchIfEmpty(Mono.error(new CustomNotFoundException("User authority with id %s and name %s does not exist!"
                                        .formatted(userAuthorityDto.getId(), userAuthorityDto.getName()))))
                                .map(authority -> {
                                    authority.setCreate(userAuthorityDto.getCreate());
                                    authority.setUpdate(userAuthorityDto.getUpdate());
                                    authority.setAuthorize(userAuthorityDto.getAuthorize());
                                    authority.setDelete(userAuthorityDto.getDelete());
                                    authority.setRead(userAuthorityDto.getRead());
                                    return authority;
                                }))
                        .flatMap(userAuthorityRepository::save)
                        .doOnNext(a -> log.debug(" {} was created on database successfully", a))
                        .then()
                        .map($ -> userRole));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, UserRole.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
