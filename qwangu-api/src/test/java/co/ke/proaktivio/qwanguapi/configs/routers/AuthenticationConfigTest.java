package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.AuthenticationHandler;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
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
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Password is required. Password must be at least 6 characters in length.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }
}
