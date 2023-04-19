package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.pojos.UserRoleDto;
import co.ke.proaktivio.qwanguapi.repositories.UserAuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRoleRepository;
import co.ke.proaktivio.qwanguapi.services.UserRoleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashSet;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserRoleServiceImplIntegrationTest {
	@Autowired
	private UserRoleRepository userRoleRepository;
	@Autowired
	private UserAuthorityRepository userAuthorityRepository;
	@Autowired
    private ReactiveMongoTemplate template;
    @Autowired
    private UserRoleService userRoleService;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));
    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Test
    void create_returnsCustomAlreadyExistsException_whenRoleWithSimilarNameExists() {
    	// given
    	var adminName ="ADMIN";
    	var userRole = new UserRole.UserRoleBuilder()
    			.name(adminName)
    			.build();
    	var userRoleWithSimilarName = new UserRoleDto();
    	userRoleWithSimilarName.setName(adminName);
    	// when
		Mono<UserRole> createAlreadyExists = userRoleRepository
				.deleteAll()
				.doOnSuccess($ -> System.out.println("--- Deleted all user roles!"))
				.then(userRoleRepository.save(userRole)).doOnSuccess(ur -> System.out.println("--- Created: " + ur))
				.then(userRoleService.create(userRoleWithSimilarName));
		// then
		StepVerifier
			.create(createAlreadyExists)
			.expectErrorMatches(e -> e instanceof CustomAlreadyExistsException
					&& e.getMessage().equals("Role with name %s already exists!".formatted(adminName)))
			.verify();
    }

    @SuppressWarnings("serial")
	@Test
    void create_returnsUserRole_whenSuccessful() {
    	// given
    	var adminName ="ADMIN";
    	var userRoleAdmin = new UserRoleDto();
    	userRoleAdmin.setName(adminName);
    	userRoleAdmin.setAuthorities(new HashSet<>() {{
    		var userAuthority = new UserAuthorityDto();
    		userAuthority.setName("DASHBOARD");
    		userAuthority.setCreate(true);
    		userAuthority.setRead(true);
    		userAuthority.setUpdate(true);
    		userAuthority.setDelete(true);
    		userAuthority.setAuthorize(true);
    		add(userAuthority);
    		var userAuthorityUsers = new UserAuthorityDto();
    		userAuthorityUsers.setName("USERS");
    		userAuthorityUsers.setCreate(true);
    		userAuthorityUsers.setRead(true);
    		userAuthorityUsers.setUpdate(true);
    		userAuthorityUsers.setDelete(true);
    		userAuthorityUsers.setAuthorize(true);
    		add(userAuthorityUsers);
    	}});
    	// when
		Mono<UserRole> createSuccessfully = userRoleRepository
				.deleteAll()
				.doOnSuccess($ -> System.out.println("--- Deleted all user roles!"))
				.then(userAuthorityRepository.deleteAll())
				.doOnSuccess($ -> System.out.println("--- Deleted all user authorities!"))
				.then(userRoleService.create(userRoleAdmin));
		// then
		StepVerifier
			.create(createSuccessfully)
			.expectNextMatches(ur -> ur.getId() != null && ur.getName().equals(adminName)
			&& ur.getCreatedBy().equals("SYSTEM") && ur.getModifiedBy().equals("SYSTEM")
			&& ur.getCreatedOn() != null && ur.getModifiedOn() != null)
			.verifyComplete();    	
    }

	@Test
    void create_returnsUserAuthorities_whenUserRoleCreatedSuccessful() {
		// given
		create_returnsUserRole_whenSuccessful();
		// when
		Flux<UserAuthority> findAllUserAuthorities = userAuthorityRepository.findAll(Sort.by(Order.desc("id")))
		.doOnNext(ua -> System.out.println("--- Found: " +ua));
		// then
		StepVerifier
		.create(findAllUserAuthorities)
		.expectNextMatches(ua -> ua.getId() != null && ua.getName().equals("USERS")
		&& ua.getCreate() && ua.getRead() && ua.getUpdate() && ua.getDelete() && ua.getAuthorize()
		&& ua.getCreatedBy().equals("SYSTEM") && ua.getModifiedBy().equals("SYSTEM")
		&& ua.getCreatedOn() != null && ua.getModifiedOn() != null)
		.expectNextMatches(ua -> ua.getId() != null && ua.getName().equals("DASHBOARD")
		&& ua.getCreate() && ua.getRead() && ua.getUpdate() && ua.getDelete() && ua.getAuthorize()
		&& ua.getCreatedBy().equals("SYSTEM") && ua.getModifiedBy().equals("SYSTEM")
		&& ua.getCreatedOn() != null && ua.getModifiedOn() != null)
		.verifyComplete(); 
		
	}

	@Test
	void findAll_returnsAuthoritiesList_whenSuccessful() {
		// when
		var userRoleAdmin = "ADMIN";
		var userRoleSupervisor = "SUPERVISOR";
		var admin = new UserRole.UserRoleBuilder()
				.name(userRoleAdmin)
				.build();
		var supervisor = new UserRole.UserRoleBuilder()
				.name(userRoleSupervisor)
				.build();
		
		Flux<UserRole> saved = userRoleRepository.deleteAll()
				.thenMany(Flux.just(admin, supervisor))
				.flatMap(a -> userRoleRepository.save(a))
				.thenMany(userRoleService.findAll(null, OrderType.DESC))
				.doOnNext(ur -> System.out.println("Found: " +ur));

		// then
		StepVerifier.create(saved)
		.expectNextMatches(ur -> ur.getId() != null && ur.getName().equals(userRoleSupervisor)
				&& ur.getCreatedBy().equals("SYSTEM") && ur.getModifiedBy().equals("SYSTEM")
				&& ur.getCreatedOn() != null && ur.getModifiedOn() != null)
		.expectNextMatches(ur -> ur.getId() != null && ur.getName().equals(userRoleAdmin)
				&& ur.getCreatedBy().equals("SYSTEM") && ur.getModifiedBy().equals("SYSTEM")
				&& ur.getCreatedOn() != null && ur.getModifiedOn() != null)
		.verifyComplete();
	}

	@Test
	void findAll_returnsEmpty_whenUserRolesDoNotExist() {
		// given

		// when
		Flux<UserRole> saved = template.dropCollection(UserRole.class)
				.doOnSuccess(e -> System.out.println("----Dropped role table successfully!"))
				.thenMany(userRoleService.findAll(null, OrderType.ASC))
				.doOnSubscribe(a -> System.out.println("----Found no roles!"));

		// then
		StepVerifier.create(saved).verifyComplete();
	}
}