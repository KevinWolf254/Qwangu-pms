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
import co.ke.proaktivio.qwanguapi.services.EmailGenerator;
import co.ke.proaktivio.qwanguapi.services.EmailService;
import co.ke.proaktivio.qwanguapi.services.OneTimeTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
class UserServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private static EmailGenerator emailGenerator;
    @Mock
    private OneTimeTokenService oneTimeTokenService;
    @Mock
    private OneTimeTokenRepository oneTimeTokenRepository;
    @Mock
    private PasswordEncoder encoder;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private JwtUtil jwtUtil;
    @InjectMocks
    private UserServiceImpl userService;


    @Test
    @DisplayName("create returns a Mono of User when email address does not exist")
    void create_returnMonoOfUser_WhenEmailAddressDoesNotExist() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String roleId = "1";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        User user = new User("1", person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), null);
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
    @DisplayName("signIn return a TokenDto when successful or UserNotFFoundException when an error occurs")
    void signIn() {
        // given
        String password = "pass@123";
        String encodedPassword = encoder.encode(password);
        var dto = new SignInDto("person@gmail.com", password);
        LocalDateTime now = LocalDateTime.now();
        var person = new Person("John", "Doe", "Doe");
        var user = new User("1", person, "person@gmail.com", "1", encodedPassword,
                false, false, false, true, now, now);
        var role = new Role("1", "ADMIN", Set.of("1"), now, null);
        var token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        // when
        Mockito.when(userRepository.findOne(Example.of(new User(dto.getUsername())))).thenReturn(Mono.just(user));
        Mockito.when(encoder.matches(dto.getPassword(), user.getPassword())).thenReturn(true);
        Mockito.when(roleRepository.findById("1")).thenReturn(Mono.just(role));
        Mockito.when(jwtUtil.generateToken(user, role)).thenReturn(token);
        // then
        StepVerifier
                .create(userService.signIn(dto))
                .expectNextCount(1)
                .verifyComplete();

        // then
        Mockito.when(userRepository.findOne(Example.of(new User(dto.getUsername())))).thenReturn(Mono.empty());
        StepVerifier
                .create(userService.signIn(dto))
                .expectErrorMatches(e -> e instanceof UsernameNotFoundException &&
                        e.getMessage().equals("Invalid username or password!"))
                .verify();
        //then
        Mockito.when(userRepository.findOne(Example.of(new User(dto.getUsername())))).thenReturn(Mono.just(user));
        Mockito.when(encoder.matches(dto.getPassword(), user.getPassword())).thenReturn(false);
        StepVerifier
                .create(userService.signIn(dto))
                .expectErrorMatches(e -> e instanceof UsernameNotFoundException &&
                        e.getMessage().equals("Invalid username or password!"))
                .verify();
        // then
        Mockito.when(roleRepository.findById("1")).thenReturn(Mono.empty());
        StepVerifier
                .create(userService.signIn(dto))
                .expectErrorMatches(e -> e instanceof UsernameNotFoundException &&
                        e.getMessage().equals("Invalid username or password!"))
                .verify();
        // then
        Mockito.when(roleRepository.findById("1")).thenReturn(Mono.just(role));
        Mockito.when(jwtUtil.generateToken(user, role)).thenReturn(null);
        StepVerifier
                .create(userService.signIn(dto))
                .expectErrorMatches(e -> e instanceof UsernameNotFoundException &&
                        e.getMessage().equals("Invalid username or password!"))
                .verify();
    }

    @Test
    @DisplayName("changePassword returns a User when successful or UserNotFFoundException/CustomBadRequestException when an error occurs")
    void changePassword() {
        // given
        String currentPassword = "pass@123!Pass";
        String encodedPassword = encoder.encode(currentPassword);
        String userId = "1";
        PasswordDto dto = new PasswordDto(currentPassword, "pass!123@Pass");
        LocalDateTime now = LocalDateTime.now();
        var person = new Person("John", "Doe", "Doe");
        var user = new User("1", person, "person@gmail.com", "1", encodedPassword,
                false, false, false, true, now, now);
        // when
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        Mockito.when(encoder.matches(dto.getCurrentPassword(), user.getPassword())).thenReturn(true);
        Mockito.when(userRepository.save(user)).thenReturn(Mono.just(user));

        // then
        StepVerifier
                .create(userService.changePassword(userId, dto))
                .expectNext(user)
                .verifyComplete();
        // then
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.empty());
        StepVerifier
                .create(userService.changePassword(userId, dto))
                .expectErrorMatches(e -> e instanceof UsernameNotFoundException &&
                        e.getMessage().equals("User with id %s does not exist!".formatted(userId)))
                .verify();
        // then
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        Mockito.when(encoder.matches(dto.getCurrentPassword(), user.getPassword())).thenReturn(false);
        StepVerifier
                .create(userService.changePassword(userId, dto))
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Passwords do not match!"))
                .verify();
    }

    @Test
    @DisplayName("createAndNotify returns a Mono of User when email address does not exist")
    void createAndNotify_returnMonoOfUser_WhenEmailAddressDoesNotExist() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String roleId = "1";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        String userId = "1";
        User user = new User(userId, person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), null);
        String token = UUID.randomUUID().toString();
        var now = LocalDateTime.now();
        var oneTimeToken = new OneTimeToken(null, token, now, now.plusHours(12), user.getId());
        Email email = new Email();
        email.setSubject("Account Activation");
        email.setTo(List.of(emailAddress));
        // when
        Mockito.when(userRepository.create(dto)).thenReturn(Mono.just(user));
        Mockito.when(emailGenerator.generateAccountActivationEmail(eq(user), any(String.class))).thenReturn(email);
        Mockito.when(oneTimeTokenService.create(eq(userId), any(String.class))).thenReturn(Mono.just(oneTimeToken));
        Mockito.when(emailService.send(email)).thenReturn(Mono.just(false));
        // then
        StepVerifier
                .create(userService.createAndNotify(dto))
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
    @DisplayName("update returns Mono of user when successful")
    void update_returnsMonoOfUser_whenRoleIdUserIdAndEmailAddressExists() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        UserDto dto = new UserDto(person, emailAddress, roleId);
        String id ="1";
        LocalDateTime now = LocalDateTime.now();
        User user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, now);
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
        User userEntity = new User(id, person, "person@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null);

        String id2 = "2";
        Person person2 = new Person("Jane", "Doe", "Doe");
        User userEntity2 = new User(id2, person2, "person2@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null);

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

    @Test
    void active() {
        // given
        var token = UUID.randomUUID().toString();
        var userId = "1";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(userId, person, "person@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null);
        LocalDateTime now = LocalDateTime.now();
        OneTimeToken ott = new OneTimeToken("1", token, now, now.plusHours(12), userId);

        // when
        Mockito.when(oneTimeTokenService.find(token, userId)).thenReturn(Mono.just(ott));
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        Mockito.when(userRepository.save(user)).thenReturn(Mono.just(user));
        Mockito.when(oneTimeTokenRepository.deleteById(ott.getId())).thenReturn(Mono.empty());

        // then
        StepVerifier
                .create(userService.activate(token, userId))
                .expectNext(user)
                .verifyComplete();

        // when
        ott.setCreated(now.plusMinutes(30));
        ott.setExpiration(now);
        // then
        StepVerifier
                .create(userService.activate(token, userId))
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equalsIgnoreCase("Token has expired! Contact administrator."))
                .verify();

        // when
        ott.setCreated(now);
        ott.setExpiration(now.plusHours(12));
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.empty());
        // then
        StepVerifier
                .create(userService.activate(token, userId))
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equalsIgnoreCase("User could not be found!"))
                .verify();

    }

    @Test
    void resetPassword() {
        // given
        var token = UUID.randomUUID().toString();
        var password = "password!234P";
        var userId = "1";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(userId, person, "person@gmail.com", "1", null, false, false, false, true, LocalDateTime.now(), null);
        LocalDateTime now = LocalDateTime.now();
        OneTimeToken ott = new OneTimeToken("1", token, now, now.plusHours(12), userId);
        OneTimeToken ott2 = new OneTimeToken("1", token, now.minusDays(2), now.minusDays(1), userId);

        // when
        Mockito.when(oneTimeTokenRepository.findOne(Example.of(new OneTimeToken(token)))).thenReturn(Mono.just(ott));
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        Mockito.when(userRepository.save(user)).thenReturn(Mono.just(user));
        Mockito.when(oneTimeTokenRepository.deleteById("1")).thenReturn(Mono.empty());

        // then
        StepVerifier
                .create(userService.resetPassword(token, userId))
                .expectNext(user)
                .verifyComplete();

        // when
        Mockito.when(oneTimeTokenRepository.findOne(Example.of(new OneTimeToken(token)))).thenReturn(Mono.just(ott2));
        // then
        StepVerifier
                .create(userService.resetPassword(token, userId))
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equalsIgnoreCase("Token has expired! Contact administrator."))
                .verify();

        // when
        Mockito.when(oneTimeTokenRepository.findOne(Example.of(new OneTimeToken(token)))).thenReturn(Mono.just(ott));
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.empty());
        // then
        StepVerifier
                .create(userService.resetPassword(token, userId))
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("User could not be found!"))
                .verify();

        // when
        user.setEnabled(false);
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        // then
        StepVerifier
                .create(userService.resetPassword(token, userId))
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equalsIgnoreCase("User is disabled! Contact administrator."))
                .verify();
    }

    @Test
    void sendResetPassword() {
        // given
        String emailAddress = "person@gmail.com";
        var emailDto = new EmailDto(emailAddress);
        var userId = "1";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(userId, person, emailAddress, "1", null, false, false, false, true, LocalDateTime.now(), null);
        String token = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        OneTimeToken ott = new OneTimeToken("1", token, now, now.plusHours(12), userId);
        Email email = new Email();
        email.setTo(List.of(emailAddress));
        email.setSubject("Password Reset");
        // when
        Mockito.when(userRepository.findOne(Example.of(new User(emailAddress)))).thenReturn(Mono.just(user));
        Mockito.when(oneTimeTokenService.create(userId, token)).thenReturn(Mono.just(ott));
        Mockito.when(emailService.send(email)).thenReturn(Mono.just(true));

        // then
        StepVerifier
                .create(userService.sendResetPassword(emailDto))
                .expectComplete();

        Mockito.when(userRepository.findOne(Example.of(new User(emailAddress)))).thenReturn(Mono.empty());

        // then
        StepVerifier
                .create(userService.sendResetPassword(emailDto))
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Email address person@gmail.com could not be found!"))
                .verify();
    }
}