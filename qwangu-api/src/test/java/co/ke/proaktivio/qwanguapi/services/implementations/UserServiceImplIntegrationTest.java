package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.UserAuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.OneTimeTokenRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import co.ke.proaktivio.qwanguapi.services.EmailService;
import co.ke.proaktivio.qwanguapi.services.OneTimeTokenService;
import co.ke.proaktivio.qwanguapi.services.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserServiceImplIntegrationTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserRoleRepository roleRepository;
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;
    @Autowired
    private UserService underTest;
    @Autowired
    private OneTimeTokenService oneTimeTokenService;
    @Autowired
    private OneTimeTokenRepository oneTimeTokenRepository;
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ReactiveMongoTemplate template;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;
    @MockBean
    private EmailService emailService;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

	private Mono<Void> reset() {
		return userRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Users!"))
                .then(roleRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Roles!"))
                .then(userAuthorityRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Authorities!"))
                .then(oneTimeTokenRepository.deleteAll())
                .doOnSuccess($ -> System.out.println("---- Deleted all Tokens!"));
	}

    @Test
    void create_returnsCustomNotFoundException_whenUserRoleDoesNotExist() {
    	// given
        Person person = new Person("John", "Doe", "Doe");
        String userRoleId = "1";
		UserDto dto = new UserDto(person, "john.doe@gmail.com", userRoleId);

		var role = new UserRole.UserRoleBuilder()
				.name("ADMIN")
				.build();
		role.setId(userRoleId);
		// when
		Mono<User> createWithUserRoleIdNotExisting = reset().then(underTest.create(dto));
		// then
        StepVerifier
                .create(createWithUserRoleIdNotExisting)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException 
                		&& e.getMessage().equals("UserRole with id %s does not exist!".formatted(userRoleId)))
                .verify();
		
    }
    
	@Test
	void create_returnsUser_whenSuccessful() {
		String firstName = "John";
		String otherNames = "Doe";
		String surname = "Doe";
		Person person = new Person(firstName, otherNames, surname);
		String emailAddress = "john.doe@gmail.com";
		String userRoleId = "1";
		UserDto dto = new UserDto(person, emailAddress, userRoleId);

		var role = new UserRole.UserRoleBuilder().name("ADMIN").build();
		role.setId(userRoleId);

		UserAuthority userAuthority = new UserAuthority(userRoleId, "ADMIN_USERS", true, true, true, true, true,
				userRoleId, LocalDateTime.now(), null, null, null);

		// when
		Mono<User> create = reset().then(userAuthorityRepository.save(userAuthority)).doOnSuccess(System.out::println)
				.flatMap(authResult -> roleRepository.save(role))
				.doOnSuccess(r -> System.out.println("---- Saved: " + r)).flatMap(roleResult -> underTest.create(dto))
				.doOnSuccess(u -> System.out.println("---- Created: " + u));

		// then
		StepVerifier
			.create(create)
			.expectNextMatches(u -> u.getId() != null && u.getPerson() != null
				&& u.getPerson().getFirstName().equals(firstName) && u.getPerson().getOtherNames().equals(otherNames)
				&& u.getPerson().getSurname().equals(surname) && u.getEmailAddress().equals(emailAddress)
				&& u.getRoleId().equals(userRoleId) && u.getPassword() == null && !u.getIsAccountExpired()
				&& !u.getIsCredentialsExpired() && !u.getIsAccountExpired() && !u.getIsEnabled()
				&& u.getCreatedOn() != null && u.getCreatedBy().equals("SYSTEM") && u.getModifiedBy().equals("SYSTEM")
				&& u.getModifiedOn() != null)
			.verifyComplete();
	}
    
    @Test
    void createAndNotify_returnsCustomAlreadyExistsException_whenEmailAddressAlreadyExists() {
    	// given
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "john.doe@gmail.com";
		UserDto dto2 = new UserDto(person, emailAddress, "1");
        // when
    	create_returnsUser_whenSuccessful();
        Mono<User> userCreateWithEmailError = underTest
                .createAndNotify(dto2)
                .doOnError(System.out::println);
        // then
        StepVerifier
                .create(userCreateWithEmailError)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException 
                		&& e.getMessage().equals("User with email address %s already exists!".formatted(emailAddress)))
                .verify();
    }

    @Test
    void update_returnsUser_whenSuccessful() {
        // given
        String userRoleId = "1";
		String id = userRoleId;
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String firstName = "Peter";
		String otherNames = "Jane";
		String surname = "Joe";
		UserDto dto = new UserDto(new Person(firstName, otherNames, surname), emailAddress, userRoleId);
        User userEntity = new User(id, person, emailAddress, userRoleId, null, false,
                false, false, true, LocalDateTime.now(), null,
                null, null);

        UserAuthority userAuthority = new UserAuthority(userRoleId, "ADMIN_USERS", true, true, true, true,
                true, userRoleId, LocalDateTime.now(), null, null, null);

		var role = new UserRole.UserRoleBuilder()
				.name("ADMIN")
				.build();
		role.setId(userRoleId);
        // when
        Mono<User> update = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .then(template.dropCollection(UserRole.class))
                .doOnSuccess(t -> System.out.println("---- Dropped table Role!"))
                .then(template.dropCollection(UserAuthority.class))
                .doOnSuccess(t -> System.out.println("---- Dropped table Authority!"))
                .then(userAuthorityRepository.save(userAuthority))
                .doOnSuccess(System.out::println)
                .flatMap(authResult -> roleRepository.save(role))
                .doOnSuccess(a -> System.out.println("---- Created " + a))
                .flatMap(roleResult -> userRepository.save(userEntity))
                .doOnSuccess(a -> System.out.println("---- Created " + a))
                .flatMap(userResult -> underTest.update(id, dto))
                .doOnSuccess(a -> System.out.println("---- Updated " + a));
        // then
		StepVerifier
		.create(update)
		.expectNextMatches(u -> u.getId() != null && u.getPerson() != null
			&& u.getPerson().getFirstName().equals(firstName) && u.getPerson().getOtherNames().equals(otherNames)
			&& u.getPerson().getSurname().equals(surname) && u.getEmailAddress().equals(emailAddress)
			&& u.getRoleId().equals(userRoleId) && u.getPassword() == null && !u.getIsAccountExpired()
			&& !u.getIsCredentialsExpired() && !u.getIsAccountExpired() && u.getIsEnabled()
			&& u.getCreatedOn() != null && u.getModifiedBy().equals("SYSTEM")
			&& u.getModifiedOn() != null)
		.verifyComplete();
    }

    @Test
    void update_returnsCustomBadRequestException_whenRoleIdDoesNotExist() {
        // given
        String roleId = "2";
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        User userEntity = new User(id, person, emailAddress, "1", null, false, false, false, true, LocalDateTime.now(), null, null, null);

		var role = new UserRole.UserRoleBuilder()
				.name("ADMIN")
				.build();
		role.setId("1");
		
        UserAuthority userAuthority = new UserAuthority("1", "ADMIN_USERS", true, true, true, true,
                true, "1", LocalDateTime.now(), null, null, null);

        // when
        Mono<User> user = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .then(template.dropCollection(UserRole.class))
                        .doOnSuccess(t -> System.out.println("---- Dropped table Role!"))
                .then(template
                        .dropCollection(UserAuthority.class))
                        .doOnSuccess(t -> System.out.println("---- Dropped table Authority!"))
                .then(userAuthorityRepository.save(userAuthority)
                        .doOnSuccess(System.out::println))
                .flatMap(authResult -> roleRepository.save(role))
                .doOnSuccess(System.out::println)
                .flatMap(roleResult -> userRepository.save(userEntity))
                .doOnSuccess(System.out::println)
                .flatMap(userResult -> underTest.update(id, dto))
                .doOnError(System.out::println);

        // then
        StepVerifier
                .create(user)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equalsIgnoreCase("Role with id %s does not exist!".formatted(roleId)))
                .verify();
    }

    @Test
    void update_returnsCustomBadRequestException_whenUserIdWithEmailAddressDoesNotExist() {
        // given
        String roleId = "1";
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String emailAddress1 = "goblin@gmail.com";
        UserDto dto = new UserDto(person, emailAddress1, roleId);
        User userEntity = new User(id, person, emailAddress, "1", null, false, false, false, true, LocalDateTime.now(), null, null, null);

		var role = new UserRole.UserRoleBuilder()
				.name("ADMIN")
				.build();
		role.setId(roleId);
		
        UserAuthority userAuthority = new UserAuthority("1", "ADMIN_USERS", true, true, true, true,
                true, roleId, LocalDateTime.now(), null, null, null);

        // when
        Mono<User> user = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .then(template.dropCollection(UserRole.class))
                .doOnSuccess(t -> System.out.println("---- Dropped table Role!"))
                .then(template.dropCollection(UserAuthority.class))
                .doOnSuccess(t -> System.out.println("---- Dropped table Authority!"))
                .then(userAuthorityRepository.save(userAuthority))
                .doOnSuccess(System.out::println)
                .flatMap(authResult -> roleRepository.save(role))
                .doOnSuccess(System.out::println)
                .flatMap(roleResult -> userRepository.save(userEntity))
                .doOnSuccess(System.out::println)
                .flatMap(userResult -> underTest.update(id, dto))
                .doOnError(System.out::println);

        // then
        StepVerifier
                .create(user)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equalsIgnoreCase("User with id %s and email address %s does not exist!".formatted(id, emailAddress1)))
                .verify();
    }

    @Test
    void findAll_returnsUsers_whenSuccessful() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        User userEntity = new User(id, person, "person@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null, null, null);

        String id2 = "2";
        Person person2 = new Person("Jane", "Doe2", "Doe2");
        User userEntity2 = new User(id2, person2, "person2@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null, null, null);

        //when
        Flux<User> saved = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .thenMany(Flux
                        .just(userEntity, userEntity2))
                .flatMap(entity -> template.save(entity, "USER"))
                .thenMany(underTest.findAll(null, null))
                .doOnNext(System.out::println);

        // then
        StepVerifier.create(saved)
        		.expectNextMatches(u -> u.getId() != null && u.getPerson() != null
    			&& u.getPerson().getFirstName().equals("Jane") && u.getPerson().getOtherNames().equals("Doe2")
    			&& u.getPerson().getSurname().equals("Doe2") && u.getEmailAddress().equals("person2@gmail.com")
    			&& u.getRoleId().equals("1") && u.getPassword() == null && !u.getIsAccountExpired()
    			&& !u.getIsCredentialsExpired() && !u.getIsAccountExpired() && u.getIsEnabled()
    			&& u.getCreatedOn() != null && u.getModifiedBy().equals("SYSTEM")
    			&& u.getModifiedOn() != null)
        		.expectNextMatches(u -> u.getId() != null && u.getPerson() != null
    			&& u.getPerson().getFirstName().equals("John") && u.getPerson().getOtherNames().equals("Doe")
    			&& u.getPerson().getSurname().equals("Doe") && u.getEmailAddress().equals("person@gmail.com")
    			&& u.getRoleId().equals("1") && u.getPassword() == null && !u.getIsAccountExpired()
    			&& !u.getIsCredentialsExpired() && !u.getIsAccountExpired() && u.getIsEnabled()
    			&& u.getCreatedOn() != null && u.getModifiedBy().equals("SYSTEM")
    			&& u.getModifiedOn() != null)
                .verifyComplete();
    }

    @Test
    void findAll_returnsEmpty_whenUsersDoNotExist() {
        // given
        //when
        Flux<User> saved = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .thenMany(underTest.findAll(null, null));
        // then
        StepVerifier
                .create(saved)
                .expectComplete();
//        .expectNextMatches(List::isEmpty)
//        .verifyComplete();
    }

    @Test
    void delete_returnsTrue_whenSuccessful() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        User userEntity = new User(id, person, "person@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null, null, null);

        String id2 = "2";
        Person person2 = new Person("Jane", "Doe", "Doe");
        User userEntity2 = new User(id2, person2, "person2@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null, null, null);

        // when
        Flux<Boolean> deleted = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .thenMany(Flux
                        .just(userEntity, userEntity2))
                .flatMap(entity -> template.save(entity, "USER"))
                .doOnNext(u -> System.out.printf("---- Created %s%n", u))
                .flatMap(user -> underTest.deleteById(user.getId()))
                .doOnNext(b -> System.out.println("---- Deleted user!"));

        // then
        StepVerifier
                .create(deleted)
                .expectNext(true)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns CustomNotFoundException when users do not exist")
    void delete_returnsCustomNotFoundException_whenUsersDoNotExist() {
        // given
        String id = "1";
        // when
        Mono<Boolean> deleted = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .then(underTest.deleteById(id))
                .doOnError(System.out::println);
        // then
        StepVerifier
                .create(deleted)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("User with id %s does not exist!".formatted(id)))
                .verify();
    }

    @Test
    void activate_returnsCustomBadRequestException_whenTokenDoesNotExist() {
    	// given
    	var ott = new OneTimeToken.OneTimeTokenBuilder().userId("1").build();
    	// when
		Mono<User> doesNotExistOneTimeToken = reset()
				.then(underTest.activate(ott.getToken()));
		// then
		StepVerifier
			.create(doesNotExistOneTimeToken)
			.expectErrorMatches(e -> e instanceof CustomBadRequestException
					&& e.getMessage().equals("Token does not exist!"))
			.verify();
    }
    
    @Test
    void activate_returnsCustomBadRequestException_whenTokenHasExpired() {
    	// given
    	var userId = "1";
    	LocalDateTime now = LocalDateTime.now();
    	var oneTimeToken = new OneTimeToken.OneTimeTokenBuilder().userId(userId).build();
    	
    	// when
		Mono<User> expiredOneTimeToken = reset()
				.then(oneTimeTokenRepository.save(oneTimeToken))
				.doOnSuccess(u -> System.out.println("---- Created: " + u))
				.doOnNext(ott -> ott.setCreatedOn(now.minusDays(2)))
				.flatMap(ott -> oneTimeTokenRepository.save(ott))
				.doOnSuccess(u -> System.out.println("---- Updated: " + u))
				.flatMap(ott -> underTest.activate(ott.getToken()));
		
		// then
		StepVerifier
			.create(expiredOneTimeToken)
			.expectErrorMatches(e -> e instanceof CustomBadRequestException
					&& e.getMessage().equals("Token has expired! Contact administrator."))
			.verify();
    }

    @Test
    void activate_returnsCustomBadRequestException_whenUserDoesNotExist() {
        // given
    	var userIdDoesNotExist = "12345";
        var ott = new OneTimeToken.OneTimeTokenBuilder().userId(userIdDoesNotExist).build();

        // when
        Mono<User> activateDoesNotExistUser = reset()
                .then(oneTimeTokenRepository.save(ott)
                .doOnSuccess(otts -> System.out.println("---- Created: " + otts))
                .flatMap(otts -> underTest.activate(otts.getToken())))
                .doOnSuccess(otts -> System.out.println("---- Result: " + otts));

		// then
		StepVerifier
			.create(activateDoesNotExistUser)
			.expectErrorMatches(e -> e instanceof CustomBadRequestException
					&& e.getMessage().equals("User could not be found!"))
			.verify();
    }

    @Test
    void activate_returnsUser_whenSuccessful() {
        // given
    	var userId = "56789";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
		String userRoleId = "1";
		User user = new User(userId, person, emailAddress, userRoleId, null,
                false, false, false, false, LocalDateTime.now(), null, null, null);
        // when
        Mono<User> activateUser = reset()
                .then(userRepository.save(user))
                .doOnSuccess(u -> System.out.println("---- Created: " + u))
                .flatMap(u -> oneTimeTokenService.create(u.getId())
                .doOnSuccess(ott -> System.out.println("---- Created: " + ott))
                .flatMap(ott -> underTest.activate(ott.getToken())))
                .doOnSuccess(ott -> System.out.println("---- Result: " + ott));

        // then
        StepVerifier.create(activateUser)
                .expectNextMatches(u -> u.getIsEnabled())
                .verifyComplete();
    }

    @Test
    void signIn_returnsCustomBadRequestException_whenUserHasBeenDisabled() {
    	// given
        LocalDateTime now = LocalDateTime.now();
        var password = "12345";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        User user = new User(null, person, emailAddress, null, password,
                false, false, false, false, now,
                null, null, null);
        // when
		Mono<TokenDto> userDoesNotExist = reset()
				.then(userRepository.save(user))
				.then(underTest.signIn(new SignInDto(emailAddress, password)));
		// then
		StepVerifier
		.create(userDoesNotExist)
		.expectErrorMatches(e -> e instanceof CustomBadRequestException
				&& e.getMessage().equals("Account is disabled! Contact Administrator!"))
		.verify();
    }

    @Test
    void signIn_returnsUsernameNotFoundException_whenPasswordsDoNotMatch() {
    	// given
        LocalDateTime now = LocalDateTime.now();
        var password = "12345";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        User user = new User(null, person, emailAddress, null, password,
                false, false, false, true, now,
                null, null, null);
        // when
		Mono<TokenDto> userDoesNotExist = reset()
				.then(userRepository.save(user))
				.then(underTest.signIn(new SignInDto(emailAddress, "12345678")));
		// then
		StepVerifier
		.create(userDoesNotExist)
		.expectErrorMatches(e -> e instanceof UsernameNotFoundException
				&& e.getMessage().equals("Invalid username or password!"))
		.verify();
    }

    @Test
    void signIn_returnsUsernameNotFoundException_whenUserWithEmailAddressDoesNotExist() {
    	// given
        String emailAddress = "person@gmail.com";
        var password = "12345";
        // when
		Mono<TokenDto> userDoesNotExist = reset().then(underTest.signIn(new SignInDto(emailAddress, password)));
		// then
		StepVerifier
		.create(userDoesNotExist)
		.expectErrorMatches(e -> e instanceof UsernameNotFoundException
				&& e.getMessage().equals("Invalid username or password!"))
		.verify();
    }

    @Test
    void signIn_returnsTokenDto_whenSuccessful() {
        LocalDateTime now = LocalDateTime.now();
        var password = "12345";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        User user = new User(null, person, emailAddress, null, password,
                false, false, false, true, now,
                null, null, null);
		var role = new UserRole.UserRoleBuilder()
				.name("ADMIN")
				.build();

        var authority = new UserAuthority(null, "USER", true, true, true, true,
                true, null, now, null, null, null);
        // when
        Mono<TokenDto> signIn = reset()
                .then(userRepository.save(user))
                .doOnSuccess(u -> System.out.println("---- Created " + u))
                .doOnSuccess(u -> u.setPassword(encoder.encode(u.getPassword()))).subscribeOn(Schedulers.parallel())
                .doOnSuccess(u -> System.out.println("---- Encrypted password for " + u))
                .flatMap(u -> roleRepository.save(role)
                        .doOnSuccess(r -> System.out.println("---- Created " + r))
                        .flatMap(r -> {
                            authority.setRoleId(r.getId());
                            return userAuthorityRepository.save(authority)
                                    .doOnSuccess(a -> System.out.println("---- Created " + a))
                                    .flatMap($ -> {
                                        u.setRoleId(r.getId());
                                        return userRepository.save(u);
                                    });
                        })
                )
                .doOnSuccess(u -> System.out.println("---- Updated role for " + u))
                .then(underTest.signIn(new SignInDto(emailAddress, password)))
                .doOnSuccess(u -> System.out.println("---- Result " + u));
        // then
        StepVerifier
                .create(signIn)
                .expectNextMatches(ut -> StringUtils.hasText(ut.getToken()) && jwtUtil.isValid(ut.getToken()))
                .verifyComplete();
    }

    @Test
    void changePassword_returnsUser_whenSuccessful() {
        LocalDateTime now = LocalDateTime.now();
        var password = "12345";
        String newPassword = "A1234567";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        User user = new User(null, person, emailAddress, "1", password,
                false, false, false, true, now,
                null, null, null);
        // when
        Mono<User> changePassword = reset()
                .then(userRepository.save(user))
                .doOnSuccess(u -> System.out.println("---- Created " + u))
                .doOnSuccess(u -> u.setPassword(encoder.encode(u.getPassword()))).subscribeOn(Schedulers.parallel())
                .doOnSuccess(u -> System.out.println("---- Encrypted password for " + u))
                .flatMap(u -> userRepository.save(u))
                .doOnSuccess(u -> System.out.println("---- Updated " + u))
                .flatMap(u -> underTest.changePassword(u.getId(), new PasswordDto(password, newPassword)))
                .doOnSuccess(u -> System.out.println("---- Result " + u));
        // then
        StepVerifier
                .create(changePassword)
                .expectNextMatches(u -> encoder.matches(newPassword, u.getPassword()))
                .verifyComplete();


    }

    @Test
    void setPassword_returnUser_whenSuccessful() {
        LocalDateTime now = LocalDateTime.now();
        var password = "12345";
        String newPassword = "A1234567";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        User user = new User(null, person, emailAddress, null, password,
                false, false, false, true, now,
                null, null, null);

        // when
        Mono<User> resetPassword = reset()
                .then(userRepository.save(user))
                .doOnSuccess(u -> System.out.println("---- Created " + u))
                .flatMap(u -> oneTimeTokenService.create(u.getId()))
                .doOnSuccess(u -> System.out.println("---- Created " + u))
                .flatMap(ott -> underTest.setPassword(ott.getToken(), newPassword))
                .doOnSuccess(u -> System.out.println("---- Result " + u));
        // then
        StepVerifier
                .create(resetPassword)
                .expectNextMatches(u -> encoder.matches(newPassword, u.getPassword()))
                .verifyComplete();
    }

    @Test
    void sendResetPassword_whenSuccessful() {
        LocalDateTime now = LocalDateTime.now();
        var password = "12345";
        String id = "A1234567";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        User user = new User(id, person, emailAddress, null, password,
                false, false, false, true, now,
                null, null, null);
        // when
        Mockito.when(emailService.send(Mockito.any(EmailNotification.class))).thenReturn(Mono.just(true));
        Flux<OneTimeToken> resetPassword = reset()
                .then(userRepository.save(user))
                .doOnSuccess(u -> System.out.println("---- Created " + u))
                .then(underTest.requestPasswordReset(new EmailDto(emailAddress)))
                .thenMany(oneTimeTokenRepository.findAll())
                .doOnNext(ott -> System.out.println("---- Found " + ott));

        // then
        StepVerifier
                .create(resetPassword)
                .expectNextMatches(ott -> ott.getUserId().equals(id))
                .verifyComplete();
    }
}