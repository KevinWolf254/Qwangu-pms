package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import co.ke.proaktivio.qwanguapi.services.EmailGenerator;
import co.ke.proaktivio.qwanguapi.services.EmailService;
import co.ke.proaktivio.qwanguapi.services.OneTimeTokenService;
import co.ke.proaktivio.qwanguapi.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
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
    private final PasswordEncoder encoder;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<User> create(UserDto dto) {
        return userRepository.create(dto);
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
    public Mono<User> activate(Optional<String> tokenOpt, Optional<String> userIdOpt) {
        return oneTimeTokenService
                .find(tokenOpt, userIdOpt)
                .filter(token -> token.getExpiration().isBefore(LocalDateTime.now()))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token has expired! Contact administrator.")))
                .flatMap(ott -> userRepository.findById(userIdOpt.get()))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("User could not be found!")))
                .flatMap(user -> {
                    user.setModified(LocalDateTime.now());
                    user.setEnabled(true);
                    return userRepository.save(user);
                });
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
                                        .map(role -> jwtUtil.generateToken(user, role))
                                        .filter(StringUtils::hasText)
                                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                                        .map(TokenDto::new)
                                )
                        )
                );
    }

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
    public Mono<User> resetPassword(String id, String token, String password) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User with id %s could not bbe found!".formatted(id))))
                .filter(User::getEnabled)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("User is disabled!")))
                .flatMap(user -> oneTimeTokenService.find(Optional.of(token), Optional.of(user.getId()))
                        .filter(tokenDto -> tokenDto.getExpiration().isBefore(LocalDateTime.now()))
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("Token has expired! Contact administrator.")))
                        .map(tokenDto -> {
                            user.setPassword(encoder.encode(password));
                            return user;
                        }))
                .flatMap(userRepository::save);
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

    @Override
    public Mono<OneTimeToken> findToken(Optional<String> tokenOpt, Optional<String> idOpt) {
        return oneTimeTokenService.find(tokenOpt, idOpt);
    }
}
