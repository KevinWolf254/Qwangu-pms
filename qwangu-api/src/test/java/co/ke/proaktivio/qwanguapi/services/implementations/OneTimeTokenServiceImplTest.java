package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.repositories.OneTimeTokenRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.services.OneTimeTokenService;

import org.junit.jupiter.api.DisplayName;
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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OneTimeTokenServiceImplTest {
    @Autowired
    private OneTimeTokenRepository oneTimeTokenRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OneTimeTokenService underTest;

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
    
    private Mono<Void> reset() {
		return oneTimeTokenRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all OneTimeTokens!"))
                .then(userRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Users!"));
	}
    
    @Test
    void create_returnCustomBadRequestException_whenUserIdDoesNotExist() {
        // given
        String userId = "1";
        // when
		Mono<OneTimeToken> create = reset()
				.then(underTest.create(userId))
				.doOnSuccess(t -> System.out.println("---- Found: " + t));
        // then
        StepVerifier
                .create(create)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("User with id %s could not be found!".formatted(userId)))
                .verify();
    }
    
    @Test
    @DisplayName("create returns a Mono of OneTimeToken when user id exists")
    void create_returnOneTimeToken_whenUserIdExists() {
        // given
        String userId = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String roleId = "1";
        User user = new User(userId, person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), null, null ,null);
        // when
		Mono<OneTimeToken> create = reset()
				.then(userRepository.save(user))
				.doOnSuccess(u -> System.out.println("---- Created: " + u))
				.then(underTest.create(userId));
        // then
        StepVerifier
                .create(create)
                .expectNextMatches(ott -> !ott.getId().isEmpty() && !ott.getToken().isEmpty() && ott.getCreatedOn() != null && ott.getUserId() != null
                && !ott.hasExpired())
                .verifyComplete();
    }

    @Test
    void findAll_returnOneTimeToken_whenTokenExists() {
        // when
    	create_returnOneTimeToken_whenUserIdExists();
    	Flux<OneTimeToken> findAll = underTest.findAll(null, null, null);
        // then
        StepVerifier
                .create(findAll)
                .expectNextMatches(ott -> !ott.getId().isEmpty() && !ott.getToken().isEmpty() && ott.getCreatedOn() != null && ott.getUserId() != null
                && !ott.hasExpired())
                .verifyComplete();
    }
    
    @Test
    void findAll_returnsEmpty_whenTokensDoNotExist() {
        // given
        String token = UUID.randomUUID().toString();
        String userId = "1";
        // when
    	Flux<OneTimeToken> findAll = reset().thenMany(underTest.findAll(token, userId, null));
        // then
        StepVerifier
                .create(findAll)
                .expectComplete();
    }
    
	@Test
	void deleteById_returnsCustomNotFoundException_whenTokenDoesNotExist() {
		// given
		String tokenId = "1";
		// when
		Mono<Void> deleteById = reset().then(underTest.deleteById(tokenId));
		// then
		StepVerifier.create(deleteById)
				.expectErrorMatches(
						e -> e instanceof CustomNotFoundException && e.getMessage().equals("Token could not be found!"))
				.verify();
	}

    @Test
    @DisplayName("deleteById returns true when token is deleted successfully")
    void deleteById_returnVoid() {
        // given
        OneTimeToken token = new OneTimeToken.OneTimeTokenBuilder().userId("1").build();
        //when
		Mono<Void> deleteById = reset().then(oneTimeTokenRepository.save(token))
				.doOnSuccess(u -> System.out.println("---- Created: " + u))
				.flatMap(ott -> underTest.deleteById(ott.getId()));
        // then
        StepVerifier
                .create(deleteById)
                .expectComplete();
    }
}