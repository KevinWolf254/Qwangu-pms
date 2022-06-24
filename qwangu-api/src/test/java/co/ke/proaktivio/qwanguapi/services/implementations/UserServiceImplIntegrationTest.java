package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import co.ke.proaktivio.qwanguapi.repositories.AuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.services.EmailGenerator;
import co.ke.proaktivio.qwanguapi.services.EmailService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Set;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
class UserServiceImplIntegrationTest {

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private EmailGenerator emailGenerator;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private AuthorityRepository authorityRepository;
    @Autowired
    private UserServiceImpl userService;

    @Test
    @DisplayName("createAndNotify rollsBack when a runtime exception occurs")
    void createAndNotify() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, "john.doe@gmail.com", "1");
        UserDto dto2 = new UserDto(person, "john.doe1@gmail.com", "1");

        Authority authority = new Authority("1", "ADMIN_USERS", true, true, true, true, true, LocalDateTime.now(), null);
        Role role = new Role("1", "ADMIN", null, LocalDateTime.now(), null);

        // when
        Mono<User> create = userRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Users!"))
                .then(roleRepository
                        .deleteAll()
                        .doOnSuccess(t -> System.out.println("---- Deleted all Roles!")))
                .then(authorityRepository
                        .deleteAll()
                        .doOnSuccess(t -> System.out.println("---- Deleted all Authorities!")))
                .then(authorityRepository
                        .save(authority)
                        .doOnSuccess(System.out::println))
                .flatMap(authResult -> {
                    role.setAuthorityIds(Set.of(authResult.getId()));
                    return roleRepository.save(role);
                })
                .doOnSuccess(System.out::println)
                .flatMap(roleResult -> userService.create(dto))
                .doOnSuccess(System.out::println);

        Mono<User> userCreateWithEmailError = userService
                .createAndNotify(dto2)
                .doOnError(System.out::println);

        Flux<User> getUsers = userRepository
                .findAll()
                .doOnNext(System.out::println);

        // then
        StepVerifier
                .create(create)
                .expectNextCount(1)
                .verifyComplete();

        // then
        StepVerifier
                .create(userCreateWithEmailError)
                .expectError()
                .verify();

        // then
        StepVerifier
                .create(getUsers)
                .expectNextCount(1)
                .verifyComplete();
    }
}