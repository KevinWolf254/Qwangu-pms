package co.ke.proaktivio.qwanguapi.services.implementations;

import java.util.UUID;

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
import co.ke.proaktivio.qwanguapi.models.UserToken;
import co.ke.proaktivio.qwanguapi.pojos.UserTokenDto;
import co.ke.proaktivio.qwanguapi.repositories.UserTokenRepository;
import co.ke.proaktivio.qwanguapi.services.UserTokenService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserTokenServiceImplIntegrationTest {
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private UserTokenService underTest;
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

	private final String emailAddress = "johnDoe@company.co.ke";
	private final String token = UUID.randomUUID().toString();

	private Mono<Void> reset() {
		return userTokenRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all UserTokens!"));
	}
	@Test
	void create_returnsUserToken_whenSuccessful() {
		// given
		var dto = new UserTokenDto();
		dto.setEmailAddress(emailAddress);
		dto.setToken(token);
		// when
		Mono<UserToken> create = reset().then(underTest.create(dto));
		// then
		StepVerifier
			.create(create)
			.expectNextMatches(ut -> !ut.getId().isEmpty() && ut.getEmailAddress().equals(emailAddress)
					&& ut.getToken().equals(token) && ut.getFirstSignIn() != null && ut.getLastSignIn() != null)
			.verifyComplete();
	}

	@Test
	void exists_returnsTrue_whenUserTokenExists() {
		// given
		// when
		create_returnsUserToken_whenSuccessful();
		Mono<Boolean> exists = underTest.exists(emailAddress, token);
		// then
		StepVerifier
			.create(exists)
			.expectNextMatches(isTrue -> isTrue)
			.verifyComplete();
		
	}

	@Test
	void exists_returnsFalse_whenUserTokenDoesNotExist() {
		// given
		// when
		Mono<Boolean> exists =  reset().then(underTest.exists(emailAddress, token));
		// then
		StepVerifier
			.create(exists)
			.expectNextMatches(isTrue -> !isTrue)
			.verifyComplete();		
	}

}
