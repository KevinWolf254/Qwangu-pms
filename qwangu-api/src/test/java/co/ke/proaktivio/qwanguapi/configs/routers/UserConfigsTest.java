package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.handlers.UserHandler;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UserService;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {UserConfigs.class, UserHandler.class, SecurityConfig.class, GlobalErrorConfig.class,
        GlobalErrorWebExceptionHandler.class})
class UserConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private UserService userService;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void create_returnsUnauthorized_status401_whenUserIsNotAuthenticated() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsUser_status201_whenEmailAddressDoesNotExist() {
        // given
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, emailAddress, roleId);
        User user = new User("1", person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), null, null ,null);

        //when
        Mockito.when(userService.createAndNotify(dto)).thenReturn(Mono.just(user));

        // then
        client
                .post()
                .uri("/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("User created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.person").isNotEmpty()
                .jsonPath("$.data.person.firstName").isEqualTo("John")
                .jsonPath("$.data.person.otherNames").isEqualTo("Doe")
                .jsonPath("$.data.person.surname").isEqualTo("Doe")
                .jsonPath("$.data.emailAddress").isEqualTo(emailAddress)
                .jsonPath("$.data.roleId").isEqualTo(roleId)
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenRequiredValidationFails() {
        // given
        Person person = new Person(null, "Doe", null);
        UserDto dto = new UserDto(person, null, null);

        // then
        client
                .post()
                .uri("/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Email address is required. First name is required. Surname is required. Role id is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenMinLengthValidationFails() {
        // given
        Person person = new Person(" ", "Doe", " ");
        UserDto dto = new UserDto(person, " ", " ");

        // when
        Mockito.when(userService.create(dto)).thenReturn(Mono.error(new CustomBadRequestException("")));

        // then
        client
                .post()
                .uri("/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Email address is required. Email address must be at least 6 characters in length. Email address is not valid. First name is required. Surname is required. First name must be at least 3 characters in length. Surname must be at least 3 characters in length. Role id is required. Role id must be at least 1 characters in length.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenMaxLengthValidationFails() {
        // given
        Person person = new Person("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");
        UserDto dto = new UserDto(person, "person@gmail.com", "1");

        // when
        Mockito.when(userService.create(dto)).thenReturn(Mono.error(new CustomBadRequestException("")));

        // then
        client
                .post()
                .uri("/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("First name must be at most 25 characters in length. First name must be at most 40 characters in length. Surname must be at most 25 characters in length.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsCustomBadRequestException_status400_whenEmailValidationFails() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, "abcdefghijklmnopqrstuvwxyz", "1");

        // when
        Mockito.when(userService.create(dto)).thenReturn(Mono.error(new CustomBadRequestException("")));

        // then
        client
                .post()
                .uri("/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Email address is not valid.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    void update_returnsUnauthorized_status401_whenUserIsNotAuthenticated() {
        // given
        String id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsUpdatedUser_status200_whenSuccessful() {
        // given
        String id = "1";
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        UpdateUserDto dto = new UpdateUserDto(person, emailAddress, roleId, true);
        User user = new User(id, person, emailAddress, roleId, null, false, false,
                false, true, LocalDateTime.now(), null, LocalDateTime.now() ,null);

        //when
        Mockito.when(userService.update(id, dto)).thenReturn(Mono.just(user));

        // then
        client
                .put()
                .uri("/v1/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("User updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.person").isNotEmpty()
                .jsonPath("$.data.person.firstName").isEqualTo("John")
                .jsonPath("$.data.person.otherNames").isEqualTo("Doe")
                .jsonPath("$.data.person.surname").isEqualTo("Doe")
                .jsonPath("$.data.emailAddress").isEqualTo(emailAddress)
                .jsonPath("$.data.roleId").isEqualTo(roleId)
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsBadRequest_status400_whenRequiredValidationFails() {
        // given
        String id = "1";
        Person person = new Person(null, "Doe", null);
        UserDto dto = new UserDto(person, null, null);

        // when
        Mockito.when(userService.create(dto)).thenReturn(Mono.error(new CustomBadRequestException("")));

        // then
        client
                .put()
                .uri("/v1/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Email address is required. First name is required. Surname is required. Role id is required. IsEnabled is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsBadRequest_status400_whenMinLengthValidationFails() {
        // given
        String id = "1";
        Person person = new Person(" ", "Doe", " ");
        UserDto dto = new UserDto(person, " ", " ");

        // when
        Mockito.when(userService.create(dto)).thenReturn(Mono.error(new CustomBadRequestException("")));

        // then
        client
                .put()
                .uri("/v1/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Email address is required. Email address must be at least 6 characters in length. Email address is not valid. First name is required. Surname is required. First name must be at least 3 characters in length. Surname must be at least 3 characters in length. Role id is required. Role id must be at least 1 characters in length. IsEnabled is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsBadRequest_status400_whenMaxLengthValidationFails() {
        // given
        String id = "1";
        Person person = new Person("abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz", "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqrstuvwxyz");
        UserDto dto = new UserDto(person, "person@gmail.com", "1");

        // when
        Mockito.when(userService.create(dto)).thenReturn(Mono.error(new CustomBadRequestException("")));

        // then
        client
                .put()
                .uri("/v1/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("First name must be at most 25 characters in length. First name must be at most 40 characters in length. Surname must be at most 25 characters in length. IsEnabled is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsBadRequest_status400_whenEmailValidationFails() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, "abcdefghijklmnopqrstuvwxyz", "1");

        // when
        Mockito.when(userService.create(dto)).thenReturn(Mono.error(new CustomBadRequestException("")));

        // then
        client
                .put()
                .uri("/v1/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UserDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Email address is not valid. IsEnabled is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    void findAll_returnsUnauthorized_status401_whenUserIsNotAuthorised() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/users")
                        .queryParam("id", 1)
                        .queryParam("name", "name")
                        .queryParam("order", OrderType.ASC)
                        .build();
        client
                .put()
                .uri(uriFunc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsUsersList_status200_whenSuccessful() {
        // given
        String id = "1";
        LocalDateTime now = LocalDateTime.now();
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null, null ,null);
        OrderType order = OrderType.ASC;

        // when
        Mockito.when(userService
                .findAll(emailAddress, order))
                .thenReturn(Flux.just(user));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/users")
                        .queryParam("emailAddress", emailAddress)
                        .queryParam("order", order)
                        .build();
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Users found successfully.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].emailAddress").isEqualTo(emailAddress)
                .jsonPath("$.data.[0].person").isNotEmpty()
                .jsonPath("$.data.[0].person.firstName").isEqualTo("John")
                .jsonPath("$.data.[0].person.otherNames").isEqualTo("Doe")
                .jsonPath("$.data.[0].person.surname").isEqualTo("Doe")
                .jsonPath("$.data.[0].roleId").isEqualTo(roleId)
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .jsonPath("$.data.[0].modifiedOn").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsEmpty_status200_whenUsersWereNotFound() {
        // given
        String emailAddress = "person@gmail.com";
        String order = OrderType.ASC.name();

        // when
        Mockito.when(userService.findAll(emailAddress, OrderType.valueOf(order))).thenReturn(Flux.empty());

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/users")
                        .queryParam("emailAddress", emailAddress)
                        .queryParam("order", order)
                        .build();
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.message").isEqualTo("Users were not found!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

    }

    @Test
    void deleteById_returnsUnauthorized_status401_whenUserIsNotAuthenticated() {
        // given
        String id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void deleteById_returnsTrue_status200() {
        // given
        String id = "1";

        // when
        Mockito.when(userService.deleteById(id))
                .thenReturn(Mono.just(true));

        // then
        client
                .delete()
                .uri("/v1/users/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("User with id %s deleted successfully.".formatted(id))
                .consumeWith(System.out::println);
    }

    @Test
    void changePassword_returnsUnauthorized_status401_whenUserIsUnauthenticated() {
        // given
        String id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());

        // then
        client
                .put()
                .uri("/v1/users/{id}/password", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void changePassword_returnsBadRequest_whenPasswordsFailValidation() {
    	// given
        String id = "1";

        // then
        PasswordDto passwordsNotValid = new PasswordDto("pass!123", "pass@1234");
        client
                .put()
                .uri("/v1/users/{id}/password", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(passwordsNotValid), PasswordDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Current password is not valid. New password is not valid.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void changePassword_returnsUser_whenPasswordChangedSuccessfully() {
        // given
        String id = "1";
        PasswordDto dto = new PasswordDto("pass!123@Pass", "pass@1234!Pass");
        LocalDateTime now = LocalDateTime.now();
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null, null ,null);

        // when
        when(userService.changePassword(id, dto)).thenReturn(Mono.just(user));

        // then
//        String uri = "/v1/users/%s/password".formatted(id);
        client
                .put()
                .uri("/v1/users/{id}/password", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PasswordDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("User password updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .consumeWith(System.out::println);
    }
}