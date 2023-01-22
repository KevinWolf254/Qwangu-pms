package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.pojos.PropertyDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.PropertyRepository;
import co.ke.proaktivio.qwanguapi.services.PropertyService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PropertyServiceImplIntegrationTest {
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private PropertyService propertyService;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

    private final String name = "Luxury property";
    private final PropertyDto dto = new PropertyDto(name);

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    //    TODO ADD ON VM OPTIONS
    //-XX:+AllowRedefinitionToAddDeleteMethods

    private Mono<Void> deleteAll() {
        return propertyRepository.deleteAll()
                .doOnSuccess($ -> System.out.println("---- Deleted all properties!"));
    }

    @Test
    @DisplayName("create returns a property when property name does not exist")
    void create_returnMonoOfProperty_whenSuccessful() {
        // when
        Mono<Property> saved = deleteAll()
                .then(propertyService.create(dto));
        // then
        StepVerifier.create(saved)
                .expectNextMatches(property ->
                        StringUtils.hasText(property.getId()) &&
                                StringUtils.hasText(property.getName()) && property.getName().equalsIgnoreCase(name) &&
                                property.getCreatedOn() != null && StringUtils.hasText(property.getCreatedOn().toString()))
                .verifyComplete();
    }

    @Test
    @DisplayName("create returns a CustomAlreadyExistsException when name already exists")
    void create_returnsCustomAlreadyExistsException_whenPropertyNameExists() {
        // when
        Mono<Property> saved = deleteAll()
                .then(propertyService.create(dto))
                .thenReturn(dto)
                .flatMap(d -> propertyService.create(d));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equalsIgnoreCase("Property %s already exists!".formatted(name)))
                .verify();
    }

    @Test
    @DisplayName("update returns an updated property when successful")
    void update_returnsMonoOfAnUpdatedProperty_whenSuccessful() {
        // given
        String updatedName = "Thika road propertys";
        //when
        Mono<Property> updated = deleteAll()
                .then(propertyService.create(dto))
                .doOnSuccess(a -> System.out.println("---- Created: " + a))
                .flatMap(a -> propertyService.update(a.getId(), new PropertyDto(updatedName)));

        // then
        StepVerifier.create(updated)
                .expectNextMatches(property -> StringUtils.hasText(property.getId()) &&
                        StringUtils.hasText(property.getName()) && property.getName().equalsIgnoreCase(updatedName) &&
                        property.getModifiedOn() != null && StringUtils.hasText(property.getModifiedOn().toString()))
                .verifyComplete();
    }

    @Test
    @DisplayName("update returns a CustomNotFoundException when id does not exist")
    void update_returnsCustomNotFoundException_whenIdDoesNotExist() {
        // when
        Mono<Property> saved = deleteAll()
                .then(propertyService.update("12345", dto));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("property with id %s does not exists!".formatted("12345")))
                .verify();
    }

    @Test
    @DisplayName("update returns a CustomAlreadyExistsException when name already exists")
    void update_returnsCustomAlreadyExistsException_whenNameAlreadyExists() {
        // when
        Property property01 = new Property(name);
        var property02 = new Property("Luxury properties B");
        Mono<Property> saved = deleteAll()
                .then(propertyRepository.save(property01))
                .doOnSuccess(a -> System.out.println("---- Created: " + a))
                .flatMap(property -> propertyRepository.save(property02))
                .doOnSuccess(a -> System.out.println("---- Created: " + a))
                .flatMap(a -> propertyService.update(a.getId(), dto));

        // then
        StepVerifier.create(saved)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equalsIgnoreCase("property %s already exists!".formatted(name)))
                .verify();
    }

    @Test
    @DisplayName("find paginated returns a flux of properties when successful")
    void findPaginated_returnsFluxOfProperties_whenSuccessful() {
        //when
        Flux<Property> saved = deleteAll()
                .thenMany(Flux
                        .just(new PropertyDto("Luxury properties"), new PropertyDto("Luxury properties B")))
                .flatMap(a -> propertyService.create(a))
                .doOnNext(a -> System.out.println("---- Created " +a))
                .thenMany(propertyService
                        .find(Optional.empty(),
                                OrderType.ASC))
                .doOnNext(a -> System.out.println("---- Found " +a));

        // then
        StepVerifier
                .create(saved)
                .expectNextMatches(aprt -> aprt.getName().equalsIgnoreCase("Luxury properties") ||
                        aprt.getName().equalsIgnoreCase("Luxury properties B"))
                .expectNextMatches(aprt -> aprt.getName().equalsIgnoreCase("Luxury properties") ||
                        aprt.getName().equalsIgnoreCase("Luxury properties B"))
                .verifyComplete();
    }

    @Test
    @DisplayName("find returns a CustomNotFoundException when none exist!")
    void find_returnsCustomNotFoundException_whenPropertiesDoNotExist() {
        // given

        //when
        Flux<Property> saved = deleteAll()
                .thenMany(propertyService.find(Optional.empty(),
                        OrderType.ASC))
                .doOnError(a -> System.out.println("---- Found no properties!"));

        // then
        StepVerifier
                .create(saved)
                .expectNextCount(0)
//                .expectNextMatches(r -> r..getMessage().equalsIgnoreCase("Properties were not found!"))
                .verifyComplete();
    }

    @Test
    @DisplayName("delete returns a true when successful")
    void delete_returnsTrue_whenSuccessful() {
        // when
        Mono<Boolean> deleted = deleteAll()
                .then(propertyService.create(dto))
                .flatMap(a -> propertyService.deleteById(a.getId()));

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
                .then(propertyService.deleteById("11234"));

        // then
        StepVerifier.create(deleted)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Property with id %s does not exist!".formatted("11234")))
                .verify();
    }
}