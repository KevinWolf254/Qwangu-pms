package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.CreateOccupationDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateOccupationDto;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.TenantRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
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
import java.util.Optional;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
class OccupationServiceImplIntegrationTest {
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private ReactiveMongoTemplate template;
    @Autowired
    private OccupationServiceImpl occupationService;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Test
    void create() {
        // given
        String unitId = "12";
        String tenantId = "13";
        var dto = new CreateOccupationDto(false, LocalDateTime.now(), null, tenantId, unitId);
        var dtoUnitIdNotExist = new CreateOccupationDto(false, LocalDateTime.now(), null, tenantId, "5");
        var dtoTenantIdNotExist = new CreateOccupationDto(false, LocalDateTime.now(), null, "6", unitId);
        var tenant = new Tenant(tenantId, "John", "middle", "Doe", "0700000000", "person@gmail.com", LocalDateTime.now(), null);
        var tenantActive = new Tenant("1", "John", "middle", "Doe", "0700000000", "person@gmail.com", LocalDateTime.now(), null);
        var unit = new Unit(unitId, true, "TE99", Unit.Type.APARTMENT_UNIT, Unit.Identifier.B,
                2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");
        var occupationActive = new Occupation(null, true, LocalDateTime.now(), null, "1", unitId, LocalDateTime.now(), null);
        // when
        Mono<Occupation> createOccupation = unitRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(tenantRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"))
                .then(tenantRepository.save(tenant))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto))
                .doOnSuccess(a -> System.out.println("---- Saved " + a));
        // then
        StepVerifier
                .create(createOccupation)
                .expectNextMatches(o -> !o.getId().isEmpty() && o.getTenantId().equals(tenantId) &&
                        o.getUnitId().equals(unitId) && !o.getActive())
                .verifyComplete();

        // when
        Mono<Occupation> createWithUnitIdNonExist = occupationService.create(dtoUnitIdNotExist);
        // then
        StepVerifier
                .create(createWithUnitIdNonExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Unit with id 5 does not exist!"))
                .verify();

        // when
        Mono<Occupation> createWithTenantIdNonExist = occupationService.create(dtoTenantIdNotExist);
        // then
        StepVerifier
                .create(createWithTenantIdNonExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Tenant with id 6 does not exist!"))
                .verify();

        // when
        Mono<Occupation> createAlreadyActive = tenantRepository
                .save(tenantActive)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationRepository.save(occupationActive))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto));
        // then
        StepVerifier
                .create(createAlreadyActive)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equals("Occupation already exists!"))
                .verify();
    }

    @Test
    void update() {
        // given
        String id = "1";
        String unitId = "12";
        String tenantId = "13";
        var dto = new UpdateOccupationDto(true, LocalDateTime.now(), null);
        var occupation = new Occupation(id, true, LocalDateTime.now(), null, tenantId, unitId, LocalDateTime.now(), null);
        var occupationThatIsActive = new Occupation("2", true, LocalDateTime.now(), null, "14", unitId, LocalDateTime.now(), null);
        var tenant = new Tenant(tenantId, "John", "middle", "Doe", "0700000000", "person@gmail.com", LocalDateTime.now(), null);
        var unit = new Unit(unitId, true, "TE99", Unit.Type.APARTMENT_UNIT, Unit.Identifier.B,
                2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");
        var dtoToDiActivate = new UpdateOccupationDto(false, LocalDateTime.now(), null);

        //when
        Mono<Occupation> updateOccupation = unitRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(tenantRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(tenantRepository.save(tenant))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.update(id, dto))
                .doOnSuccess(a -> System.out.println("---- Updated " + a));

        // then
        StepVerifier
                .create(updateOccupation)
                .expectNextCount(1)
                .verifyComplete();

        // when
        Mono<Occupation> updateOccupationThatIsActive = occupationRepository.save(occupationThatIsActive)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.update(id, dto));
        // then
        StepVerifier
                .create(updateOccupationThatIsActive)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Can not activate while other occupation is active!"))
                .verify();

        // when
        Mono<Occupation> updateToDiActivate = occupationService.update(id, dtoToDiActivate);
        // then
        StepVerifier
                .create(updateToDiActivate)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void findPaginated() {
        // given
        String id = "1";
        boolean active = true;
        String unitId = "1";
        String tenantId= "1";
        var occupation = new Occupation(id, active, LocalDateTime.now(), null, tenantId, unitId, LocalDateTime.now(), null);
        var occupation2 = new Occupation("2", active, LocalDateTime.now(), null, "2", "2", LocalDateTime.now(), null);

        //when
        Flux<Occupation> findOccupation = occupationRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(occupationService.findPaginated(Optional.of(id), Optional.of(active), Optional.of(unitId),
                        Optional.of(tenantId), 1, 10, OrderType.ASC));
        // then
        StepVerifier
                .create(findOccupation)
                .expectNextMatches(o -> o.getActive() &&o.getId().equals(id))
                .verifyComplete();

        // when
        Flux<Occupation> findOccupationNotExist = occupationService.findPaginated(Optional.of("2000"), Optional.of(active), Optional.of(unitId),
                Optional.of(tenantId), 1, 10, OrderType.ASC);
        // then
        StepVerifier
                .create(findOccupationNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Occupations were not found!"))
                .verify();

        // when
        Flux<Occupation> findAll = occupationRepository.save(occupation2)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(occupationService.findPaginated(Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty(), 1, 10, OrderType.DESC));
        // then
        StepVerifier
                .create(findAll)
                .expectNextMatches(o -> o.getId().equals("2"))
                .expectNextMatches(o -> o.getId().equals("1"))
                .verifyComplete();
    }

    @Test
    void deleteById() {
        // given
        String id = "1";
        boolean active = true;
        String unitId = "1";
        String tenantId= "1";
        var occupation = new Occupation(id, active, LocalDateTime.now(), null, tenantId, unitId, LocalDateTime.now(), null);

        // then
        Mono<Boolean> createThenDelete = occupationRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.deleteById(id));
        // then
        StepVerifier
                .create(createThenDelete)
                .expectNext(true)
                .verifyComplete();


        // when
        Mono<Boolean> deleteThatDoesNotExist = occupationService.deleteById("3090");
        // then
        StepVerifier
                .create(deleteThatDoesNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Occupation with id 3090 does not exist!"))
                .verify();
    }

}