package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Email;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.services.EmailGenerator;
import co.ke.proaktivio.qwanguapi.services.EmailService;
import co.ke.proaktivio.qwanguapi.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailGenerator emailGenerator;
    @Override
    public Mono<User> create(UserDto dto) {
        return userRepository.create(dto);
    }

    @Override
    @Transactional
    public Mono<User> createAndNotify(UserDto dto) {
        return create(dto)
                .flatMap(user -> {
                    Email email = emailGenerator.generateAccountActivationEmail(user);
                    return emailService
                            .send(email)
                            .map(success -> user);
                });
    }

    @Override
    public Mono<User> update(String id, UserDto dto) {
        return userRepository.update(id, dto);
    }

    @Override
    public Flux<User> findPaginated(Optional<String> id, Optional<String> emailAddress, int page, int pageSize, OrderType order) {
        return userRepository.findPaginated(id, emailAddress, page, pageSize, order);
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return userRepository.delete(id);
    }
}
