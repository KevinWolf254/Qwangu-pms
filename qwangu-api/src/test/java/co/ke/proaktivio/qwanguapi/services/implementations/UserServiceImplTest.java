package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Create returns a Mono of User when email address does not exist")
    void create_returnMonoOfUser_WhenEmailAddressDoesNotExist() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String roleId = "1";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        User user = new User("1", person, emailAddress, roleId, LocalDateTime.now(), null);
        // when
        Mockito.when(userRepository.create(dto)).thenReturn(Mono.just(user));
        // then
        StepVerifier
                .create(userService.create(dto))
                .expectNextMatches(u ->
                        u.getId().equals("1") &&
                        u.getPerson().equals(person) &&
                        u.getEmailAddress().equalsIgnoreCase(emailAddress) &&
                        u.getRoleId().equalsIgnoreCase(roleId) &&
                        u.getCreated() != null &&
                        u.getModified() == null
                )
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
        Mockito.when(userRepository.create(dto)).thenReturn(Mono.error(new CustomNotFoundException("Role with id %s does not exist!".formatted(roleId))));
        // then
        StepVerifier
                .create(userService.create(dto))
                .expectErrorMatches(e ->
                        e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Role with id %s does not exist!".formatted(roleId)))
                .verify();
    }

    @Test
    @DisplayName("create returns CustomAlreadyExistsException when email address exists")
    void create_returnsCustomAlreadyExistsException_whenEmailAddressExists() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        // when
        Mockito.when(userRepository.create(dto)).thenReturn(Mono.error(new CustomAlreadyExistsException("User with email address %s already exists!".formatted(emailAddress))));
        // then
        StepVerifier
                .create(userService.create(dto))
                .expectErrorMatches(e ->
                        e instanceof CustomAlreadyExistsException &&
                                e.getMessage().equalsIgnoreCase("User with email address %s already exists!".formatted(emailAddress)))
                .verify();
    }

    @Test
    @DisplayName("update returns Mono of user when successful")
    void update_returnsMonoOfUser_whenRoleIdUserIdAndEmailAddressExists() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        String id ="1";
        LocalDateTime now = LocalDateTime.now();
        User user = new User(id, person, emailAddress, roleId, now, now);
        // when
        Mockito.when(userRepository.update(id, dto)).thenReturn(Mono.just(user));
        // then
        StepVerifier
                .create(userService.update(id, dto))
                .expectNextMatches(u ->
                        u.getId().equals("1") &&
                                u.getPerson().equals(person) &&
                                u.getEmailAddress().equalsIgnoreCase(emailAddress) &&
                                u.getRoleId().equalsIgnoreCase(roleId) &&
                                u.getCreated() != null &&
                                u.getModified() != null
                )
                .verifyComplete();

    }

    @Test
    @DisplayName("update returns CustomNotFoundException when role id does not exists")
    void update_returnsCustomNotFoundException_whenRoleIdDoesNotExist() {
        // given
        String id ="1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String roleId = "1";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        // when
        Mockito.when(userRepository.update(id, dto)).thenReturn(Mono.error(new CustomNotFoundException("Role with id %s does not exist!".formatted(roleId))));
        // then
        StepVerifier
                .create(userService.update(id, dto))
                .expectErrorMatches(e ->
                        e instanceof CustomNotFoundException &&
                                e.getMessage().equalsIgnoreCase("Role with id %s does not exist!".formatted(roleId)))
                .verify();
    }

    @Test
    @DisplayName("update returns CustomNotFoundException when role id does not exists")
    void update_returnsCustomNotFoundException_whenUserIdWithEmailAddressDoesNotExist() {
        // given
        String id ="1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String roleId = "1";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        // when
        Mockito.when(userRepository.update(id, dto)).thenReturn(Mono.error(new CustomNotFoundException("User with id %s and email address %s does not exist!".formatted(id, emailAddress))));
        // then
        StepVerifier
                .create(userService.update(id, dto))
                .expectErrorMatches(e ->
                        e instanceof CustomNotFoundException &&
                                e.getMessage().equalsIgnoreCase("User with id %s and email address %s does not exist!".formatted(id, emailAddress)))
                .verify();

    }

    @Test
    @DisplayName("find paginated returns Flux of users when exists")
    void find_paginated_returnsFluxOfUsers_whenSuccessful() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        User userEntity = new User(id, person, "person@gmail.com", "1", LocalDateTime.now(), null);

        String id2 = "2";
        Person person2 = new Person("Jane", "Doe", "Doe");
        User userEntity2 = new User(id2, person2, "person2@gmail.com", "1", LocalDateTime.now(), null);

        // when
        Mockito.when(userRepository
                .findPaginated(Optional.empty(),Optional.empty(), 1, 10,OrderType.ASC))
                .thenReturn(Flux.just(userEntity, userEntity2));
        // then
        StepVerifier
                .create(userService
                        .findPaginated(Optional.empty(),Optional.empty(), 1, 10,OrderType.ASC))
                .expectNext(userEntity)
                .expectNext(userEntity2)
                .verifyComplete();
    }

    @Test
    @DisplayName("find paginated returns Mono of CustomNotFoundException when users do not exist")
    void find_paginated_returnsCustomNotFoundException_whenUsersDoNotExist() {
        // given
        // when
        Mockito.when(userRepository
                        .findPaginated(Optional.empty(),Optional.empty(), 1, 10,OrderType.ASC))
                .thenReturn(Flux.error(new CustomNotFoundException("Users were not found!")));
        // then
        StepVerifier
                .create(userService
                        .findPaginated(Optional.empty(),Optional.empty(), 1, 10,OrderType.ASC))
                .expectErrorMatches(e ->
                        e instanceof CustomNotFoundException &&
                                e.getMessage().equalsIgnoreCase("Users were not found!"))
                .verify();
    }

    @Test
    @DisplayName("delete returns a true when successful")
    void delete_returnsTrue_whenSuccessful() {
        // given
        String id = "1";
        // when
        Mockito.when(userRepository.delete(id)).thenReturn(Mono.just(true));
        // then
        StepVerifier
                .create(userService.deleteById(id))
                .expectNext(true)
                .verifyComplete();
    }
    @Test
    @DisplayName("delete returns CustomNotFoundException when users do not exist")
    void delete_returnsCustomNotFoundException_whenUsersDoNotExist() {
        // given
        String id = "1";
        // when
        Mockito.when(userRepository.delete(id)).thenReturn(Mono.error(new CustomNotFoundException("User with id %s does not exist!".formatted(id))));
        // then
        StepVerifier
                .create(userService.deleteById(id))
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("User with id %s does not exist!".formatted(id)))
                .verify();
    }
}