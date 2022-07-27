package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import co.ke.proaktivio.qwanguapi.repositories.ApartmentRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import org.junit.jupiter.api.DisplayName;
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

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
class UnitServiceImplIntegrationTest {
    @Autowired
    private ApartmentRepository apartmentRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private ReactiveMongoTemplate template;
    @Autowired
    private UnitServiceImpl unitService;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Test
    @DisplayName("create a unit that is unique and apartment exists.")
    void create() {
        // given
        String id = "1";
        String name = "Luxury Apartment";
        LocalDateTime now = LocalDateTime.now();
        var apartment = new Apartment(id, name, now, null);
        var dto = new UnitDto(Unit.Status.VACANT, Unit.Type.APARTMENT_UNIT, Unit.Identifier.A, 1, 2, 1, 2, Unit.Currency.KES,
                25000, 500, 500, id);
        var dtoWithNonExistingApartment = new UnitDto(Unit.Status.VACANT, Unit.Type.APARTMENT_UNIT, Unit.Identifier.A, 1, 2, 1, 2, Unit.Currency.KES,
                25000, 500, 500, "2");
        var dtoNotApartmentUnit = new UnitDto(Unit.Status.VACANT, Unit.Type.MAISONETTES, null, null, 2, 1, 2, Unit.Currency.KES,
                25000, 500, 500, null);
        // when
        Mono<Unit> createUnit = apartmentRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Apartments!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(apartmentRepository.save(apartment))
                        .doOnSuccess(a -> System.out.println("---- Saved " + a))
                        .then(unitService.create(dto))
                        .doOnSuccess(a -> System.out.println("---- Saved " + a));

        // then
        StepVerifier
                .create(createUnit)
                .expectNextCount(1)
                .verifyComplete();

        // when
        Mono<Unit> createUnitNotApartmentUnit = unitService.create(dtoNotApartmentUnit);
        // then
        StepVerifier
                .create(createUnitNotApartmentUnit)
                .expectNextMatches(u -> u.getType().equals(Unit.Type.MAISONETTES) &&
                        u.getApartmentId() == null && u.getIdentifier() == null &&
                        u.getFloorNo() == null)
                .verifyComplete();

        // when
        Mono<Unit> createUnitWithNonExistingApartment = unitService.create(dtoWithNonExistingApartment);
        // then
        StepVerifier
                .create(createUnitWithNonExistingApartment)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Apartment with id 2 does not exist!"))
                .verify();
        // when
        Mono<Unit> createUnitThatAlreadyExists = unitService.create(dto);
        // then
        StepVerifier
                .create(createUnitThatAlreadyExists)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equals("Unit already exists!"))
                .verify();
    }

    @Test
    @DisplayName("update a unit that exists.")
    void update() {
        // given
        String id = "1";
        String name = "Luxury Apartment";
        LocalDateTime now = LocalDateTime.now();
        var apartment = new Apartment(id, name, now, null);
        var dto = new UnitDto(Unit.Status.VACANT, Unit.Type.APARTMENT_UNIT, Unit.Identifier.A, 1, 2, 1, 2, Unit.Currency.KES,
                25000, 500, 500, id);
        var dtoUpdate = new UnitDto(Unit.Status.VACANT, Unit.Type.APARTMENT_UNIT, Unit.Identifier.A, 1, 2, 1, 2, Unit.Currency.KES,
                26000, 510, 510, "1");
        var unit = new Unit("301", Unit.Status.VACANT, "TE34", Unit.Type.APARTMENT_UNIT, Unit.Identifier.B,
                2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");
        var dtoThatChangesUnitType = new UnitDto(Unit.Status.VACANT, Unit.Type.MAISONETTES, Unit.Identifier.A, 1, 2, 1, 2, Unit.Currency.KES,
                25000, 500, 500, "1");
        var unit2 = new Unit("302", Unit.Status.VACANT, "TE35", Unit.Type.APARTMENT_UNIT, Unit.Identifier.C,
                3, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");
        var dtoThatChangesUnitIdentifierAndFloorNo = new UnitDto(Unit.Status.VACANT, Unit.Type.APARTMENT_UNIT, Unit.Identifier.A, 1, 2, 1, 2, Unit.Currency.KES,
                25000, 500, 500, "1");
        var unitNotForApartment = new Unit("4444", Unit.Status.VACANT, "SE44", Unit.Type.MAISONETTES, null,
                null, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, null);
        var dtoUpdateNotForApartment = new UnitDto(Unit.Status.VACANT, Unit.Type.MAISONETTES, null, null, 5, 3, 2, Unit.Currency.KES,
                45000, 1510, 1510, null);

        // when
        Mono<Unit> createUpdateUnit = apartmentRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Apartments!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(apartmentRepository.save(apartment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitService.create(dto))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .flatMap(u -> unitService.update(u.getId(), dtoUpdate))
                .doOnSuccess(a -> System.out.println("---- Updated " + a));
        // then
        StepVerifier
                .create(createUpdateUnit)
                .expectNextMatches(u ->
                        u.getRentPerMonth() == 26000 &&
                                u.getSecurityPerMonth() == 510 &&
                                u.getGarbagePerMonth() == 510 &&
                                u.getModified() != null)
                .verifyComplete();

        // when
        Mono<Unit> createUnitAndUpdateNotForApartment = unitRepository.save(unitNotForApartment)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitService.update("4444", dtoUpdateNotForApartment));
        // then
        StepVerifier
                .create(createUnitAndUpdateNotForApartment)
                .expectNextMatches(u -> u.getIdentifier() == null &&
                        u.getFloorNo() == null && u.getApartmentId() == null &&
                        u.getId().equals("4444") &&
                        u.getModified() != null &&
                        u.getRentPerMonth() == 45000 &&
                        u.getSecurityPerMonth() == 1510 &&
                        u.getGarbagePerMonth() == 1510)
                .verifyComplete();
        // when
        Mono<Unit> updateUnitThatDoesNotExist = unitService.update("299999", dto);
        // then
        StepVerifier
                .create(updateUnitThatDoesNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Unit with id 299999 does not exist!"))
                .verify();

        // when
        Mono<Unit> updateUnitType = unitRepository.save(unit)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .flatMap(u -> unitService.update(u.getId(), dtoThatChangesUnitType));
        // then
        StepVerifier
                .create(updateUnitType)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Can not change the unit's type!"))
                .verify();

        // when
        Mono<Unit> updateUnitThatChangesUnitIdentifierAndFloorNo = unitRepository.save(unit2)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .flatMap(u -> unitService.update(u.getId(), dtoThatChangesUnitIdentifierAndFloorNo));
        // then
        StepVerifier
                .create(updateUnitThatChangesUnitIdentifierAndFloorNo)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Can not change the unit's floorNo and identifier!"))
                .verify();
    }

    @Test
    @DisplayName("findPaginated a unit that exists.")
    void findPaginated() { // given
        String id = "1";
        String name = "Luxury Apartment";
        LocalDateTime now = LocalDateTime.now();
        var apartment = new Apartment(id, name, now, null);
        var unit = new Unit("301", Unit.Status.VACANT, "TE34", Unit.Type.APARTMENT_UNIT, Unit.Identifier.B,
                2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");
        var unit2 = new Unit("303", Unit.Status.VACANT, "TE36", Unit.Type.APARTMENT_UNIT, Unit.Identifier.A,
                2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");

        // when
        Flux<Unit> findUnit = apartmentRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Apartments!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(apartmentRepository.save(apartment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(unitService.findPaginated(Optional.of("301"), Optional.of(Unit.Status.VACANT), Optional.of("TE34"), Optional.of(Unit.Type.APARTMENT_UNIT),
                        Optional.of(Unit.Identifier.B), Optional.of(2), Optional.of(2), Optional.of(1), Optional.of(id), 1, 5, OrderType.ASC));
        // then
        StepVerifier
                .create(findUnit)
                .expectNextMatches(u -> u.getId().equals("301"))
                .verifyComplete();

        // when
        Flux<Unit> findUnitNonExisting = unitService.findPaginated(Optional.of("302"), Optional.of(Unit.Status.VACANT), Optional.of("TE34"), Optional.of(Unit.Type.APARTMENT_UNIT),
                Optional.of(Unit.Identifier.B), Optional.of(2), Optional.of(2), Optional.of(1), Optional.of(id), 1, 5, OrderType.ASC);
        // then
        StepVerifier
                .create(findUnitNonExisting)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Units were not found!"))
                .verify();

        // when
        Flux<Unit> createUnitAndFindAllOnSecondFloor = unitRepository.save(unit2)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(unitService.findPaginated(Optional.empty(), Optional.empty(),Optional.empty(), Optional.of(Unit.Type.APARTMENT_UNIT),
                        Optional.empty(), Optional.of(2), Optional.of(2), Optional.of(1), Optional.of(id), 1, 5, OrderType.DESC));
        // then
        StepVerifier
                .create(createUnitAndFindAllOnSecondFloor)
                .expectNextMatches(u -> u.getId().equals("303"))
                .expectNextMatches(u -> u.getId().equals("301"))
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteById a unit that exists.")
    void deleteById() {
        // given
        var unit = new Unit("9999", Unit.Status.VACANT, "TE99", Unit.Type.APARTMENT_UNIT, Unit.Identifier.B,
                2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");

        // when
        Mono<Boolean> createUnitThenDelete = apartmentRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Apartments!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitService.deleteById("9999"));
        // then
        StepVerifier
                .create(createUnitThenDelete)
                .expectNext(true)
                .verifyComplete();

        // when
        Mono<Boolean> deleteUnitThatDoesNotExist = unitService.deleteById("3090");
        // then
        StepVerifier
                .create(deleteUnitThatDoesNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Unit with id 3090 does not exist!"))
                .verify();

    }
}