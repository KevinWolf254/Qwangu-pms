package co.ke.proaktivio.qwanguapi.services.implementations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.repositories.AuthorityRepository;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuthorityServiceImplIntegrationTest {
	@Autowired
	private AuthorityRepository authorityRepository;
	@Autowired
	private AuthorityService underTest;

	@MockBean
	private BootstrapConfig bootstrapConfig;
	@MockBean
	private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(
			DockerImageName.parse("mongo:latest"));

	@DynamicPropertySource
	public static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
	}

	@Test
	void find_returnsEmpty_whenNonExist() {
		// when
		Flux<Authority> findNonExist = authorityRepository.deleteAll().thenMany(underTest.findAll(null, null));
		// then
		StepVerifier.create(findNonExist).expectComplete().verify();
	}

	@Test
	void findAll_returnsAuthority_whenSuccessful() {
		// given
		var authority = new Authority.AuthorityBuilder().name("USERS").build();
		var authority2 = new Authority.AuthorityBuilder().name("DASHBOARD").build();
		// when
		Flux<Authority> allAuthorities = authorityRepository.deleteAll().thenMany(Flux.just(authority, authority2))
				.flatMap(a -> authorityRepository.save(a)).doOnNext(a -> System.out.println("--- Created: " + a))
				.thenMany(underTest.findAll(null, null));
		// then
		StepVerifier.create(allAuthorities)
				.expectNextMatches(a -> a.getId() != null && a.getName().equals("DASHBOARD") && a.getCreatedOn() != null
						&& a.getCreatedBy().equals("SYSTEM") && a.getModifiedOn() != null
						&& a.getModifiedBy().equals("SYSTEM"))
				.expectNextMatches(a -> a.getId() != null && a.getName().equals("USERS") && a.getCreatedOn() != null
						&& a.getCreatedBy().equals("SYSTEM") && a.getModifiedOn() != null
						&& a.getModifiedBy().equals("SYSTEM"))
				.verifyComplete();
	}

}
