package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface UserRoleService {
    Mono<UserRole> create(UserRoleDto dto);
    Mono<UserRole> findById(String roleId);
    Flux<UserRole> findPaginated(Optional<String> name, int page, int pageSize, OrderType order);
    Mono<UserRole> update(String id, UserRoleDto dto);
    Mono<Boolean> deleteById(String id);
}
