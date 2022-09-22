package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface UserService {
    Mono<User> create(UserDto dto);
    Mono<User> createAndNotify(UserDto dto);
    Mono<User> activate(String token, String userId);
    Mono<TokenDto> signIn(SignInDto signInDto);
    Mono<User> changePassword(String userId, PasswordDto dto);
    Mono<User> resetPassword(String token, String password);
    Mono<User> update(String id, UserDto dto);
    Mono<User> findById(String roleId);
    Flux<User> findPaginated(Optional<String> id, Optional<String> emailAddress, int page, int pageSize, OrderType order);
    Mono<Boolean> deleteById(String id);
    Mono<Void> sendForgotPasswordEmail(EmailDto dto);
}
