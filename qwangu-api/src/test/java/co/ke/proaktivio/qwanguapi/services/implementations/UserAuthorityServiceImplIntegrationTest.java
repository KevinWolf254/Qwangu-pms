package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UserAuthorityDto;
import co.ke.proaktivio.qwanguapi.repositories.UserAuthorityRepository;
import co.ke.proaktivio.qwanguapi.services.UserAuthorityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserAuthorityServiceImplIntegrationTest {
    @Autowired
    private UserAuthorityRepository userAuthorityRepository;
    @Autowired
    private ReactiveMongoTemplate template;
    @Autowired
    private UserAuthorityService userAuthorityService;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));
    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

	@Test
	void update_returnsCustomNotFoundException_whenUserAuthorityDoesNotExist() {
		// given
		var userAuthorityId = "1";
		var userAuthorityDto = new UserAuthorityDto();
		// when
		Mono<UserAuthority> updateWithCustomNotFoundException = template.dropCollection(UserAuthority.class)
				.doOnSuccess($ -> System.out.println("Dropped table UserAuthority!"))
				.then(userAuthorityService.update(userAuthorityId, userAuthorityDto));
		// then
		StepVerifier.create(updateWithCustomNotFoundException)
				.expectErrorMatches(e -> e instanceof CustomNotFoundException
						&& e.getMessage().equals("UserAuthority with id 1 does not exists!"))
				.verify();
	}

	@Test
	void update_returnsUserAuthority_whenSuccessful() {
		// given
		var userAuthorityId = "1";
		var name = "USERS";
        String userRoleId = "12345";
        
        var userAuthorityDto = new UserAuthorityDto();
        userAuthorityDto.setName(name);
        userAuthorityDto.setAuthorize(false);
        userAuthorityDto.setCreate(true);
        userAuthorityDto.setDelete(false);
        userAuthorityDto.setRead(true);
        userAuthorityDto.setUpdate(true);
		userAuthorityDto.setRoleId(userRoleId);
    	
		var userAuthority = new UserAuthority.UserAuthorityBuilder().name(name).create(true).read(true).update(true)
				.delete(true).authorize(true).roleId(userRoleId).build();
		userAuthority.setId(userAuthorityId);
		
		// when
		Mono<UserAuthority> update = template.dropCollection(UserAuthority.class)
				.doOnSuccess(e -> System.out.println("--- Dropped authority table successfully!"))
				.then(userAuthorityRepository.save(userAuthority))
				.doOnSuccess(e -> System.out.println("--- Created: " + e))
				.then(userAuthorityService.update(userAuthorityId, userAuthorityDto));
		// then
		StepVerifier
			.create(update)
			.expectNextMatches(ua -> ua.getId().equals(userAuthorityId)
					&& !ua.getAuthorize() && !ua.getDelete() 
					&& ua.getCreate() && ua.getRead() && ua.getUpdate() && ua.getRoleId().equals(userRoleId))
			.verifyComplete();

	}

	@Test
	void findAll_returnEmpty_WhenNoUserAuthoritiesExist() {
		// when
		Flux<UserAuthority> saved = template.dropCollection(UserAuthority.class)
				.doOnSuccess(e -> System.out.println("--- Dropped authority table successfully!"))
				.thenMany(userAuthorityService.findAll(null, null, OrderType.ASC));
		// then
		StepVerifier.create(saved).verifyComplete();
	}

	@Test
	void findAll_returnsUserAuthorityList_whenSuccessful() {
		// when
		Flux<UserAuthority> saved = Flux
				.just(new UserAuthority.UserAuthorityBuilder().name("USERS").create(true).read(true).update(true)
						.delete(true).authorize(true).roleId("1").build(),
						new UserAuthority.UserAuthorityBuilder().name("PROPERTIES").create(true).read(true).update(true)
								.delete(true).authorize(true).roleId("1").build())
				.flatMap(a -> userAuthorityRepository.save(a))
				.thenMany(userAuthorityService.findAll(null, null, OrderType.ASC));
		// then
		StepVerifier.create(saved).expectNextCount(2).verifyComplete();
	}

	@Test
	void deleteById_returnsCustomNotFoundException_whenUserAuthorityDoesNotExist() {
		// given
		var userAuthorityId = "12345";
		// when
		Mono<Boolean> deleteWithCustomNotFoundException = template.dropCollection(UserAuthority.class)
				.doOnSuccess($ -> System.out.println("Dropped table UserAuthority!"))
				.then(userAuthorityService.deleteById(userAuthorityId));
		// then
		StepVerifier
			.create(deleteWithCustomNotFoundException)
			.expectErrorMatches(e -> e instanceof CustomNotFoundException 
					&& e.getMessage().equals("UserAuthority with id 12345 does not exists!"))
			.verify();
	}

	@Test
	void deleteById_returnTrue_whenUserAuthorityDeletedSuccessfully() {
		// given
		var userAuthorityId = "12345";
		var userAuthority = new UserAuthority.UserAuthorityBuilder().name("USERS").create(true).read(true).update(true)
				.delete(true).authorize(true).roleId("1").build();
		userAuthority.setId(userAuthorityId);
		// when
		Mono<Boolean> delete = userAuthorityRepository.save(userAuthority)
				.doOnSuccess(ua -> System.out.println("Created: " + ua))
				.then(userAuthorityService.deleteById(userAuthorityId));
		// then
		StepVerifier.create(delete).expectNextMatches(b -> b).verifyComplete();
	}
}