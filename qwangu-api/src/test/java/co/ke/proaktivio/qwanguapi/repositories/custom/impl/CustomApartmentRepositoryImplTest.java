package co.ke.proaktivio.qwanguapi.repositories.custom.impl;

import co.ke.proaktivio.qwanguapi.QwanguApiApplication;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.ContextConfiguration;
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
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
class CustomApartmentRepositoryImplTest {

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
    @DisplayName("create returns a Mono of Apartment when name does not exist")
    void create_returnMonoOfApartment_whenSuccessful() {

        // given
        String name = "Luxury Apartment";
        ApartmentDto dto = new ApartmentDto(name);

        // when
        Mono<Apartment> saved = customApartmentRepository.create(dto);

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
    @DisplayName("update returns an updated apartment when successful")
    void update_returnsMonoOfAnUpdatedApartment_whenSuccessful() {
        // given
        String updatedName = "Thika road Apartments";

        //when
        Mono<Apartment> updated =
                template.dropCollection(Apartment.class)
                        .thenReturn(new ApartmentDto("Luxury Apartments"))
                        .flatMap(a -> customApartmentRepository.create(a))
                        .flatMap(a -> customApartmentRepository.update(a.getId(), new ApartmentDto(updatedName)));

        // then
        StepVerifier.create(updated)
                .expectNextMatches(apartment ->
                        StringUtils.hasText(apartment.getId()) &&
                                StringUtils.hasText(apartment.getName()) && apartment.getName().equalsIgnoreCase(updatedName) &&
                                apartment.getModified() != null && StringUtils.hasText(apartment.getModified().toString()))
//                        result.getId() != null && !result.getId().isBlank() && !result.getId().isEmpty() &&
//                        result.getName().isPresent() && result.getName().get().equalsIgnoreCase(updatedName))
                .verifyComplete();
    }

    @Test
    @DisplayName("update returns a CustomNotFoundException when id does not exist")
    void update_returnsCustomNotFoundException_whenIdDoesNotExist() {

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
    @DisplayName("update returns a CustomAlreadyExistsException when name already exists")
    void update_returnsCustomAlreadyExistsException_whenNameAlreadyExists() {

        // given
        String name = "Luxury Apartments";
        ApartmentDto dto = new ApartmentDto(name);

        // when
        Mono<Apartment> saved = customApartmentRepository.create(dto)
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
        Flux<Apartment> saved = template.dropCollection(Apartment.class)
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
                .expectNextCount(2)
//                .expectNextMatches(aprt1 -> aprt1.getName().equalsIgnoreCase("Luxury Apartments"))
//                .expectNextMatches(aprt2 -> aprt2.getName().equalsIgnoreCase("Luxury Apartments B"))
                .verifyComplete();
    }

    @Test
    @DisplayName("findPaginated returns a CustomNotFoundException when none exist!")
    void findPaginated_returnsCustomNotFoundException_whenNoApartmentsExists() {
        // given

        //when
        Flux<Apartment> saved = template.dropCollection(Apartment.class)
                .doOnSuccess(e -> System.out.println("----Dropped table successfully!"))
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
        //given
        var dto = new ApartmentDto("Luxury Apartments");

        // when
        Mono<Boolean> deleted = customApartmentRepository
                .create(dto)
                .flatMap(a -> customApartmentRepository.delete(a.getId()));

        // then
        StepVerifier.create(deleted)
                .expectNextMatches(r -> r)
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns CustomNotFoundException when id does not exist")
    void delete_returnsCustomNotFoundException_whenIdDoesNotExist() {
        // given
        String id = "1";

        // when
        Mono<Boolean> deleted = customApartmentRepository.delete(id);

        // then
        StepVerifier.create(deleted)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Apartment with id %s does not exist!".formatted(id)))
                .verify();
    }
}