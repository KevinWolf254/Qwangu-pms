package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface UserService {
    Mono<User> create(UserDto dto);
    Mono<User> createAndNotify(UserDto dto);
    Mono<User> activate(Optional<String> tokenOpt, Optional<String> userIdOpt);
    Mono<User> update(String id, UserDto dto);
    Flux<User> findPaginated(Optional<String> id, Optional<String> emailAddress, int page, int pageSize, OrderType order);
    Mono<Boolean> deleteById(String id);
}
