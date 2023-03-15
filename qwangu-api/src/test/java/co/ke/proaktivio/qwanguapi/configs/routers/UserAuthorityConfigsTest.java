package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.UserAuthorityHandler;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.services.UserAuthorityService;
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
@ContextConfiguration(classes = {UserAuthorityConfigs.class, UserAuthorityHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class UserAuthorityConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private UserAuthorityService userAuthorityService;

    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void findAll_returnsUnauthorized_status401_whenUserIsNotAuthenticated() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/userAuthorities")
                        .build();
        client
                .get()
                .uri(uriFunc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsAuthorities_status200_whenSuccessful() {
        // given
        String name = "ADMIN";
        LocalDateTime now = LocalDateTime.now();
        var authority = new UserAuthority("1", name, true, true, true, true, true,
                "1", now, null, null, null);
        OrderType order = OrderType.ASC;

        // when
        Mockito.when(userAuthorityService.findAll(name, null, order)).thenReturn(Flux.just(authority));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/userAuthorities")
                        .queryParam("name", name)
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
                .jsonPath("$.message").isEqualTo("Authorities found successfully.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].name").isEqualTo(name)
                .jsonPath("$.data.[0].create").isEqualTo(true)
                .jsonPath("$.data.[0].read").isEqualTo(true)
                .jsonPath("$.data.[0].update").isEqualTo(true)
                .jsonPath("$.data.[0].delete").isEqualTo(true)
                .jsonPath("$.data.[0].authorize").isEqualTo(true)
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsSuccessFalse_status200_whenAuthoritiesDoNotExist() {
    	// given
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/userAuthorities")
                .build();
        // when
        Mockito.when(userAuthorityService.findAll(null, null, OrderType.DESC)).thenReturn(Flux.empty());
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
                .jsonPath("$.message").isEqualTo("Authorities were not found!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    void findById_returnsUnauthorized_status401_whenUserIsNotAuthenticated() {
        // given
        var userAuthorityId = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsNotFound_status404_whenUserAuthorityDoesNotExist() {
        // given
        var userAuthorityId = "1";
    	// when
    	when(userAuthorityService.findById(userAuthorityId)).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
                .jsonPath("$.message").isEqualTo("User authority with id %S was not found!".formatted(userAuthorityId))
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);    	
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsUserAuthority_status200_whenSuccessful() {
        // given
        var userAuthorityId = "1";
        String name = "ADMIN";
        var userAuthority = new UserAuthority();
        userAuthority.setId(userAuthorityId);
        userAuthority.setAuthorize(true);
        userAuthority.setCreate(true);
        userAuthority.setDelete(true);
		userAuthority.setName(name);
        userAuthority.setRead(true);
        userAuthority.setRoleId("12345");
        userAuthority.setUpdate(true);
        userAuthority.setCreatedBy("SYSTEM");
        userAuthority.setCreatedOn(LocalDateTime.now());
        userAuthority.setModifiedBy("SYSTEM");
        userAuthority.setModifiedOn(LocalDateTime.now());
    	// when
    	when(userAuthorityService.findById(userAuthorityId)).thenReturn(Mono.just(userAuthority));
        // then
        client
                .get()
                .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.message").isEqualTo("User authority found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.name").isEqualTo(name)
                .jsonPath("$.data.create").isEqualTo(true)
                .jsonPath("$.data.read").isEqualTo(true)
                .jsonPath("$.data.update").isEqualTo(true)
                .jsonPath("$.data.delete").isEqualTo(true)
                .jsonPath("$.data.authorize").isEqualTo(true)
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.createdBy").isEqualTo("SYSTEM")
                .jsonPath("$.data.modifiedOn").isNotEmpty()
                .jsonPath("$.data.modifiedBy").isEqualTo("SYSTEM")
                .consumeWith(System.out::println);
    }

    @Test
    void update_returnsUnauthorized_status401_whenUserIsNotAuthenticated() {
        // given
        var userAuthorityId = "1";
        var userAuthorityDto = new UserAuthorityDto();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
    	        .body(Mono.just(userAuthorityDto), UserAuthorityDto.class)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsBadRequest_status400_whenAllPropertiesAreNull() {
        // given
        var userAuthorityId = "1";
        var userAuthorityDto = new UserAuthorityDto();
    	// when
    	when(userAuthorityService.update(userAuthorityId, userAuthorityDto)).thenReturn(Mono.empty());
    	// then
        client
	        .put()
            .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
	        .body(Mono.just(userAuthorityDto), UserAuthorityDto.class)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$.success").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("Name is required. Create is required. Read is required. Update is required. Delete is required. Authorize is required. Role id is required.")
            .jsonPath("$.data").isEmpty()
            .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsBadRequest_status400_whenNameLengthIsLessThanRequired() {
    	// given
        var userAuthorityId = "1";
        String name = "G";
        var userAuthorityDto = new UserAuthorityDto();
        userAuthorityDto.setName(name);
        userAuthorityDto.setAuthorize(true);
        userAuthorityDto.setCreate(true);
        userAuthorityDto.setDelete(true);
        userAuthorityDto.setRead(true);
        userAuthorityDto.setUpdate(true);
        userAuthorityDto.setRoleId("12345");
    	// then
        client
	        .put()
            .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
	        .body(Mono.just(userAuthorityDto), UserAuthorityDto.class)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$.success").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("Name must be at least 3 characters in length.")
            .jsonPath("$.data").isEmpty()
            .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsBadRequest_status400_whenNameLengthIsMoreThanRequired() {
    	// given
        var userAuthorityId = "1";
        var userAuthorityDto = new UserAuthorityDto();
        userAuthorityDto.setName("HWERRROPTOTKTJYIGJGNGMGKTOTWPOWKDNSOPORRKRORJRNFJFIKFORIRFIRRIRJRIRJRJRIRJRIRIRSSKSDIDIPQJQ");
        userAuthorityDto.setAuthorize(true);
        userAuthorityDto.setCreate(true);
        userAuthorityDto.setDelete(true);
        userAuthorityDto.setRead(true);
        userAuthorityDto.setUpdate(true);
        userAuthorityDto.setRoleId("12345");
        // then

        client
	        .put()
            .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
	        .body(Mono.just(userAuthorityDto), UserAuthorityDto.class)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$.success").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("Name must be at most 50 characters in length.")
            .jsonPath("$.data").isEmpty()
            .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsUserAuthority_status200_whenSuccessful() {
        // given
        var userAuthorityId = "1";
        String name = "ADMIN";

        var userAuthorityDto = new UserAuthorityDto();
        userAuthorityDto.setName(name);
        userAuthorityDto.setAuthorize(true);
        userAuthorityDto.setCreate(true);
        userAuthorityDto.setDelete(true);
        userAuthorityDto.setRead(true);
        userAuthorityDto.setUpdate(true);
        userAuthorityDto.setRoleId("12345");
        
        var by = "SYSTEM";
        var userAuthority = new UserAuthority();
        userAuthority.setId(userAuthorityId);
        userAuthority.setAuthorize(true);
        userAuthority.setCreate(true);
        userAuthority.setDelete(true);
		userAuthority.setName(name);
        userAuthority.setRead(true);
        userAuthority.setRoleId("12345");
        userAuthority.setUpdate(true);
		userAuthority.setCreatedBy(by);
        userAuthority.setCreatedOn(LocalDateTime.now());
        userAuthority.setModifiedBy(by);
        userAuthority.setModifiedOn(LocalDateTime.now());

    	// when
    	when(userAuthorityService.update(userAuthorityId, userAuthorityDto)).thenReturn(Mono.just(userAuthority));
    	// then
        client
	        .put()
            .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
	        .body(Mono.just(userAuthorityDto), UserAuthorityDto.class)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
            .jsonPath("$.message").isEqualTo("UserAuthority updated successfully.")
            .jsonPath("$.data").isNotEmpty()
            .jsonPath("$.data.id").isEqualTo(userAuthorityId)
            .jsonPath("$.data.name").isEqualTo(name)
            .jsonPath("$.data.create").isEqualTo(true)
            .jsonPath("$.data.read").isEqualTo(true)
            .jsonPath("$.data.update").isEqualTo(true)
            .jsonPath("$.data.delete").isEqualTo(true)
            .jsonPath("$.data.authorize").isEqualTo(true)
            .jsonPath("$.data.createdOn").isNotEmpty()
            .jsonPath("$.data.createdBy").isEqualTo(by)
            .jsonPath("$.data.modifiedOn").isNotEmpty()
            .jsonPath("$.data.modifiedBy").isEqualTo(by)
            .consumeWith(System.out::println);
    }

    @Test
    void delete_returnsUnauthorized_status401_whenUserIsNotAuthenticated() {
        // given
        var userAuthorityId = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .delete()
                .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void delete_returnsFalse_status200_whenNotSuccessful() {
        // given
        var userAuthorityId = "1";
        // when
    	when(userAuthorityService.deleteById(userAuthorityId)).thenReturn(Mono.just(false));
        //then
        client
	        .delete()
	        .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
	        .expectStatus().isOk()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
	        .jsonPath("$.message").isEqualTo("UserAuthority with id 1 was unable to be deleted!")
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void delete_returnsTrue_status200_whenSuccessful() {
        // given
        var userAuthorityId = "1";
        // when
    	when(userAuthorityService.deleteById(userAuthorityId)).thenReturn(Mono.just(true));
        //then
        client
        .delete()
        .uri("/v1/userAuthorities/{userAuthorityId}", userAuthorityId)
        .accept(MediaType.APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectHeader().contentType("application/json")
        .expectBody()
        .jsonPath("$").isNotEmpty()
        .jsonPath("$.success").isEqualTo(true)
        .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
        .jsonPath("$.message").isEqualTo("UserAuthority with id 1 was deleted successfully.")
        .jsonPath("$.data").isEmpty()
        .consumeWith(System.out::println);
    }
}