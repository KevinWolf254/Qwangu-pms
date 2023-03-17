package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.handlers.AuthenticationHandler;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.EmailDto;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.pojos.ResetPasswordDto;
import co.ke.proaktivio.qwanguapi.pojos.SignInDto;
import co.ke.proaktivio.qwanguapi.pojos.TokenDto;
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
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {AuthenticationConfig.class, AuthenticationHandler.class, SecurityConfig.class, GlobalErrorConfig.class,
        GlobalErrorWebExceptionHandler.class})
public class AuthenticationConfigTest {
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
                .uri("/v1/sign-in")
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
    @DisplayName("signIn returns BadRequestException when password is empty status 400")
    void signIn_returnsBadRequestException_whenPasswordIsEmpty_status400() {
        // given
        SignInDto dto = new SignInDto("person@gmail.com", " ");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/sign-in")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), SignInDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Password is required. Password must be at least 6 characters in length.")
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
                .uri("/v1/sign-in")
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
                .uri("/v1/sign-in")
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
    void sendForgotPasswordEmail_returnsSuccess_status200_whenEmailAddressDoesNotExists_returnsCustomBadRequestException() {
        // given
        var emailDto = new EmailDto("person@gmail.com");
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(userService.sendForgotPasswordEmail(emailDto)).thenReturn(Mono.error(new CustomBadRequestException("Email address %s could not be found!".formatted(emailDto.getEmailAddress()))));

        // then
        client
                .post()
                .uri("/v1/forgot-password")
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
  void sendForgotPasswordEmail_returnsSuccess_status200_whenEmailAddressExists() {
      // given
      var emailDto = new EmailDto("person@gmail.com");
      // when
      when(contextRepository.load(any())).thenReturn(Mono.empty());
      when(userService.sendForgotPasswordEmail(emailDto)).thenReturn(Mono.empty());

      // then
      client
              .post()
              .uri("/v1/forgot-password")
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
    void createPassword_returnsUser_status200_whenSuccessful() {
    	// given
        String password = "qwerty@123Man";
        var resetPasswordDto = new ResetPasswordDto(password);
        var token = UUID.randomUUID().toString();
        
        String id = "1";
        String roleId = "1";
        var now = LocalDateTime.now();
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        var user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null, null ,null);
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(userService.resetPassword(token, resetPasswordDto.getPassword())).thenReturn(Mono.just(user));

        // then
    	Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/password")
                .queryParam("token", token)
                .build();
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
    }

    @Test
    void createPassword_returnsBadRequest_status400_whenTokenIsNullOrEmpty() {
    	// given
        String password = "qwerty@123Man";
        var resetPasswordDto = new ResetPasswordDto(password);
        
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());

        // then
        client
                .post()
                .uri("/v1/password")
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
    }

    @Test
    void createPassword_returnsBadRequest_status400_whenPasswordIsNull() {
    	// given
        var token = UUID.randomUUID().toString();
        
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());

        // then
    	  Function<UriBuilder, URI> uriFunc = uriBuilder ->
          uriBuilder
                  .path("/v1/password")
                  .queryParam("token", token)
                  .build();
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
    }
    
    @Test
    @DisplayName("setFirstTimePassword returns success when user is not authenticated status 200")
    void createPassword_returnsBadRequest_status400_whenPasswordFailsValidation() {
        // given
        var token = UUID.randomUUID().toString();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/password")
                .queryParam("token", token)
                .build();
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
    void activate_returnsBadRequest_status400_whenTokenIsNotProvided() {
    	// given
    	var token = "";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/activate")
                .queryParam("token", token)
                .build();
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
    }
    @Test
    void activate_returnsUser_status200_whenActivationIsSuccessful() {
    	// given
        String uuid = UUID.randomUUID().toString();

        String id = "1";
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        LocalDateTime now = LocalDateTime.now();
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, emailAddress, roleId, null, false, false, false, true, now, null, null ,null);

        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(userService.activateByToken(uuid)).thenReturn(Mono.just(user));

        // then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/activate")
                .queryParam("token", uuid)
                .build();
        // when
        client
                .get()
                .uri(uriFunc)
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
