package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
import co.ke.proaktivio.qwanguapi.repositories.UserAuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRoleRepository;
import co.ke.proaktivio.qwanguapi.services.UserRoleService;
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
public class UserRoleServiceImpl implements UserRoleService {
    private final ReactiveMongoTemplate template;
    private final UserRoleRepository userRoleRepository;
    private final UserAuthorityRepository userAuthorityRepository;

    @Override
    public Mono<UserRole> create(UserRoleDto dto) {
        String name = dto.getName();
        return this.exists(name)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Role with name %s already exists!".formatted(name))))
                .doOnSuccess(a -> log.debug("Checks for Role {} was successful", name))
                .map($ -> new UserRole.UserRoleBuilder().name(name).build())
                .flatMap(userRoleRepository::save)
                .doOnSuccess(r -> log.info("Created: {}", r))
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
                        .doOnNext(a -> log.info("Created: {}", a))
                        .then(Mono.just(userRole)));
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
    public Flux<UserRole> findAll(String name, OrderType order) {
        Query query = new Query();
        if(StringUtils.hasText(name)) {
        	query.addCriteria(Criteria.where("name").is(name.trim()));
        }

        Sort sort = order != null ? order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id")):
                    Sort.by(Sort.Order.desc("id"));
        query.with(sort);
        return template.find(query, UserRole.class);
    }

    @Override
    public Mono<UserRole> findById(String roleId) {
    	return userRoleRepository.findById(roleId);
    }

//    @Override
//    public Mono<UserRole> update(String id, UserRoleDto dto) {
//        String name = dto.getName();
//        return findByIdAndName(id, name)
//                .filter(Objects::nonNull)
//                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role with id %s and name %s does not exist!"
//                        .formatted(id, name))))
//                .map(role -> {
//                    role.setName(name);
//                    return role;
//                })
//                .doOnSuccess(a -> log.debug("Checks for role {} was successful", name))
//                .flatMap(userRoleRepository::save)
//                .doOnSuccess(a -> log.debug("Role {} was updated on database successfully", name))
//                .flatMap(userRole -> Flux.fromIterable(dto.getAuthorities())
//                        .flatMap(userAuthorityDto -> userAuthorityService.findByIdAndName(userAuthorityDto.getId(),
//                                        userAuthorityDto.getName())
//                                .switchIfEmpty(Mono.error(new CustomNotFoundException("User authority with id %s and name %s does not exist!"
//                                        .formatted(userAuthorityDto.getId(), userAuthorityDto.getName()))))
//                                .map(authority -> {
//                                    authority.setCreate(userAuthorityDto.getCreate());
//                                    authority.setUpdate(userAuthorityDto.getUpdate());
//                                    authority.setAuthorize(userAuthorityDto.getAuthorize());
//                                    authority.setDelete(userAuthorityDto.getDelete());
//                                    authority.setRead(userAuthorityDto.getRead());
//                                    return authority;
//                                }))
//                        .flatMap(userAuthorityRepository::save)
//                        .doOnNext(a -> log.info("Updated : {}", a))
//                        .then(Mono.just(userRole)));
//    }
}
