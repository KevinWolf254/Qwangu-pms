package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRoleService {
	Mono<UserRole> create(UserRoleDto dto);

	Mono<UserRole> findById(String roleId);

	Flux<UserRole> findAll(String name, OrderType order);

//	Mono<UserRole> update(String id, UserRoleDto dto);
}
