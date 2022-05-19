package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StringUtils;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Optional;

@Testcontainers
@DataMongoTest
@ExtendWith(SpringExtension.class)
class CustomApartmentRepositoryImplIntegrationTest {

    @Container
    private static MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

    @Autowired
    private ReactiveMongoTemplate template;

    @Autowired
    private CustomApartmentRepositoryImpl customApartmentRepository;

    @Test
    @DisplayName("Create returns a Mono of Apartment when name does not exist")
    void create_ReturnMonoOfApartment_WhenSuccessful() {

        // given
        String name = "Luxury Apartment";
        ApartmentDto dto = new ApartmentDto(name);

        // when
        Mono<Apartment> saved = customApartmentRepository.create(dto);

        // then
        StepVerifier.create(saved)
                .expectNextMatches(apartment ->
                        StringUtils.hasText(apartment.getId()) &&
                                apartment.getName().equalsIgnoreCase(name) &&
                                apartment.getCreated() != null &&
                                apartment.getModified() != null)
                .verifyComplete();
    }

    @Test
    @DisplayName("Create returns a CustomAlreadyExistsException when name already exists")
    void create_ReturnsCustomAlreadyExistsException_WhenApartmentNameExists() {

        // given
        String name = "Luxury Apartments";
        ApartmentDto dto = new ApartmentDto(name);

        // when
        Mono<Apartment> saved = customApartmentRepository.create(dto)
                .thenReturn(dto)
                .flatMap(d -> customApartmentRepository.create(d));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equalsIgnoreCase("Apartment %s already exists!".formatted(name)))
                .verify();
    }

    @Test
    @DisplayName("Update returns an updated apartment when successful")
    void update_ReturnsMonoOfAnUpdatedApartment_WhenSuccessful() {
        // given
        String updatedName = "Thika road Apartments";

        //when
        Mono<Apartment> updated =
                template.dropCollection(Apartment.class)
                        .thenReturn(new ApartmentDto("Luxury Apartments"))
                        .flatMap(d -> customApartmentRepository.create(d))
                        .flatMap(d -> customApartmentRepository.update(d.getId(), new ApartmentDto(updatedName)));

        // then
        StepVerifier.create(updated)
                .expectNextMatches(result -> StringUtils.hasText(result.getId()) &&
                        result.getName().equalsIgnoreCase(updatedName))
                .verifyComplete();
    }

    @Test
    @DisplayName("Update returns a CustomNotFoundException when id does not exist")
    void update_ReturnsCustomNotFoundException_WhenIdDoesNotExist() {

        // given
        String id = "1";
        ApartmentDto dto = new ApartmentDto("Luxury Apartments");

        // when
        Mono<Apartment> saved = customApartmentRepository.update(id, dto);

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Apartment with id %s does not exists!".formatted(id)))
                .verify();
    }

    @Test
    @DisplayName("Update returns a CustomAlreadyExistsException when name already exists")
    void update_ReturnsCustomAlreadyExistsException_WhenNameAlreadyExists() {

        // given
        String name = "Luxury Apartments";
        ApartmentDto dto = new ApartmentDto(name);

        // when
        Mono<Apartment> saved = customApartmentRepository.create(dto)
                .thenReturn(new ApartmentDto("Luxury Apartments B"))
                .flatMap(a -> customApartmentRepository.create(a))
                .flatMap(a -> customApartmentRepository.update(a.getId(), dto));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equalsIgnoreCase("Apartment %s already exists!".formatted(name)))
                .verify();
    }

    @Test
    @DisplayName("FindPaginated returns a flux of apartments when successful")
    void findPaginated_ReturnsFluxOfApartments_WhenSuccessful() {
        // given

        //when
        Flux<Apartment> saved = Flux.just(new ApartmentDto("Luxury Apartments"), new ApartmentDto("Luxury Apartments B"))
                .flatMap(a -> customApartmentRepository.create(a))
                .thenMany(customApartmentRepository.findPaginated(Optional.empty(),
                        Optional.empty(), 0, 10,
                        OrderType.ASC));

        // then
        StepVerifier.create(saved)
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    @DisplayName("FindPaginated returns a CustomNotFoundException when none exist!")
    void findPaginated_ReturnsCustomNotFoundException_WhenNoApartmentsExists() {
        // given

        //when
        Flux<Apartment> saved = template.dropCollection(Apartment.class)
                .thenMany(customApartmentRepository.findPaginated(Optional.empty(),
                        Optional.empty(), 0, 10,
                        OrderType.ASC));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Apartments do not exist!"))
                .verify();
    }

    @Test
    @DisplayName("Delete returns a true when successful")
    void delete_ReturnsTrue_WhenSuccessful() {
        //given
        var dto = new ApartmentDto("Luxury Apartments");

        // when
        Mono<Boolean> deleted = customApartmentRepository.create(dto)
                .flatMap(a -> customApartmentRepository.delete(a.getId()));

        // then
        StepVerifier.create(deleted)
                .expectNextMatches(r -> r.booleanValue() == true);
    }

    @Test
    @DisplayName("Delete returns CustomNotFoundException when id does not exist")
    void delete_ReturnsCustomNotFoundException_WhenIdDoesNotExist() {

        // when
        String id = "1";
        Mono<Boolean> deleted = customApartmentRepository.delete(id);

        // then
        StepVerifier.create(deleted)
                .expectErrorMatches(r -> r instanceof CustomNotFoundException &&
                        r.getMessage().equalsIgnoreCase("Apartment with id %s does not exist!".formatted(id)));
    }
}