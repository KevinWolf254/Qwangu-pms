package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.handlers.UserHandler;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.ErrorCode;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.pojos.UserDto;
import co.ke.proaktivio.qwanguapi.services.UserService;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@ContextConfiguration(classes = {UserConfigs.class, UserHandler.class})
@WebFluxTest
class UserConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private UserService userService;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    @DisplayName("create returns a user when email address does not exist")
    void create_returnsUser_whenEmailAddressDoesNotExist_status201() {
        // given
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, emailAddress, roleId);
        User user = new User("1", person, emailAddress, roleId, LocalDateTime.now(), null);

        //when
        Mockito.when(userService.create(dto)).thenReturn(Mono.just(user));

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
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("create returns CustomAlreadyExistsException with status 400")
    void create_returnsCustomAlreadyExistsException_status400() {
        // given
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, emailAddress, roleId);

        // when
        Mockito.when(userService.create(dto)).thenReturn(Mono.error(new CustomAlreadyExistsException("User with email address %s already exists!".formatted(emailAddress))));

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
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("User with email address %s already exists!".formatted(emailAddress))
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("create returns CustomBadRequestException when required validation fails with status 403")
    void create_returnsCustomBadRequestException_whenRequiredValidationFails_status403() {
        // given
        Person person = new Person(null, "Doe", null);
        UserDto dto = new UserDto(person, null, null);

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
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Email address is required. First name is required. Surname is required. Role id is required.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("create returns CustomBadRequestException when min length validation fails with status 403")
    void create_returnsCustomBadRequestException_whenMinLengthValidationFails_status403() {
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
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Email address is required. Email address must be at least 6 characters in length. Email address is not valid. First name is required. Surname is required. First name must be at least 3 characters in length. Surname must be at least 3 characters in length. Role id is required. Role id must be at least 1 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("create returns CustomBadRequestException when max length validation fails with status 403")
    void create_returnsCustomBadRequestException_whenMaxLengthValidationFails_status403() {
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
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("First name must be at most 25 characters in length. First name must be at most 40 characters in length. Surname must be at most 25 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("create returns CustomBadRequestException when email address validation fails with status 403")
    void create_returnsCustomBadRequestException_whenEmailValidationFails_status403() {
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
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Email address is not valid.")
                .consumeWith(System.out::println);
    }
}