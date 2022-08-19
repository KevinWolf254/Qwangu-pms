package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.UserHandler;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.UserService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.mail.MailSendException;
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
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {UserConfigs.class, UserHandler.class, SecurityConfig.class})
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
    @DisplayName("create returns unauthorised when user is not authenticated status 401")
    void create_returnsUnauthorized_status401() {
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
    @WithMockUser
    @DisplayName("create returns a user when email address does not exist")
    void create_returnsUser_whenEmailAddressDoesNotExist_status201() {
        // given
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, emailAddress, roleId);
        User user = new User("1", person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), null);

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
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("create returns CustomAlreadyExistsException with status 400")
    void create_returnsCustomAlreadyExistsException_status400() {
        // given
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, emailAddress, roleId);

        // when
        Mockito.when(userService.createAndNotify(dto)).thenReturn(Mono.error(new CustomAlreadyExistsException("User with email address %s already exists!".formatted(emailAddress))));

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
                .jsonPath("$.data").isEqualTo("User with email address %s already exists!".formatted(emailAddress))
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
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
                .jsonPath("$.data").isEqualTo("Email address is required. First name is required. Surname is required. Role id is required.")
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
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
                .jsonPath("$.data").isEqualTo("Email address is required. Email address must be at least 6 characters in length. Email address is not valid. First name is required. Surname is required. First name must be at least 3 characters in length. Surname must be at least 3 characters in length. Role id is required. Role id must be at least 1 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
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
                .jsonPath("$.data").isEqualTo("First name must be at most 25 characters in length. First name must be at most 40 characters in length. Surname must be at most 25 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
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
                .jsonPath("$.data").isEqualTo("Email address is not valid.")
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("create returns MailException with status 400")
    void create_returnsMailException_status400() {
        // given
        String roleId = "1";
        String emailAddress = "a@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, emailAddress, roleId);

        // when
        Mockito.when(userService.createAndNotify(dto)).thenReturn(Mono.error(new MailSendException("")));

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
                .jsonPath("$.data").isEqualTo("Mail could not be sent!")
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("create returns Exception with status 500")
    void create_returnsException_status500() {
        // given
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, "person@gmail.com", "1");

        //when
        Mockito.when(userService.create(dto)).thenThrow(new RuntimeException("Something happened!"));

        //then
        client
                .post()
                .uri("/v1/users")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.toString())
                .jsonPath("$.data").isEqualTo("Something happened!")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns unauthorised when user is not authenticated status 401")
    void update_returnsUnauthorized_status401() {
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
    @WithMockUser
    @DisplayName("update returns a Mono of updated User when id and email address are paired")
    void update_returnsUpdatedUser_status200() {
        // given
        String id = "1";
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, emailAddress, roleId);
        User user = new User(id, person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), LocalDateTime.now());

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
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("update returns CustomBadRequestException when required validation fails with status 403")
    void update_returnsCustomBadRequestException_whenRequiredValidationFails_status403() {
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
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Email address is required. First name is required. Surname is required. Role id is required.")
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("update returns CustomBadRequestException when min length validation fails with status 403")
    void update_returnsCustomBadRequestException_whenMinLengthValidationFails_status403() {
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
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Email address is required. Email address must be at least 6 characters in length. Email address is not valid. First name is required. Surname is required. First name must be at least 3 characters in length. Surname must be at least 3 characters in length. Role id is required. Role id must be at least 1 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("update returns CustomBadRequestException when max length validation fails with status 403")
    void update_returnsCustomBadRequestException_whenMaxLengthValidationFails_status403() {
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
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("First name must be at most 25 characters in length. First name must be at most 40 characters in length. Surname must be at most 25 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("update returns CustomBadRequestException when email address validation fails with status 403")
    void update_returnsCustomBadRequestException_whenEmailValidationFails_status403() {
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
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Email address is not valid.")
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("update returns Exception with status 500")
    void update_returnsException_status500() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        UserDto dto = new UserDto(person, "person@gmail.com", "1");

        //when
        Mockito.when(userService.update(id, dto)).thenThrow(new RuntimeException("Something happened!"));

        //then
        client.put()
                .uri("/v1/users/%s".formatted(id))
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.toString())
                .jsonPath("$.data").isEqualTo("Something happened!")
                .consumeWith(System.out::println);

    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void find_returnsUnauthorized_status401() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/users")
                        .queryParam("id", 1)
                        .queryParam("name", "name")
                        .queryParam("page", 1)
                        .queryParam("pageSize", 10)
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
    @WithMockUser
    @DisplayName("Find returns a Flux of Users")
    void find_returnsFluxOfUsers_status200() {
        // given
        String id = "1";
        LocalDateTime now = LocalDateTime.now();
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null);

        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        OrderType order = OrderType.ASC;

        // when
        Mockito.when(userService
                .findPaginated(
                        Optional.of(id),
                        Optional.of(emailAddress),
                        finalPage,
                        finalPageSize,
                        order))
                .thenReturn(Flux.just(user));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/users")
                        .queryParam("id", id)
                        .queryParam("emailAddress", emailAddress)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
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
                .jsonPath("$.data.[0].created").isNotEmpty()
                .jsonPath("$.data.[0].modified").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("find returns CustomNotFoundException with status 404")
    void find_returnsCustomNotFoundException_status404() {
        // given
        String id = "1";
        String emailAddress = "person@gmail.com";
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        String order = OrderType.ASC.name();

        // when
        Mockito.when(userService.findPaginated(
                        Optional.of(id),
                        Optional.of(emailAddress),
                        finalPage,
                        finalPageSize,
                        OrderType.valueOf(order)))
                .thenReturn(Flux.error(new CustomNotFoundException("Users were not found!")));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/users")
                        .queryParam("id", id)
                        .queryParam("emailAddress", emailAddress)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", order)
                        .build();
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.NOT_FOUND_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Not found!")
                .jsonPath("$.data").isEqualTo("Users were not found!")
                .consumeWith(System.out::println);

    }

    @Test
    @WithMockUser
    @DisplayName("find returns Exception with status 500")
    void find_returnsException_status500() {
        // given
        String id = "1";
        String emailAddress = "person@gmail.com";
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");

        // when
        OrderType order = OrderType.ASC;
        Mockito.when(userService.findPaginated(
                        Optional.of(id),
                        Optional.of(emailAddress),
                        finalPage,
                        finalPageSize,
                        order))
                .thenReturn(Flux.error(new RuntimeException("Something happened!")));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/users")
                        .queryParam("id", id)
                        .queryParam("emailAddress", emailAddress)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", order)
                        .build();
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.toString())
                .jsonPath("$.data").isEqualTo("Something happened!")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Delete by id  returns unauthorised when user is not authenticated status 401")
    void deleteById_returnsUnauthorized_status401() {
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
    @WithMockUser
    @DisplayName("delete by id returns a Mono of boolean when id exists")
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
    @WithMockUser
    @DisplayName("delete by id returns CustomNotFoundException with status 404")
    void deleteById_returnsCustomNotFoundException_status404() {
        // given
        String id = "1";

        // when
        Mockito.when(userService.deleteById(id))
                .thenReturn(Mono.error(new CustomNotFoundException("User with id %s does not exist!".formatted(id))));

        // then
        client
                .delete()
                .uri("/v1/users/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.NOT_FOUND_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Not found!")
                .jsonPath("$.data").isEqualTo("User with id %s does not exist!".formatted(id))
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser
    @DisplayName("delete by id returns Exception with status 500")
    void deleteById_returnsException_status500() {
        // given
        String id = "1";

        // when
        Mockito.when(userService.deleteById(id))
                .thenReturn(Mono.error(new RuntimeException("Something happened!")));

        // then
        client
                .delete()
                .uri("/v1/users/{id}", id)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.toString())
                .jsonPath("$.data").isEqualTo("Something happened!")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("signIn returns a jwt when a user exists")
    void signIn_returnsAJwt_whenUserExists_status200() {
        // given
        String password = "QwwsefRgvt_@er23";
        String emailAddress = "person@gmail.com";

        SignInDto dto = new SignInDto(emailAddress, password);
        var token = new TokenDto("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c");

        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(userService.signIn(dto)).thenReturn(Mono.just(token));

        // then
        client
                .post()
                .uri("/v1/signIn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), SignInDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Signed in successfully.")
                .jsonPath("$.data").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("signIn returns BadRequestException when username is null status 400")
    void signIn_returnsBadRequestException_whenUsernameIsNull_status400() {
        // given
        String password = "QwwsefRgvt_@er23";
        SignInDto dto = new SignInDto(null, password);
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/signIn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), SignInDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Username is required.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("signIn returns BadRequestException when username is empty status 400")
    void signIn_returnsBadRequestException_whenUsernameIsEmpty_status400() {
        // given
        String password = "QwwsefRgvt_@er23";
        SignInDto dto = new SignInDto(" ", password);
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/signIn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), SignInDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Username is required. Username must be at least 6 characters in length. Username is not a valid email address.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("signIn returns BadRequestException when password is null status 400")
    void signIn_returnsBadRequestException_whenPasswordIsNull_status400() {
        // given
        SignInDto dto = new SignInDto("person@gmail.com", null);
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/signIn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), SignInDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Password is required.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("signIn returns BadRequestException when password is empty status 400")
    void signIn_returnsBadRequestException_whenPasswordIsEmpty_status400() {
        // given
        SignInDto dto = new SignInDto("person@gmail.com", " ");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/signIn")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), SignInDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Password is required. Password must be at least 6 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("changePassword returns unauthorised when user is not authenticated status 401")
    void changePassword_returnsUnauthorized_status401() {
        // given
        String id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());

        // then
        client
                .get()
                .uri("/v1/users/{id}/changePassword", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    void changePassword() {
        // given
        String id = "1";
        PasswordDto dto = new PasswordDto("pass!123@Pass", "pass@1234!Pass");
        LocalDateTime now = LocalDateTime.now();
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null);

        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(userService.changePassword(id, dto)).thenReturn(Mono.just(user));

        // then
        String uri = "/v1/users/%s/changePassword".formatted(id);
        client
                .post()
                .uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PasswordDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("User updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .consumeWith(System.out::println);

        // then
        PasswordDto passwordsNotValid = new PasswordDto("pass!123", "pass@1234");
        client
                .post()
                .uri("/v1/users/{id}/changePassword", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(passwordsNotValid), PasswordDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data").isEqualTo("Current password is not valid. New password is not valid.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("activate returns unauthorised when user is not authenticated status 401")
    void activate_returnsUnauthorized_status401() {
        // given
        String id = "1";
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/users/%s/activate".formatted(id))
                        .queryParam("token", "")
                        .build();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());

        // then
        client
                .get()
                .uri(uriFunc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    void activate() {
        // given
        String id = "1";
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/users/%s/activate".formatted(id))
                        .queryParam("token", "")
                        .build();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());

        // then
        client
                .get()
                .uri(uriFunc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Token is required!")
                .consumeWith(System.out::println);

        // given
        String uuid = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null);
        Function<UriBuilder, URI> uriFunc2 = uriBuilder ->
                uriBuilder
                        .path("/v1/users/%s/activate".formatted(id))
                        .queryParam("token", uuid)
                        .build();
        // when
        when(userService.activate(uuid, id)).thenReturn(Mono.just(user));

        // then
        client
                .get()
                .uri(uriFunc2)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("User updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("sendResetPassword returns success when user is not authenticated status 200")
    void sendResetPassword_returnsSuccess_status200() {
        // given
        var emailDto = new EmailDto("person@gmail.com");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(userService.sendResetPassword(emailDto)).thenReturn(Mono.empty());

        // then
        client
                .post()
                .uri("/v1/users/sendResetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(emailDto), EmailDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Email for password reset will be sent if email address exists.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    void sendResetPassword() {
        // given
        var emailDto = new EmailDto("person.com");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/users/sendResetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(emailDto), EmailDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Email address is not valid.")
                .consumeWith(System.out::println);

        // given
        var emailDto1 = new EmailDto("");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/users/sendResetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(emailDto1), EmailDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Email address is required. Email address must be at least 6 characters in length. Email address is not valid.")
                .consumeWith(System.out::println);

        // given
        var emailDto2 = new EmailDto(null);
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/users/sendResetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(emailDto2), EmailDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Email address is required.")
                .consumeWith(System.out::println);
        // given
        var emailDto3 = new EmailDto("person@gmail.com");
        // when
        when(userService.sendResetPassword(emailDto3)).thenReturn(Mono.error(new CustomNotFoundException("")));

        // then
        client
                .post()
                .uri("/v1/users/sendResetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(emailDto3), EmailDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Email for password reset will be sent if email address exists.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("resetPassword returns success when user is not authenticated status 200")
    void resetPassword_returnsSuccess_status200() {
        // given
        String id = "1";
        String password = "qwerty@123Man";
        var resetPasswordDto = new ResetPasswordDto(password);
        var token = UUID.randomUUID().toString();
        var now = LocalDateTime.now();
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        var user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null);
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/resetPassword")
                        .queryParam("token", token)
                        .build();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(userService.resetPassword(token, resetPasswordDto.getPassword())).thenReturn(Mono.just(user));

        // then
        client
                .post()
                .uri(uriFunc)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(resetPasswordDto), ResetPasswordDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("User password updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .consumeWith(System.out::println);

        // then
        client
                .post()
                .uri("/v1/resetPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(resetPasswordDto), ResetPasswordDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Token is required!")
                .consumeWith(System.out::println);

        // then
        client
                .post()
                .uri(uriFunc)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new ResetPasswordDto()), ResetPasswordDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Password is required.")
                .consumeWith(System.out::println);

        // then
        client
                .post()
                .uri(uriFunc)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(new ResetPasswordDto("pass")), ResetPasswordDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Password is not valid.")
                .consumeWith(System.out::println);
    }
}