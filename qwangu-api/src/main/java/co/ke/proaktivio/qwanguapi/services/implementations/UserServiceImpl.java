package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.*;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.models.UserToken;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.UserRoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import co.ke.proaktivio.qwanguapi.services.*;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final OneTimeTokenService oneTimeTokenService;
    private final PasswordEncoder encoder;
    private final UserRoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final UserAuthorityService userAuthorityService;
    private final ReactiveMongoTemplate template;
    private final UserTokenService userTokenService;
    private final AccountActivationEmailNotificationService accountActivationEmailNotificationService;
    private final RequestPasswordResetEmailNotificationService requestPasswordResetEmailNotificationService;
    
    @Override
    public Mono<User> create(UserDto dto) {
        String emailAddress = dto.getEmailAddress();
        return roleRepository.findById(dto.getRoleId())
                .switchIfEmpty(Mono.error(new CustomNotFoundException("UserRole with id %s does not exist!"
                        .formatted(dto.getRoleId()))))
                .flatMap(role -> findByEmailAddress(emailAddress))
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("User with email address %s already exists!"
                        .formatted(emailAddress))))
                .map($ -> new User.UserBuilder()
                		.person(dto.getPerson())
                		.emailAddress(emailAddress)
                		.roleId(dto.getRoleId()).build())
                .flatMap(userRepository::save)
                .doOnSuccess(a -> log.info("Created: {}", a));
    }

    public Mono<Boolean> findByEmailAddress(String emailAddress) {
        return template.exists(new Query()
                .addCriteria(Criteria.where("emailAddress").is(emailAddress)), User.class);
    }

	@Override
	@Transactional
	public Mono<User> createAndNotify(UserDto dto) {
		return create(dto).flatMap(user -> notify(user).flatMap($ -> Mono.just(user)));
	}
	
	private Mono<EmailNotification> notify(User user) {
		return oneTimeTokenService.create(user.getId())
		.flatMap(ott -> accountActivationEmailNotificationService.create(user, ott.getToken()));
	}
	
    @Override
    public Mono<User> update(String id, UserDto dto) {
        String emailAddress = dto.getEmailAddress();
        String roleId = dto.getRoleId();

        Query query = new Query()
                .addCriteria(Criteria.where("emailAddress").is(emailAddress).and("id").is(id));

        return template.findById(roleId, UserRole.class)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Role with id %s does not exist!".formatted(roleId))))
                .flatMap(role -> template.findOne(query, User.class))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("User with id %s and email address %s does not exist!"
                        .formatted(id, emailAddress))))
                .map(user -> {
                    user.setPerson(dto.getPerson());
                    user.setRoleId(roleId);
                    user.setModifiedOn(LocalDateTime.now());
                    return user;
                })
                .flatMap(userRepository::save)
                .doOnSuccess(user -> log.info("Updated: {}", user));
    }

    @Override
    public Mono<User> findById(String userId) {
    	return userRepository.findById(userId);
    }

    @Override
    public Flux<User> findAll(String emailAddress, OrderType order) {
        Query query = new Query();
        if(StringUtils.hasText(emailAddress))
        	query.addCriteria(Criteria.where("emailAddress").is(emailAddress.trim()));

        Sort sort = order != null ? order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id")) :
                    Sort.by(Sort.Order.desc("id"));
        query.with(sort);
        return template.find(query, User.class);
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, User.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("User with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged)
                .doOnSuccess($ -> log.info("Deleted user with id %s: ".formatted(id)));
    }

    @Override
    public Mono<User> activate(String token) {
        return oneTimeTokenService.findByToken(token)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token does not exist!")))
                .filter(t -> !t.hasExpired())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token has expired! Contact administrator.")))
                .flatMap(ott -> userRepository.findById(ott.getUserId())
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("User could not be found!")))
                        .doOnSuccess(user -> {
                            user.setModifiedOn(LocalDateTime.now());
                            user.setIsEnabled(true);
                        })
                        .flatMap(userRepository::save)
                );
    }

    @Override
    public Mono<TokenDto> signIn(SignInDto signInDto) {
        String emailAddress = signInDto.getUsername();
        String password = signInDto.getPassword();
        
        return userRepository.findByEmailAddress(emailAddress)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                .filter(user -> user.getIsEnabled() && !user.getIsAccountLocked() && !user.getIsCredentialsExpired() &&
                        !user.getIsAccountExpired())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Account is disabled! Contact Administrator!")))
                .doOnSuccess($ -> log.debug("Checks for signing in {} were successful", emailAddress))
                .flatMap(user -> Mono
                        .just(encoder.matches(password, user.getPassword())).subscribeOn(Schedulers.parallel())
                        .filter(passwordsMatch -> passwordsMatch)
                        .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                        .flatMap($ -> roleRepository
                                .findById(user.getRoleId())
                                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                                .doOnSuccess(a -> log.debug(" User role {} found successfully", a.getName()))
                                .flatMap(role -> userAuthorityService.findByRoleId(role.getId())
                                        .collectList()
                                        .doOnSuccess(a -> log.debug(" {} user authorities found successfully", a.size()))
                                        .map(authorities -> jwtUtil.generateToken(user, role, authorities)))
                                .filter(StringUtils::hasText)
                                .switchIfEmpty(Mono.error(new UsernameNotFoundException("Invalid username or password!")))
                                .doOnSuccess(a -> log.info(" Token for {} generated successfully", emailAddress))
                                .flatMap(token -> userTokenService.create(new UserTokenDto(emailAddress, token))
                                        .map(UserToken::getToken))
                                .doOnSuccess(a -> log.debug(" Token saved on database successfully"))
                                .map(TokenDto::new)
                        )
                );
    }

    @Override
    public Mono<User> changePassword(String userId, PasswordDto dto) {
        return userRepository.findById(userId)
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
    public Mono<User> setPassword(String token, String password) {
        return oneTimeTokenService.findByToken(token)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Token could not be found!")))
                .filter(t -> !t.hasExpired())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Token has expired! Contact administrator.")))
                .flatMap(ott -> userRepository.findById(ott.getUserId())
                        .switchIfEmpty(Mono.error(new CustomNotFoundException("User could not be found!")))
                        .filter(User::getIsEnabled)
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("User is disabled! Contact administrator.")))
                        .map(user -> {
                            user.setPassword(encoder.encode(password));
                            return user;
                        })
                        .flatMap(userRepository::save)
                        .map(user -> {
                        	oneTimeTokenService.deleteById(ott.getId());
                            return user;
                        })
                );
    }

	@Override
	@Transactional
	public Mono<Void> requestPasswordReset(EmailDto dto) {
		return userRepository.findByEmailAddress(dto.getEmailAddress())
				.switchIfEmpty(Mono.error(new CustomBadRequestException(
						"Email address %s could not be found!".formatted(dto.getEmailAddress()))))
				.flatMap(user -> notifyForRequestPasswordReset(user)).flatMap($ -> Mono.empty());
	}

	private Mono<EmailNotification> notifyForRequestPasswordReset(User user) {
		return oneTimeTokenService.create(user.getId())
		.flatMap(ott -> requestPasswordResetEmailNotificationService.create(user, ott.getToken()));
	}
}
