package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface UserService {
    Mono<User> create(UserDto dto);

    Mono<User> createAndNotify(UserDto dto);

    Mono<User> activate(Optional<String> tokenOpt, Optional<String> userIdOpt);

    Mono<TokenDto> signIn(SignInDto signInDto);

    Mono<User> changePassword(String userId, PasswordDto dto);

    Mono<User> resetPassword(String id, String token, String password);

    Mono<User> update(String id, UserDto dto);

    Flux<User> findPaginated(Optional<String> id, Optional<String> emailAddress, int page, int pageSize, OrderType order);

    Mono<Boolean> deleteById(String id);

    Mono<Void> sendResetPassword(EmailDto dto);

    Mono<OneTimeToken> findToken(Optional<String> token, Optional<String> id);
}
