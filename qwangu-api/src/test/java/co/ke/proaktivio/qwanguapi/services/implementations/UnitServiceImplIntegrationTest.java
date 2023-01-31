package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import co.ke.proaktivio.qwanguapi.repositories.PropertyRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UnitServiceImplIntegrationTest {
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private ReactiveMongoTemplate template;
    @Autowired
    private UnitServiceImpl unitService;
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
    @DisplayName("create a unit that is unique and apartment exists.")
    void create() {
        // given
        String id = "1";
        String name = "Luxury Apartment";
//        LocalDateTime now = LocalDateTime.now();
        var apartment = new Property();
        apartment.setName(name);
        apartment.setId(id);
        var dto = new UnitDto(Unit.Status.VACANT, Unit.UnitType.APARTMENT_UNIT, Unit.Identifier.A, 1,
                2, 1, 2, Unit.Currency.KES,
                BigDecimal.valueOf(25000), BigDecimal.valueOf(500), BigDecimal.valueOf(500), null, id);
        var dtoWithNonExistingApartment = new UnitDto(Unit.Status.VACANT, Unit.UnitType.APARTMENT_UNIT, Unit.Identifier.A,
                1, 2, 1, 2, Unit.Currency.KES,
                BigDecimal.valueOf(25000), BigDecimal.valueOf(500), BigDecimal.valueOf(500), null, "2");
        var dtoNotApartmentUnit = new UnitDto(Unit.Status.VACANT, Unit.UnitType.MAISONETTES, null, null,
                2, 1, 2, Unit.Currency.KES,
                BigDecimal.valueOf(25000), BigDecimal.valueOf(500), BigDecimal.valueOf(500), null, null);
        // when
        Mono<Unit> createUnit = propertyRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Apartments!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(propertyRepository.save(apartment))
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
                .expectNextMatches(u -> u.getType().equals(Unit.UnitType.MAISONETTES) &&
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
        var apartment = new Property();
        apartment.setName(name);
        var otherAmounts = new HashMap<String, BigDecimal>();
        otherAmounts.put("Gym", new BigDecimal(500));
        otherAmounts.put("Swimming Pool", new BigDecimal(800));

        apartment.setId(id);
        var dto = new UnitDto(Unit.Status.VACANT, Unit.UnitType.APARTMENT_UNIT, Unit.Identifier.A, 1, 2,
                1, 2, Unit.Currency.KES, BigDecimal.valueOf(25000), BigDecimal.valueOf(500),
                BigDecimal.valueOf(500), null, id);
        var dtoUpdate = new UnitDto(Unit.Status.VACANT, Unit.UnitType.APARTMENT_UNIT, Unit.Identifier.A, 1,
                2, 1, 2, Unit.Currency.KES, BigDecimal.valueOf(26000),
                BigDecimal.valueOf(510), BigDecimal.valueOf(510), otherAmounts, "1");
//        var unit = new Unit("301", Unit.Status.VACANT, false, "TE34", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.B, 2, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(27000), BigDecimal.valueOf(510), BigDecimal.valueOf(300), LocalDateTime.now(),
//                null, "1");
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
//                .booked(false)
                .number("TE34")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.B)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(510))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        unit.setId("301");

        var dtoThatChangesUnitType = new UnitDto(Unit.Status.VACANT, Unit.UnitType.MAISONETTES, Unit.Identifier.A, 1,
                2, 1, 2, Unit.Currency.KES, BigDecimal.valueOf(25000),
                BigDecimal.valueOf(500), BigDecimal.valueOf(500), null, "1");
//        var unit2 = new Unit("302", Unit.Status.VACANT, false, "TE35", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.C, 3, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(27000), BigDecimal.valueOf(510), BigDecimal.valueOf(300), LocalDateTime.now(),
//                null, "1");
        var unit2 = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
//                .booked(false)
                .number("TE35")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.C)
                .floorNo(3)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(510))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        unit2.setId("302");

        var dtoThatChangesUnitIdentifierAndFloorNo = new UnitDto(Unit.Status.VACANT, Unit.UnitType.APARTMENT_UNIT,
                Unit.Identifier.A, 1, 2, 1, 2, Unit.Currency.KES,
                BigDecimal.valueOf(25000), BigDecimal.valueOf(500), BigDecimal.valueOf(500), null, "1");
//        var unitNotForApartment = new Unit("4444", Unit.Status.VACANT, false, "SE44",
//                Unit.Type.MAISONETTES, null, null, 2, 1, 2,
//                Unit.Currency.KES, BigDecimal.valueOf(27000), BigDecimal.valueOf(510), BigDecimal.valueOf(300),
//                LocalDateTime.now(), null, null);
        var unitNotForApartment = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
//                .booked(false)
                .number("SE44")
                .type(Unit.UnitType.MAISONETTES)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(510))
                .garbagePerMonth(BigDecimal.valueOf(300)).build();
        unitNotForApartment.setId("4444");

        var dtoUpdateNotForApartment = new UnitDto(Unit.Status.VACANT, Unit.UnitType.MAISONETTES, null, null,
                5, 3, 2, Unit.Currency.KES, BigDecimal.valueOf(45000),
                BigDecimal.valueOf(1510), BigDecimal.valueOf(1510), null, null);

        // when
        Mono<Unit> createUpdateUnit = propertyRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Apartments!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(propertyRepository.save(apartment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitService.create(dto))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .flatMap(u -> unitService.update(u.getId(), dtoUpdate))
                .doOnSuccess(a -> System.out.println("---- Updated " + a));
        // then
        StepVerifier
                .create(createUpdateUnit)
                .expectNextMatches(u ->
                        u.getRentPerMonth().equals(BigDecimal.valueOf(26000)) &&
                                u.getSecurityPerMonth().equals(BigDecimal.valueOf(510)) &&
                                u.getGarbagePerMonth().equals(BigDecimal.valueOf(510)) &&
                                u.getModifiedOn() != null)
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
                        u.getModifiedOn() != null &&
                        u.getRentPerMonth().equals(BigDecimal.valueOf(45000)) &&
                        u.getSecurityPerMonth().equals(BigDecimal.valueOf(1510)) &&
                        u.getGarbagePerMonth().equals(BigDecimal.valueOf(1510)))
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
        var apartment = new Property();
        apartment.setName(name);
        apartment.setId(id);
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
//                .booked(false)
                .number("TE34")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.B)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(510))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        unit.setId("301");
        var unit2 = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
//                .booked(false)
                .number("TE36")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.A)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(510))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        unit2.setId("303");

        // when
        Flux<Unit> findUnit = propertyRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Apartments!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(propertyRepository.save(apartment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(unitService.find(Optional.of(id), Optional.of(Unit.Status.VACANT), Optional.of("TE34"),
                        Optional.of(Unit.UnitType.APARTMENT_UNIT), Optional.of(Unit.Identifier.B), Optional.of(2),
                        Optional.of(2), Optional.of(1), OrderType.ASC))
                .doOnNext(u -> System.out.println("---- Found " +u));
        // then
        StepVerifier
                .create(findUnit)
                .expectNextMatches(u -> u.getId().equals("301"))
                .verifyComplete();

        // when
        Flux<Unit> findUnitNonExisting = unitService.find(Optional.of(id), Optional.of(Unit.Status.VACANT),
                Optional.of("TE35"), Optional.of(Unit.UnitType.APARTMENT_UNIT), Optional.of(Unit.Identifier.E),
                Optional.of(2), Optional.of(2), Optional.of(1), OrderType.ASC)
                .doOnNext(a -> System.out.println("---- Found " +a));
        // then
        StepVerifier
                .create(findUnitNonExisting)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Units were not found!"))
                .verify();

        // when
        Flux<Unit> createUnitAndFindAllOnSecondFloor = unitRepository.save(unit2)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(unitService.find(Optional.of(id), Optional.empty(),Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty(),  OrderType.DESC))
                .doOnNext(a -> System.out.println(" Found " +a));
        // then
        StepVerifier
                .create(createUnitAndFindAllOnSecondFloor)
                .expectNextMatches(u -> u.getId().equals("303") || u.getId().equals("301"))
                .expectNextMatches(u -> u.getId().equals("303") || u.getId().equals("301"))
                .verifyComplete();
    }

    @Test
    @DisplayName("deleteById a unit that exists.")
    void deleteById() {
        // given
//        var unit = new Unit("9999", Unit.Status.VACANT, false, "TE99", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.B, 2, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(27000), BigDecimal.valueOf(510), BigDecimal.valueOf(300), LocalDateTime.now(),
//                null, "1");
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
//                .booked(false)
                .number("TE99")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.B)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(510))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        unit.setId("9999");

        // when
        Mono<Boolean> createUnitThenDelete = propertyRepository.deleteAll()
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