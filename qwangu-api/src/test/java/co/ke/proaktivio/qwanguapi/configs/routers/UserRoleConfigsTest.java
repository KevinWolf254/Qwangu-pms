package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.UserRoleHandler;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
import co.ke.proaktivio.qwanguapi.services.UserRoleService;
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
import java.util.HashSet;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {UserRoleConfigs.class, UserRoleHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class UserRoleConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private UserRoleService userRoleService;

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
		        .uri("/v1/roles")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
	@Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
	void create_returnsBadRequest_status400_whenNameIsNull() {
    	// given
    	var userRoleDto = new UserRoleDto();    	
    	// then
        client
	        .post()
	        .uri("/v1/roles")
	        .body(Mono.just(userRoleDto), UserRoleDto.class)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
            .expectStatus().isBadRequest()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$.success").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("Name is required.")
            .jsonPath("$.data").isEmpty()
            .consumeWith(System.out::println);
    }
    
	@Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
	void create_returnsBadRequest_status400_whenNameLengthIsLessThanRequired() {
    	// given
    	var userRoleDto = new UserRoleDto(); 
    	userRoleDto.setName("H");
    	// then
        client
	        .post()
	        .uri("/v1/roles")
	        .body(Mono.just(userRoleDto), UserRoleDto.class)
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
	void create_returnsBadRequest_status400_whenNameLengthIsMoreThanRequired() {
    	// given
    	var userRoleDto = new UserRoleDto(); 
    	userRoleDto.setName("HWERRROPTOTKTJYIGJGNGMGKTOTWPOWKDNSOPORRKRORJRNFJFIKFORIRFIRRIRJRIRJRJRIRJRIRIRSSKSDIDIPQJQ");
    	// then
        client
	        .post()
	        .uri("/v1/roles")
	        .body(Mono.just(userRoleDto), UserRoleDto.class)
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
    
    @SuppressWarnings("serial")
	@Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsUserRole_status200_whenSuccessful() {
    	// given
    	var name ="ADMIN";
    	var userRoleDto = new UserRoleDto();
    	userRoleDto.setName(name);
    	userRoleDto.setAuthorities(new HashSet<>() {{
    		var authority = new UserAuthorityDto();
    		authority.setName("DASHBOARD");
    		authority.setRead(true);
    		add(authority);
    	}});
    	var userRoleId ="12345";
    	var userRole = new UserRole();
    	userRole.setId(userRoleId);
    	userRole.setName(name);
    	userRole.setCreatedBy("SYSTEM");
    	LocalDateTime now = LocalDateTime.now();
		userRole.setCreatedOn(now);
    	userRole.setModifiedBy("SYSTEM");
    	userRole.setModifiedOn(now);
    	// when
    	when(userRoleService.create(userRoleDto)).thenReturn(Mono.just(userRole));
    	// then

        client
	        .post()
	        .uri("/v1/roles")
	        .body(Mono.just(userRoleDto), UserRoleDto.class)
	        .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("UserRole created successfully.")
            .jsonPath("$.data").isNotEmpty()
            .jsonPath("$.data.id").isEqualTo(userRoleId)
            .jsonPath("$.data.name").isEqualTo(name)
            .jsonPath("$.data.createdOn").isNotEmpty()
            .jsonPath("$.data.createdBy").isEqualTo("SYSTEM")
            .jsonPath("$.data.modifiedOn").isNotEmpty()
            .jsonPath("$.data.modifiedBy").isEqualTo("SYSTEM")
            .consumeWith(System.out::println);
    }

    @Test
    void findById_returnsUnauthorized_status401_whenUserIsNotAuthenticated() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
		        .get()
		        .uri("/v1/roles/{roleId}", "12345")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsNotFound_whenRoleDoesNotExist() {
    	// given
    	var userRoleId ="1234";
    	// when
    	when(userRoleService.findById(userRoleId)).thenReturn(Mono.empty());
    	// then
        client
	        .get()
	        .uri("/v1/roles/{roleId}", userRoleId)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
	        .expectStatus().isNotFound()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$.success").isEqualTo(false)
            .jsonPath("$.message").isEqualTo("UserRole with id %s was not found!".formatted(userRoleId))
            .jsonPath("$.data").isEmpty()
            .consumeWith(System.out::println);    	
    }
    
    @SuppressWarnings("serial")
	@Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnUserRole_status200_whenUserRoleIdExists() {
    	// given
    	var name ="ADMIN";
    	var userRoleDto = new UserRoleDto();
    	userRoleDto.setName(name);
    	userRoleDto.setAuthorities(new HashSet<>() {{
    		var authority = new UserAuthorityDto();
    		authority.setName("DASHBOARD");
    		authority.setRead(true);
    		add(authority);
    	}});
    	var userRoleId ="12345";
    	var userRole = new UserRole();
    	userRole.setId(userRoleId);
    	userRole.setName(name);
    	userRole.setCreatedBy("SYSTEM");
    	LocalDateTime now = LocalDateTime.now();
		userRole.setCreatedOn(now);
    	userRole.setModifiedBy("SYSTEM");
    	userRole.setModifiedOn(now);
    	// when
    	when(userRoleService.findById(userRoleId)).thenReturn(Mono.just(userRole));
    	// then

        client
	        .get()
	        .uri("/v1/roles/{roleId}", userRoleId)
	        .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType("application/json")
            .expectBody()
            .jsonPath("$").isNotEmpty()
            .jsonPath("$.success").isEqualTo(true)
            .jsonPath("$.message").isEqualTo("UserRole found successfully.")
            .jsonPath("$.data").isNotEmpty()
            .jsonPath("$.data.id").isEqualTo(userRoleId)
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
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/roles")
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
    void findAll_returnsRoleList_status200_whenSuccessful() {
        // given
        String name = "ADMIN";
        LocalDateTime now = LocalDateTime.now();
		String system = "SYSTEM";
		var role = new UserRole.UserRoleBuilder()
				.name("ADMIN")
				.build();
		role.setId("1");
		role.setCreatedOn(now);
		role.setCreatedBy(system);
		role.setModifiedOn(now);
		role.setModifiedBy(system);
		
        OrderType order = OrderType.ASC;

        // when
        Mockito.when(userRoleService.findAll(name, order)).thenReturn(Flux.just(role));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/roles")
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
                .jsonPath("$.message").isEqualTo("UserRoles found successfully.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].name").isEqualTo(name)
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .jsonPath("$.data.[0].createdBy").isEqualTo(system)
                .jsonPath("$.data.[0].modifiedOn").isNotEmpty()
                .jsonPath("$.data.[0].modifiedBy").isEqualTo(system)
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsEmpty_status200_whenUserRoleDoesNotExist() {
        // given
        String name = "ADMIN";
        OrderType asc = OrderType.ASC;
		String order = asc.name();

        // when
        Mockito.when(userRoleService.findAll(name, asc)).thenReturn(Flux.empty());

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/roles")
                        .queryParam("name", name)
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
                .jsonPath("$.message").isEqualTo("UserRoles were not found!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

    }
}