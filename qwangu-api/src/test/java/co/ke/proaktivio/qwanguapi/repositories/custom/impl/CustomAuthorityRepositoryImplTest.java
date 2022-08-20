package co.ke.proaktivio.qwanguapi.repositories.custom.impl;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
@SpringBootTest
class CustomAuthorityRepositoryImplTest {

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Autowired
    private ReactiveMongoTemplate template;

    @Autowired
    private CustomAuthorityRepositoryImpl customAuthorityRepository;


    @Test
    @DisplayName("findPaginated returns a flux of authorities when successful")
    void findPaginated_ReturnsFluxOfAuthorities_WhenSuccessful() {
        // given

        //when
        Flux<Authority> saved = Flux.just(new Authority(null, "USERS", true, true, true,
                                true, true, "1", LocalDateTime.now(), null),
                        new Authority(null, "APARTMENTS", true, true, true, true,
                                true, "1", LocalDateTime.now(), null))
                .flatMap(a -> template.save(a, "AUTHORITY"))
                .thenMany(customAuthorityRepository.findPaginated(Optional.empty(),
                        Optional.empty(), 1, 10,
                        OrderType.ASC));

        // then
        StepVerifier.create(saved)
                .expectNextCount(2)
                .verifyComplete();
    }

//    @Test
//    @DisplayName("findPaginated returns a CustomNotFoundException when none exist!")
//    void findPaginated_ReturnsCustomNotFoundException_WhenNoAuthoritiesExist() {
//        // given
//
//        //when
//        Flux<Authority> saved = template.dropCollection(Authority.class)
//                .doOnSuccess(e -> System.out.println("----Dropped authority table successfully!"))
//                .thenMany(customAuthorityRepository.findPaginated(Optional.empty(),
//                        Optional.empty(), 1, 10,
//                        OrderType.ASC))
//                .doOnSubscribe(a -> System.out.println("----Found no authorities!"));
//
//        // then
//        StepVerifier
//                .create(saved)
//                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
//                        e.getMessage().equalsIgnoreCase("Authorities were not found!"))
//                .verify();
//    }

}