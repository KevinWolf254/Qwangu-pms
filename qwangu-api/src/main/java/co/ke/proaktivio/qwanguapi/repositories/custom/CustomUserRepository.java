package co.ke.proaktivio.qwanguapi.repositories.custom;

import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CustomUserRepository {
    Mono<User> create(UserDto dto);
    Mono<User> update(String id, UserDto dto);
    Flux<User> findPaginated(Optional<String> id, Optional<String> emailAddress, int page, int pageSize, OrderType order);
    Mono<Boolean> delete(String id);
}
