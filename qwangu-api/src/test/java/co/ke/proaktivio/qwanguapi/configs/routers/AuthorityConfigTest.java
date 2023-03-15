package co.ke.proaktivio.qwanguapi.configs.routers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.function.Function;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.AuthorityHandler;
import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {AuthorityConfig.class, AuthorityHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class AuthorityConfigTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
	private AuthorityService authorityService;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;
    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void findById_returnsUnauthorized_status401() {
        // given
        var authorityId = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/authorities/{authorityId}", authorityId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }
    
	@Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
	void findById_returnsNotFound_whenInvoiceDoesNotExist() {
		// given
		var authorityId = "1";
		// when
        when(authorityService.findById(authorityId)).thenReturn(Mono.empty());
        // then
        client
	        .get()
	        .uri("/v1/authorities/{authorityId}", authorityId)
	        .exchange()
	        .expectStatus().isNotFound()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Authority with id %s does not exist!".formatted(authorityId))
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);
	}

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsAuthority_status200_whenSuccessful() {
		// given
		var authorityId = "1";
		String name = "USERS";
		var authority = new Authority.AuthorityBuilder().name(name).build();
		authority.setId(authorityId);
		authority.setCreatedBy("SYSTEM");
		authority.setCreatedOn(LocalDateTime.now());
		authority.setModifiedBy("SYSTEM");
		authority.setModifiedOn(LocalDateTime.now());
		
		// when
        when(authorityService.findById(authorityId)).thenReturn(Mono.just(authority));

        // then
        client
        .get()
        .uri("/v1/authorities/{authorityId}", authorityId)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType("application/json")
        .expectBody()
        .jsonPath("$").isNotEmpty()
        .jsonPath("$.success").isEqualTo(true)
        .jsonPath("$.message").isEqualTo("Authority found successfully.")
        .jsonPath("$.data").isNotEmpty()
        .jsonPath("$.data.name").isEqualTo(name)
        .jsonPath("$.data.createdOn").isNotEmpty()
        .jsonPath("$.data.createdBy").isEqualTo("SYSTEM")
        .jsonPath("$.data.modifiedOn").isNotEmpty()
        .jsonPath("$.data.modifiedBy").isEqualTo("SYSTEM")
        .consumeWith(System.out::println);
        
    }

    @Test
    void findAll_returnsUnauthorized_status401_whenUserIsNotAuthenticated() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/authorities")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
	@WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsBadRequest_status400_whenOrderParamIsInvalid() {
    	// given
    	var wrongOrder = "WRONG";
    	// when
    	// then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/authorities")
                .queryParam("order", wrongOrder)
                .build();
        client
	        .get()
	        .uri(uriFunc)
	        .exchange()
	        .expectStatus().isBadRequest()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Order should be ASC or DESC!")
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println); 
    }
    
	@Test    
	@WithMockUser(roles = {"SUPER_ADMIN"})
	void findAll_returnsEmpty_status200_whenAuthoritiesDoNotExist() {
		// given
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/authorities")
                .build();
		// when
        when(authorityService.findAll(null, OrderType.DESC)).thenReturn(Flux.empty());

        // then
        client
	        .get()
	        .uri(uriFunc)
	        .exchange()
	        .expectStatus().isOk()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Authorities could not be found!")
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);        
	}
    
	@Test
	@WithMockUser(roles = {"SUPER_ADMIN"})
	void findAll_returnsAuthorities_whenSuccessful() {
		// given
		var authorityId = "1";
		String name = "USERS";
		var authority = new Authority.AuthorityBuilder().name(name).build();
		authority.setId(authorityId);
		authority.setCreatedBy("SYSTEM");
		authority.setCreatedOn(LocalDateTime.now());
		authority.setModifiedBy("SYSTEM");
		authority.setModifiedOn(LocalDateTime.now());
		
		// when
        when(authorityService.findAll(name, OrderType.DESC)).thenReturn(Flux.just(authority));
		
        // then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/authorities")
                .queryParam("name", name)
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
	        .jsonPath("$.message").isEqualTo("Authorities found successfully.")
	        .jsonPath("$.data").isNotEmpty()
	        .jsonPath("$.data.[0].name").isEqualTo(name)
	        .jsonPath("$.data.[0].createdOn").isNotEmpty()
	        .jsonPath("$.data.[0].createdBy").isEqualTo("SYSTEM")
	        .jsonPath("$.data.[0].modifiedOn").isNotEmpty()
	        .jsonPath("$.data.[0].modifiedBy").isEqualTo("SYSTEM")
	        .consumeWith(System.out::println);        
	}

}
