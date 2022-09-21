package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.UserRoleService;
import org.junit.jupiter.api.DisplayName;
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
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Optional;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RoleServiceImplIntegrationTest {
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
    @DisplayName("find paginated returns a flux of roles when successful")
    void findPaginated_ReturnsFluxOfAuthorities_WhenSuccessful() {
        //when
        Flux<UserRole> saved = Flux.just(new UserRole(null, "ADMIN", LocalDateTime.now(), null, null, null),
                        new UserRole(null, "SUPERVISOR", LocalDateTime.now(), null, null, null))
                .flatMap(a -> template.save(a, "USER_ROLE"))
                .thenMany(userRoleService.findPaginated(
                        Optional.empty(),
                        1,
                        10,
                        OrderType.ASC));

        // then
        StepVerifier.create(saved)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("find paginated returns a CustomNotFoundException when none exist!")
    void findPaginated_ReturnsCustomNotFoundException_WhenNoAuthoritiesExist() {
        // given

        //when
        Flux<UserRole> saved = template.dropCollection(UserRole.class)
                .doOnSuccess(e -> System.out.println("----Dropped role table successfully!"))
                .thenMany(userRoleService.findPaginated(
                        Optional.empty(),
                        1,
                        10,
                        OrderType.ASC))
                .doOnSubscribe(a -> System.out.println("----Found no roles!"));

        // then
        StepVerifier
                .create(saved)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Roles were not found!"))
                .verify();
    }
}