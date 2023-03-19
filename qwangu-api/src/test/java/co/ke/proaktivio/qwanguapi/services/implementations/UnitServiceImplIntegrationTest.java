package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import co.ke.proaktivio.qwanguapi.repositories.PropertyRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.HashMap;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UnitServiceImplIntegrationTest {
    @Autowired
    private PropertyRepository propertyRepository;
    @Autowired
    private UnitRepository unitRepository;
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

    @NotNull
    private Mono<Void> reset() {
        return propertyRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Properties!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"));
    }

    @NotNull
    private static Property getProperty() {
        String name = "Luxury Apartment";
        var property = new Property();
        property.setType(Property.PropertyType.APARTMENT);
        property.setName(name);
        property.setId("1");
        return property;
    }

    @NotNull
    private static UnitDto getUnitDto() {
        return new UnitDto(Unit.Status.VACANT, Unit.UnitType.APARTMENT_UNIT, Unit.Identifier.A, 1,
                2, 1, 2, Unit.Currency.KES,
                BigDecimal.valueOf(25000), BigDecimal.valueOf(500), BigDecimal.valueOf(500), null, "1");
    }

    @Test
    void createApartmentUnit_returnsApartmentUnit() {
        // given
        Property property = getProperty();
        UnitDto dto = getUnitDto();
        // when
        Mono<Unit> createUnit = reset()
                .then(propertyRepository.save(property))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitService.create(dto))
                .doOnSuccess(a -> System.out.println("---- Saved " + a));

        // then
        StepVerifier
                .create(createUnit)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void create_returnsCustomAlreadyExistsException_whenUnitAlreadyExists() {
        // given
        var dto = getUnitDto();
        // when
        this.createApartmentUnit_returnsApartmentUnit();
        Mono<Unit> createUnitThatAlreadyExists = unitService.create(dto);
        // then
        StepVerifier
                .create(createUnitThatAlreadyExists)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equals("Unit already exists!"))
                .verify();
    }

    @Test
    void create_returnsCustomBadRequestException_whenUnitTypeAndPropertyTypeDoNotMatch() {
        // given
        Property property = getProperty();
        var dto = getUnitDto();
        dto.setType(Unit.UnitType.VILLA);
        // when
        Mono<Unit> createUnitWithNonMatchingPropertyType = reset()
                .then(propertyRepository.save(property))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitService.create(dto))
                .doOnSuccess(a -> System.out.println("---- Saved " + a));
        // then
        StepVerifier
                .create(createUnitWithNonMatchingPropertyType)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Unit must be of the right type!"))
                .verify();
    }

    @Test
    void create_returnCustomBadRequestException_whenPropertyIdDoesNotExist() {
        // given
        var propertyId = "2";
        var dtoWithNonExistingApartment = new UnitDto(Unit.Status.VACANT, Unit.UnitType.APARTMENT_UNIT, Unit.Identifier.A,
                1, 2, 1, 2, Unit.Currency.KES,
                BigDecimal.valueOf(25000), BigDecimal.valueOf(500), BigDecimal.valueOf(500), null, propertyId);
        // when
        Mono<Unit> createUnitWithNonExistingApartment = unitService.create(dtoWithNonExistingApartment);
        // then
        StepVerifier
                .create(createUnitWithNonExistingApartment)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Property with id %s does not exist!".formatted(propertyId)))
                .verify();
    }

    @Test
    @DisplayName("update a unit that exists.")
    void update() {
        // given
        var property = getProperty();
        var otherAmounts = new HashMap<String, BigDecimal>();
        otherAmounts.put("Gym", new BigDecimal(500));
        otherAmounts.put("Swimming Pool", new BigDecimal(800));

        var dto = new UnitDto(Unit.Status.VACANT, Unit.UnitType.APARTMENT_UNIT, Unit.Identifier.A, 1, 2,
                1, 2, Unit.Currency.KES, BigDecimal.valueOf(25000), BigDecimal.valueOf(500),
                BigDecimal.valueOf(500), null, property.getId());
        var dtoUpdate = new UnitDto(Unit.Status.VACANT, Unit.UnitType.APARTMENT_UNIT, Unit.Identifier.A, 1,
                2, 1, 2, Unit.Currency.KES, BigDecimal.valueOf(26000),
                BigDecimal.valueOf(510), BigDecimal.valueOf(510), otherAmounts, "1");
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
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
                .propertyId("1").build();
        unit.setId("301");

        var dtoThatChangesUnitType = new UnitDto(Unit.Status.VACANT, Unit.UnitType.MAISONETTES, Unit.Identifier.A, 1,
                2, 1, 2, Unit.Currency.KES, BigDecimal.valueOf(25000),
                BigDecimal.valueOf(500), BigDecimal.valueOf(500), null, "1");
        var unit2 = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
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
                .propertyId("1").build();
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
        Mono<Unit> createUpdateUnit = reset()
                .then(propertyRepository.save(property))
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
                        u.getFloorNo() == null && u.getPropertyId() == null &&
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
    void find_returnsUnits_whenUnitExists() {
        // given
        String propertyId = "1";
        String name = "Luxury Apartment";
        var property = new Property();
        property.setName(name);
        property.setId(propertyId);

        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
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
                .propertyId(propertyId)
                .build();
        unit.setId("301");

        // when
        Flux<Unit> findUnit = propertyRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Properties!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(propertyRepository.save(property))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(unitService.findAll(
                        propertyId,
                        Unit.Status.VACANT,
                        "TE34",
                        Unit.UnitType.APARTMENT_UNIT,
                        Unit.Identifier.B,
                        2,
                        2,
                        1,
                        OrderType.ASC))
                .doOnNext(u -> System.out.println("---- Found " + u));

        // then
        StepVerifier
                .create(findUnit)
                .expectNextMatches(u -> u.getId().equals("301"))
                .verifyComplete();

        // given
        var unit2 = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
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
                .propertyId("1").build();
        unit2.setId("303");

        // when
        Flux<Unit> createUnitAndFindAllOnSecondFloor = unitRepository.save(unit2)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(unitService.findAll(
                        propertyId,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                       	null,
                        OrderType.DESC))
                .doOnNext(a -> System.out.println(" Found " + a));

        // then
        StepVerifier
                .create(createUnitAndFindAllOnSecondFloor)
                .expectNextMatches(u -> u.getId().equals("303") || u.getId().equals("301"))
                .expectNextMatches(u -> u.getId().equals("303") || u.getId().equals("301"))
                .verifyComplete();
    }

    @Test
    void find_returnsEmpty_whenNoUnitExists() {
        // given
        var propertyId = "5";

        // when
        Flux<Unit> findUnitNonExisting = unitService.findAll(
                        propertyId,
                        Unit.Status.VACANT,
                        "TE35",
                        Unit.UnitType.APARTMENT_UNIT,
                        Unit.Identifier.E,
                        2,
                        2,
                        1,
                        OrderType.ASC);
        // then
        StepVerifier
                .create(findUnitNonExisting)
                .expectComplete()
                .verify();
    }

    @Test
    void deleteById_returnsTrue_whenUnitExists() {
        // given
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
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
                .propertyId("1").build();
        unit.setId("9999");

        // when
        Mono<Boolean> createUnitThenDelete = reset()
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitService.deleteById("9999"));
        // then
        StepVerifier
                .create(createUnitThenDelete)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void deleteById_returnsCustomNotFoundException_whenUnitDoesNotExist() {
        // given
        var unitId = "3090";
        // when
        Mono<Boolean> deleteUnitThatDoesNotExist = unitService.deleteById(unitId);
        // then
        StepVerifier
                .create(deleteUnitThatDoesNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Unit with id %s does not exist!".formatted(unitId)))
                .verify();
    }
}