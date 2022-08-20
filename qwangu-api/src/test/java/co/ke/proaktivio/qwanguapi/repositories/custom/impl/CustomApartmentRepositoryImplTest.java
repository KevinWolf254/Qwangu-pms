package co.ke.proaktivio.qwanguapi.repositories.custom.impl;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.ApartmentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
@SpringBootTest
class CustomApartmentRepositoryImplTest {
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private CustomApartmentRepositoryImpl customApartmentRepository;
    private final String name = "Luxury Apartment";
    private final ApartmentDto dto = new ApartmentDto(name);

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    private Mono<Void> deleteAll() {
        return apartmentRepository.deleteAll()
                .doOnSuccess($ -> System.out.println("---- Deleted all Apartments!"));
    }

    @Test
    @DisplayName("create returns a Mono of Apartment when name does not exist")
    void create_returnMonoOfApartment_whenSuccessful() {
        // when
        Mono<Apartment> saved = deleteAll()
                .then(customApartmentRepository.create(dto));
        // then
        StepVerifier.create(saved)
                .expectNextMatches(apartment ->
                        StringUtils.hasText(apartment.getId()) &&
                                StringUtils.hasText(apartment.getName()) && apartment.getName().equalsIgnoreCase(name) &&
                                apartment.getCreated() != null && StringUtils.hasText(apartment.getCreated().toString()))
                .verifyComplete();
    }

    @Test
    @DisplayName("create returns a CustomAlreadyExistsException when name already exists")
    void create_returnsCustomAlreadyExistsException_whenApartmentNameExists() {
        // when
        Mono<Apartment> saved = deleteAll()
                .then(customApartmentRepository.create(dto))
                .thenReturn(dto)
                .flatMap(d -> customApartmentRepository.create(d));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equalsIgnoreCase("Apartment %s already exists!".formatted(name)))
                .verify();
    }

    @Test
    @DisplayName("update returns an updated apartment when successful")
    void update_returnsMonoOfAnUpdatedApartment_whenSuccessful() {
        // given
        String updatedName = "Thika road Apartments";
        //when
        Mono<Apartment> updated = deleteAll()
                .then(customApartmentRepository.create(dto))
                .doOnSuccess(a -> System.out.println("---- Created: " + a))
                .flatMap(a -> customApartmentRepository.update(a.getId(), new ApartmentDto(updatedName)));

        // then
        StepVerifier.create(updated)
                .expectNextMatches(apartment -> StringUtils.hasText(apartment.getId()) &&
                        StringUtils.hasText(apartment.getName()) && apartment.getName().equalsIgnoreCase(updatedName) &&
                        apartment.getModified() != null && StringUtils.hasText(apartment.getModified().toString()))
                .verifyComplete();
    }

    @Test
    @DisplayName("update returns a CustomNotFoundException when id does not exist")
    void update_returnsCustomNotFoundException_whenIdDoesNotExist() {
        // when
        Mono<Apartment> saved = deleteAll()
                .then(customApartmentRepository.update("12345", dto));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Apartment with id %s does not exists!".formatted("12345")))
                .verify();
    }

    @Test
    @DisplayName("update returns a CustomAlreadyExistsException when name already exists")
    void update_returnsCustomAlreadyExistsException_whenNameAlreadyExists() {
        // when
        Mono<Apartment> saved = deleteAll()
                .then(customApartmentRepository.create(dto))
                .thenReturn(new ApartmentDto("Luxury Apartments B"))
                .flatMap(dto2 -> customApartmentRepository.create(dto2))
                .flatMap(a -> customApartmentRepository.update(a.getId(), dto));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equalsIgnoreCase("Apartment %s already exists!".formatted(name)))
                .verify();
    }

    @Test
    @DisplayName("find paginated returns a flux of apartments when successful")
    void findPaginated_returnsFluxOfApartments_whenSuccessful() {
        // given

        //when
        Flux<Apartment> saved = deleteAll()
                .thenMany(Flux
                        .just(new ApartmentDto("Luxury Apartments"), new ApartmentDto("Luxury Apartments B")))
                .flatMap(a -> customApartmentRepository.create(a))
                .thenMany(customApartmentRepository
                        .findPaginated(Optional.empty(),
                                Optional.empty(), 1, 10,
                                OrderType.ASC));

        // then
        StepVerifier
                .create(saved)
                .expectNextMatches(aprt -> aprt.getName().equalsIgnoreCase("Luxury Apartments") ||
                        aprt.getName().equalsIgnoreCase("Luxury Apartments B"))
                .expectNextMatches(aprt -> aprt.getName().equalsIgnoreCase("Luxury Apartments") ||
                        aprt.getName().equalsIgnoreCase("Luxury Apartments B"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findPaginated returns a CustomNotFoundException when none exist!")
    void findPaginated_returnsCustomNotFoundException_whenNoApartmentsExists() {
        // given

        //when
        Flux<Apartment> saved = deleteAll()
                .thenMany(customApartmentRepository.findPaginated(Optional.empty(),
                        Optional.empty(), 1, 10,
                        OrderType.ASC))
                .doOnError(a -> System.out.println("---- Found no apartments!"));

        // then
        StepVerifier
                .create(saved)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Apartments were not found!"))
                .verify();
    }

    @Test
    @DisplayName("delete returns a true when successful")
    void delete_returnsTrue_whenSuccessful() {
        // when
        Mono<Boolean> deleted = deleteAll()
                .then(customApartmentRepository.create(dto))
                .flatMap(a -> customApartmentRepository.delete(a.getId()));

        // then
        StepVerifier.create(deleted)
                .expectNextMatches(r -> r)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns CustomNotFoundException when id does not exist")
    void delete_returnsCustomNotFoundException_whenIdDoesNotExist() {
        // when
        Mono<Boolean> deleted = deleteAll()
                .then(customApartmentRepository.delete("11234"));

        // then
        StepVerifier.create(deleted)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Apartment with id %s does not exist!".formatted("11234")))
                .verify();
    }
}