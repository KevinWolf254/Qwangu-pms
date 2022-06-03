package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataMongoTest
@ExtendWith(SpringExtension.class)
class CustomUserRepositoryImplIntegrationTest {

    @Container
    private final static MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

    @Autowired
    private ReactiveMongoTemplate template;

    @Autowired
    private CustomUserRepositoryImpl customUserRepository;

    @Test
    @DisplayName("create returns a Mono of User when email address does not exist")
    void create_returnsMonoOfUser_whenEmailAddressDoesNotExist() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, "person@gmail.com", "1");

        Authority authority = new Authority("1", "ADMIN_USERS", true, true, true, true, true, LocalDateTime.now(), null);
        Role role = new Role("1", "ADMIN", null, LocalDateTime.now(), null);

        // when
        Mono<User> user = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .then(template
                        .dropCollection(Role.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Role!")))
                .then(template
                        .dropCollection(Authority.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Authority!")))
                .then(template
                        .save(authority, "AUTHORITY")
                        .doOnSuccess(System.out::println))
                .flatMap(authResult -> {
                    role.setAuthorityIds(Set.of(authResult.getId()));
                    return template.save(role, "ROLE");
                })
                .doOnSuccess(System.out::println)
                .flatMap(roleResult -> customUserRepository.create(dto))
                .doOnSuccess(System.out::println);

        // then
        StepVerifier
                .create(user)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("create returns CustomNotFoundException when role id does not exist")
    void create_returnsCustomNotFoundException_whenRoleIdDoesNotExist() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        String roleId = "1";
        UserDto dto = new UserDto(person, "person@gmail.com", roleId);

        // when
        Mono<User> user = template
                .dropCollection(Role.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table Role!"))
                .then(customUserRepository
                        .create(dto)
                        .doOnError(System.out::println));

        // then
        StepVerifier
                .create(user)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Role with id %s does not exist!".formatted(roleId)))
                .verify();
    }

    @Test
    @DisplayName("create returns CustomAlreadyExistsException when email address exists")
    void create_returnsCustomAlreadyExistsException_whenEmailAddressExists() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        UserDto dto = new UserDto(person, emailAddress, "1");

        Authority authority = new Authority("1", "ADMIN_USERS", true, true, true, true, true, LocalDateTime.now(), null);
        Role role = new Role("1", "ADMIN", null, LocalDateTime.now(), null);

        // when
        Mono<User> user = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .then(template
                        .dropCollection(Role.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Role!")))
                .then(template
                        .dropCollection(Authority.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Authority!")))
                .then(template
                        .save(authority, "AUTHORITY")
                        .doOnSuccess(System.out::println))
                .flatMap(authResult -> {
                    role.setAuthorityIds(Set.of(authResult.getId()));
                    return template.save(role, "ROLE");
                })
                .doOnSuccess(System.out::println)
                .flatMap(roleResult -> customUserRepository.create(dto))
                .doOnSuccess(System.out::println)
                .flatMap(userResult -> customUserRepository.create(dto))
                .doOnError(System.out::println);

        // then
        StepVerifier
                .create(user)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equalsIgnoreCase("User with email address %s already exists!".formatted(emailAddress)))
                .verify();
    }

    @Test
    @DisplayName("update returns Mono of user when successful")
    void update_returnsMonoOfUser_whenRoleIdUserIdAndEmailAddressExists() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        UserDto dto = new UserDto(person, emailAddress, "1");
        User userEntity = new User(id, person, emailAddress, "1", null, false, false, false, true, LocalDateTime.now(), null);

        Authority authority = new Authority("1", "ADMIN_USERS", true, true, true, true, true, LocalDateTime.now(), null);
        Role role = new Role("1", "ADMIN", null, LocalDateTime.now(), null);

        // when
        Mono<User> user = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .then(template
                        .dropCollection(Role.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Role!")))
                .then(template
                        .dropCollection(Authority.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Authority!")))
                .then(template
                        .save(authority, "AUTHORITY")
                        .doOnSuccess(System.out::println))
                .flatMap(authResult -> {
                    role.setAuthorityIds(Set.of(authResult.getId()));
                    return template.save(role, "ROLE");
                })
                .doOnSuccess(System.out::println)
                .flatMap(roleResult -> template.save(userEntity, "USER"))
                .doOnSuccess(System.out::println)
                .flatMap(userResult -> customUserRepository.update(id, dto))
                .doOnSuccess(System.out::println);

        // then
        StepVerifier
                .create(user)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("update returns CustomNotFoundException when role id does not exists")
    void update_returnsCustomNotFoundException_whenRoleIdDoesNotExist() {
        // given
        String roleId = "2";
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        User userEntity = new User(id, person, emailAddress, "1", null, false, false, false, true, LocalDateTime.now(), null);
        Role role = new Role("1", "ADMIN", null, LocalDateTime.now(), null);

        Authority authority = new Authority("1", "ADMIN_USERS", true, true, true, true, true, LocalDateTime.now(), null);

        // when
        Mono<User> user = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .then(template
                        .dropCollection(Role.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Role!")))
                .then(template
                        .dropCollection(Authority.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Authority!")))
                .then(template
                        .save(authority, "AUTHORITY")
                        .doOnSuccess(System.out::println))
                .flatMap(authResult -> {
                    role.setAuthorityIds(Set.of(authResult.getId()));
                    return template.save(role, "ROLE");
                })
                .doOnSuccess(System.out::println)
                .flatMap(roleResult -> template.save(userEntity, "USER"))
                .doOnSuccess(System.out::println)
                .flatMap(userResult -> customUserRepository.update(id, dto))
                .doOnError(System.out::println);

        // then
        StepVerifier
                .create(user)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Role with id %s does not exist!".formatted(roleId)))
                .verify();
    }

    @Test
    @DisplayName("update returns CustomNotFoundException when role id does not exists")
    void update_returnsCustomNotFoundException_whenUserIdWithEmailAddressDoesNotExist() {
        // given
        String roleId = "1";
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String emailAddress1 = "goblin@gmail.com";
        UserDto dto = new UserDto(person, emailAddress1, roleId);
        User userEntity = new User(id, person, emailAddress, "1", null, false, false, false, true, LocalDateTime.now(), null);

        Role role = new Role(roleId, "ADMIN", null, LocalDateTime.now(), null);
        Authority authority = new Authority("1", "ADMIN_USERS", true, true, true, true, true, LocalDateTime.now(), null);

        // when
        Mono<User> user = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .then(template
                        .dropCollection(Role.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Role!")))
                .then(template
                        .dropCollection(Authority.class)
                        .doOnSuccess(t -> System.out.println("---- Dropped table Authority!")))
                .then(template
                        .save(authority, "AUTHORITY")
                        .doOnSuccess(System.out::println))
                .flatMap(authResult -> {
                    role.setAuthorityIds(Set.of(authResult.getId()));
                    return template.save(role, "ROLE");
                })
                .doOnSuccess(System.out::println)
                .flatMap(roleResult -> template.save(userEntity, "USER"))
                .doOnSuccess(System.out::println)
                .flatMap(userResult -> customUserRepository.update(id, dto))
                .doOnError(System.out::println);

        // then
        StepVerifier
                .create(user)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("User with id %s and email address %s does not exist!".formatted(id, emailAddress1)))
                .verify();
    }

    @Test
    @DisplayName("find paginated returns Flux of users when exists")
    void find_paginated_returnsFluxOfUsers_whenSuccessful() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        User userEntity = new User(id, person, "person@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null);

        String id2 = "2";
        Person person2 = new Person("Jane", "Doe", "Doe");
        User userEntity2 = new User(id2, person2, "person2@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null);

        //when
        Flux<User> saved = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .thenMany(Flux
                        .just(userEntity, userEntity2))
                .flatMap(entity -> template.save(entity, "USER"))
                .thenMany(customUserRepository.findPaginated(Optional.empty(),
                        Optional.empty(), 1, 10,
                        OrderType.ASC))
                .doOnNext(System.out::println);

        // then
        StepVerifier.create(saved)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("find paginated returns Mono of CustomNotFoundException when users do not exist")
    void find_paginated_returnsCustomNotFoundException_whenUsersDoNotExist() {
        // given
        //when
        Flux<User> saved = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .thenMany(customUserRepository.findPaginated(Optional.empty(),
                        Optional.empty(), 1, 10,
                        OrderType.ASC))
                .doOnError(a -> System.out.println("---- Found no users!"));
        // then
        StepVerifier
                .create(saved)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Users were not found!"))
                .verify();
    }

    @Test
    @DisplayName("delete returns a true when successful")
    void delete_ReturnsTrue_WhenSuccessful() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        User userEntity = new User(id, person, "person@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null);

        String id2 = "2";
        Person person2 = new Person("Jane", "Doe", "Doe");
        User userEntity2 = new User(id2, person2, "person2@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null);

        // when
        Flux<Boolean> deleted = template
                .dropCollection(User.class)
                .doOnSuccess(t -> System.out.println("---- Dropped table User!"))
                .thenMany(Flux
                        .just(userEntity, userEntity2))
                .flatMap(entity -> template.save(entity, "USER"))
                .doOnNext(u -> System.out.println("---- Created %s".formatted(u)))
                .flatMap(user -> customUserRepository.delete(user.getId()))
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
                .then(customUserRepository.delete(id))
                .doOnError(System.out::println);

        // then
        StepVerifier
                .create(deleted)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("User with id %s does not exist!".formatted(id)))
                .verify();
    }
}