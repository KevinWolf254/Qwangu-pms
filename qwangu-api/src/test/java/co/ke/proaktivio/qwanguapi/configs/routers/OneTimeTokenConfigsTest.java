package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.OneTimeTokenHandler;
import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.OneTimeTokenService;

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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {OneTimeTokenConfigs.class, OneTimeTokenHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class OneTimeTokenConfigsTest {
    @SuppressWarnings("unused")
	@Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private OneTimeTokenService oneTimeTokenService;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    @Test
    void create_returnsUnauthorized_status401_whenUserIsNotAuthorised() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/tokens")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsEmpty_status200_whenNoneExist() {
        // given
        Function<UriBuilder, URI> uriFunc1 = uriBuilder ->
                uriBuilder
                        .path("/v1/tokens")
                        .build();
        when(oneTimeTokenService.findAll(null, null, OrderType.DESC)).thenReturn(Flux.empty());

        // then
        client
                .get()
                .uri(uriFunc1)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.message").isEqualTo("Tokens were not found!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsTimeTokenList_status200_whenSuccessful() {
        // given
    	var userId = "1";
        var oneTimeToken = new OneTimeToken.OneTimeTokenBuilder().userId("1").build();
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/tokens")
                        .queryParam("token", oneTimeToken.getToken())
                        .queryParam("userId", userId)
                        .build();
        // when
//        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(oneTimeTokenService.findAll(oneTimeToken.getToken(), userId, OrderType.DESC)).thenReturn(Flux.just(oneTimeToken));

        // then
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Tokens found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .consumeWith(System.out::println);
    }
}
