package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.OneTimeTokenRepository;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import co.ke.proaktivio.qwanguapi.services.*;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailGenerator emailGenerator;
    private final OneTimeTokenService oneTimeTokenService;
    private final OneTimeTokenRepository oneTimeTokenRepository;
    private final PasswordEncoder encoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final AuthorityService authorityService;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<User> create(UserDto dto) {
        String emailAddress = dto.getEmailAddress();
        return roleRepository.findById(dto.getRoleId())
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role with id %s does not exist!"
                        .formatted(dto.getRoleId()))))
                .flatMap(role -> findByEmailAddress(emailAddress))
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("User with email address %s already exists!"
                        .formatted(emailAddress))))
                .map($ -> {
                    LocalDateTime now = LocalDateTime.now();
                    return new User( null, dto.getPerson(), emailAddress, dto.getRoleId(), null,
                            false, false, false, false, now, null);
                })
                .flatMap(userRepository::save);
    }

    public Mono<Boolean> findByEmailAddress(String emailAddress) {
        return template.exists(new Query()
                .addCriteria(Criteria.where("emailAddress").is(emailAddress)), User.class);
    }

    @Override
    @Transactional
    public Mono<User> createAndNotify(UserDto dto) {
        return create(dto)
                .flatMap(user -> {
                            String token = UUID.randomUUID().toString();
                            Email email = emailGenerator.generateAccountActivationEmail(user, token);
                            return oneTimeTokenService.create(user.getId(), token)
                                    .flatMap(tokenDto -> emailService.send(email))
                                    .map(success -> user);
                        }
                );
    }

    @Override
    public Mono<User> activate(String token, String userId) {
        return oneTimeTokenService.find(token, userId)
                .filter(t -> t.getExpiration().isAfter(LocalDateTime.now()) || t.getExpiration().isEqual(LocalDateTime.now()))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token has expired! Contact administrator.")))
                .flatMap(ott -> userRepository.findById(userId)
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("User could not be found!")))
                        .flatMap(user -> {
                            user.setModified(LocalDateTime.now());
                            user.setEnabled(true);
                            return userRepository.save(user);
                        })
                        .map(user -> {
                            oneTimeTokenRepository.deleteById(ott.getId());
                            return user;
                        })
                );
    }

    @Override
    public Mono<TokenDto> signIn(SignInDto signInDto) {
        return Mono.just(signInDto)
                .flatMap(dto -> userRepository.findOne(Example.of(new User(dto.getUsername())))
                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                        .flatMap(user -> Mono
                                .just(encoder.matches(dto.getPassword(), user.getPassword()))
                                .filter(passwordsMatch -> passwordsMatch)
                                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                                .flatMap(match -> roleRepository
                                        .findById(user.getRoleId())
                                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                                        .flatMap(role -> authorityService.findByRoleId(role.getId())
                                                .collectList()
                                                .map(authorities -> jwtUtil.generateToken(user, role, authorities)))
                                        .filter(StringUtils::hasText)
                                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                                        .map(TokenDto::new)
                                )
                        )
                );
    }

//    @Override
//    public Mono<User> update(String id, UserDto dto) {
//        return userRepository.update(id, dto);
//    }

    @Override
    public Mono<User> update(String id, UserDto dto) {
        String emailAddress = dto.getEmailAddress();
        String roleId = dto.getRoleId();

        Query query = new Query()
                .addCriteria(Criteria.where("emailAddress").is(emailAddress).and("id").is(id));

        return template.findById(roleId, Role.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role with id %s does not exist!".formatted(roleId))))
                .flatMap(role -> template.findOne(query, User.class))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("User with id %s and email address %s does not exist!"
                        .formatted(id, emailAddress))))
                .flatMap(user -> {
                    user.setPerson(dto.getPerson());
                    user.setRoleId(roleId);
                    user.setModified(LocalDateTime.now());
                    return template.save(user, "USER");
                });
    }

//    @Override
//    public Flux<User> findPaginated(Optional<String> id, Optional<String> emailAddress, int page, int pageSize, OrderType order) {
//        return userRepository.findPaginated(id, emailAddress, page, pageSize, order);
//    }

    @Override
    public Flux<User> findPaginated(Optional<String> id, Optional<String> emailAddress, int page, int pageSize,
                                    OrderType order) {
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

//    @Override
//    public Mono<Boolean> deleteById(String id) {
//        return userRepository.delete(id);
//    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, User.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("User with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }

    // TODO - CREATE PASSWORD_SERVICE
    @Override
    public Mono<User> changePassword(String userId, PasswordDto dto) {
        return userRepository
                .findById(userId)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with id %s does not exist!".formatted(userId))))
                .flatMap(user -> Mono.just(encoder.matches(dto.getCurrentPassword(), user.getPassword()))
                        .filter(passwordMatch -> passwordMatch)
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("Passwords do not match!")))
                        .map(match -> {
                            user.setPassword(encoder.encode(dto.getNewPassword()));
                            return user;
                        })
                        .flatMap(userRepository::save));
    }

    @Override
    public Mono<User> resetPassword(String token, String password) {
        return oneTimeTokenRepository.findOne(Example.of(new OneTimeToken(token)))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Token could not be found!")))
                .filter(t -> t.getExpiration().isAfter(LocalDateTime.now()) || t.getExpiration().isEqual(LocalDateTime.now()))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token has expired! Contact administrator.")))
                .flatMap(ott -> userRepository.findById(ott.getUserId())
                        .switchIfEmpty(Mono.error(new CustomNotFoundException("User could not be found!")))
                        .filter(User::getEnabled)
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("User is disabled! Contact administrator.")))
                        .map(user -> {
                            user.setPassword(encoder.encode(password));
                            return user;
                        })
                        .flatMap(userRepository::save)
                        .map(user -> {
                            oneTimeTokenRepository.deleteById(ott.getId());
                            return user;
                        })
                );
    }

    @Override
    @Transactional
    public Mono<Void> sendResetPassword(EmailDto dto) {
        return userRepository.findOne(Example.of(new User(dto.getEmailAddress())))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Email address %s could not be found!".formatted(dto.getEmailAddress()))))
                .flatMap(user -> {
                    String token = UUID.randomUUID().toString();
                    Email email = emailGenerator.generatePasswordForgottenEmail(user, token);
                    return oneTimeTokenService.create(user.getId(), token)
                            .flatMap(tokenDto -> emailService.send(email))
                            .map(success -> email);
                })
                .flatMap(r -> Mono.empty());
    }
}
