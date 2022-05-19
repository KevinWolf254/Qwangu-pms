package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.ApartmentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ApartmentServiceImplTest {

    @Mock
    private ApartmentRepository repository;
    @InjectMocks
    private ApartmentServiceImpl apartmentService;

    @Test
    @DisplayName("Create returns a Mono of Apartment when name does not exist")
    void create_ReturnMonoOfApartment_WhenSuccessful() {

        // given
        String name = "Luxury Apartment";
        ApartmentDto dto = new ApartmentDto(name);

        LocalDateTime now = LocalDateTime.now();
        Apartment apartment = new Apartment();
        apartment.setId("1");
        apartment.setName(name);
        apartment.setCreated(now);
        apartment.setModified(now);

        // when
        when(repository.create(dto)).thenReturn(Mono.just(apartment));

        // then
        StepVerifier.create(apartmentService.create(dto))
                .expectNext(apartment)
                .verifyComplete();
    }

    @Test
    @DisplayName("Create returns a Mono of Apartment when name does not exist")
    void create_ReturnCustomAlreadyExistsException() {

        // given
        String name = "Luxury Apartment";
        ApartmentDto dto = new ApartmentDto(name);

        LocalDateTime now = LocalDateTime.now();
        Apartment apartment = new Apartment();
        apartment.setId("1");
        apartment.setName(name);
        apartment.setCreated(now);
        apartment.setModified(now);

        // when
        when(repository.create(dto)).thenReturn(Mono.error(new CustomAlreadyExistsException("Apartment %s already exists!".formatted(name))));

        // then
        StepVerifier.create(apartmentService.create(dto))
                .expectError(CustomAlreadyExistsException.class)
                .verify();
    }

    @Test
    @DisplayName("Update returns a Mono of Apartment when apartment with id exists")
    void update_ReturnMonoOfApartment_WhenSuccessful() {
        // given
        String name = "Luxury Apartment";
        ApartmentDto dto = new ApartmentDto(name);

        LocalDateTime now = LocalDateTime.now();
        Apartment apartment = new Apartment();
        String id = "1";
        apartment.setId(id);
        apartment.setName(name);
        apartment.setCreated(now);
        apartment.setModified(now);

        // when
        when(repository.update(id, dto)).thenReturn(Mono.just(apartment));

        // then
        StepVerifier.create(apartmentService.update(id, dto))
                .expectNext(apartment)
                .verifyComplete();
    }
//
//    @Test
//    void findPaginated() {
//    }
//
//    @Test
//    void deleteById() {
//    }
}

@Testcontainers
@DataMongoTest
@ExtendWith(SpringExtension.class)
class ApartmentServiceImplIntegrationTest {

    @Container
    private static MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", container::getReplicaSetUrl);
    }

    @Autowired
    private ApartmentRepository repository;

    @Test
    @DisplayName("Create returns a Mono of Apartment when name does not exist")
    void create_ReturnMonoOfApartment_WhenSuccessful() {

        // given
        String name = "Luxury Apartment";
        ApartmentDto dto = new ApartmentDto(name);

        // when
        Mono<Apartment> saved = repository.create(dto);

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
        Mono<Apartment> saved = repository.create(dto)
                .thenReturn(dto)
                .flatMap(d -> repository.create(d));

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
                repository.deleteAll()
                .thenReturn(new ApartmentDto("Luxury Apartments"))
                .flatMap(d -> repository.create(d))
                .flatMap(d -> repository.update(d.getId(), new ApartmentDto(updatedName)));

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
        Mono<Apartment> saved = repository.update(id, dto);
//                repository.deleteAll()
//                .thenReturn(dto)
//                .flatMap(d -> repository.update(id, d));

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
        Mono<Apartment> saved = repository.create(dto)
//                repository.deleteAll()
//                .thenReturn(dto)
//                .flatMap(a -> repository.create(a))
                .thenReturn(new ApartmentDto("Luxury Apartments B"))
                .flatMap(a -> repository.create(a))
                .flatMap(a -> repository.update(a.getId(), dto));

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
//                repository.deleteAll()
//                .thenMany(Flux.just(new ApartmentDto("Luxury Apartments"), new ApartmentDto("Luxury Apartments B")))
                .flatMap(a -> repository.create(a))
                .thenMany(repository.findPaginated(Optional.empty(),
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
        Flux<Apartment> saved = repository.deleteAll()
                .thenMany(repository.findPaginated(Optional.empty(),
                        Optional.empty(), 0, 10,
                        OrderType.ASC));
//                repository.deleteAll()
//                .thenMany(Flux.just(new ApartmentDto("Luxury Apartments"), new ApartmentDto("Luxury Apartments B")))
//                .flatMap(a -> repository.create(a))
//                .thenMany(repository.findPaginated(Optional.empty(),
//                        Optional.empty(), 0, 10,
//                        OrderType.ASC));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Apartments do not exist!"))
                .verify();
    }
}