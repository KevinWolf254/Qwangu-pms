package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface UserAuthorityService {
    Flux<UserAuthority> findPaginated(Optional<String> name, int page, int pageSize, OrderType order);
    Flux<UserAuthority> findByRoleId(String roleId);
    Mono<UserAuthority> findById(String roleId);

    Mono<UserAuthority> findByIdAndName(String id, String name);

    Mono<UserAuthority> update(String id, UserAuthorityDto dto);
}