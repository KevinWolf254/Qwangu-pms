package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomUserRepositoryImpl implements CustomUserRepository {
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<User> create(UserDto dto) {
        String emailAddress = dto.getEmailAddress();
        Query query = new Query()
                .addCriteria(Criteria.where("emailAddress").is(emailAddress));

        return template.findById(dto.getRoleId(), Role.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role with id %s does not exist!".formatted(dto.getRoleId()))))
                .flatMap(role -> template.exists(query, User.class))
                .flatMap(exists -> {
                    if (exists)
                        throw new CustomAlreadyExistsException("User with email address %s already exists!".formatted(emailAddress));
                    LocalDateTime now = LocalDateTime.now();
                    return template.save(new User( null, dto.getPerson(), emailAddress, dto.getRoleId(), null, false, false, false, false, now, null));
                });
    }

    @Override
    public Mono<User> update(String id, UserDto dto) {
        String emailAddress = dto.getEmailAddress();
        String roleId = dto.getRoleId();

        Query query = new Query()
                .addCriteria(Criteria.where("emailAddress").is(emailAddress).and("id").is(id));

        return template.findById(roleId, Role.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role with id %s does not exist!".formatted(roleId))))
                .flatMap(role -> template.findOne(query, User.class))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("User with id %s and email address %s does not exist!".formatted(id, emailAddress))))
                .flatMap(user -> {
                    user.setPerson(dto.getPerson());
                    user.setRoleId(roleId);
                    user.setModified(LocalDateTime.now());
                    return template.save(user, "USER");
                });
    }

    @Override
    public Flux<User> findPaginated(Optional<String> id, Optional<String> emailAddress, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        id.ifPresent(s -> query.addCriteria(Criteria.where("id").is(s)));
        emailAddress.ifPresent(s -> query.addCriteria(Criteria.where("emailAddress").is(s)));
        query.with(pageable)
                .with(sort);
        return template.find(query, User.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Users were not found!")));
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return template
                .findById(id, User.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("User with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
