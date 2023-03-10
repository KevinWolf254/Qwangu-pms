package co.ke.proaktivio.qwanguapi.handlers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.routers.AuthenticationConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.EmailDto;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.pojos.ResetPasswordDto;
import co.ke.proaktivio.qwanguapi.pojos.SignInDto;
import co.ke.proaktivio.qwanguapi.services.UserService;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {AuthenticationConfig.class, AuthenticationHandler.class, SecurityConfig.class, GlobalErrorConfig.class,
        GlobalErrorWebExceptionHandler.class})
class AuthenticationHandlerTest {
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
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Password is required.")
                .jsonPath("$.data").isEmpty()
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
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Username is required.")
                .jsonPath("$.data").isEmpty()
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
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Username is required. Username must be at least 6 characters in length. Username is not a valid email address.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    void sendForgotPasswordEmail_returnsSuccess_status200() {
        // given
        var emailDto = new EmailDto("person@gmail.com");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(userService.sendForgotPasswordEmail(emailDto)).thenReturn(Mono.empty());

        // then
        client
                .post()
                .uri("/v1/forgotPassword")
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
    @DisplayName("setFirstTimePassword returns success when user is not authenticated status 200")
    void setFirstTimePassword_returnsSuccess_status200() {
        // given
        String id = "1";
        String password = "qwerty@123Man";
        var resetPasswordDto = new ResetPasswordDto(password);
        var token = UUID.randomUUID().toString();
        var now = LocalDateTime.now();
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        var user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null, null ,null);
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/setPassword")
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
                .uri("/v1/setPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(resetPasswordDto), ResetPasswordDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Token is required!")
                .jsonPath("$.data").isEmpty()
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
                .jsonPath("$.message").isEqualTo("Password is required.")
                .jsonPath("$.data").isEmpty()
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
                .jsonPath("$.message").isEqualTo("Password is not valid.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    void setPassword() {
        // given
        var emailDto = new EmailDto("person.com");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/forgotPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(emailDto), EmailDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Email address is not valid.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

        // given
        var emailDto1 = new EmailDto("");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/forgotPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(emailDto1), EmailDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Email address is required. Email address must be at least 6 characters in length. Email address is not valid.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

        // given
        var emailDto2 = new EmailDto(null);
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/forgotPassword")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(emailDto2), EmailDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Email address is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

        // given
        var emailDto3 = new EmailDto("person@gmail.com");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(userService.sendForgotPasswordEmail(emailDto3)).thenReturn(Mono.error(new CustomNotFoundException("")));

        // then
        client
                .post()
                .uri("/v1/forgotPassword/")
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
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void activate() {
        // given
        String id = "1";
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/%s/activate".formatted(id))
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
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Token is required!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

        // given
        String uuid = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null, null ,null);
        Function<UriBuilder, URI> uriFunc2 = uriBuilder ->
                uriBuilder
                        .path("/v1/%s/activate".formatted(id))
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
}