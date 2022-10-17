package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.TenantRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OccupationServiceImplIntegrationTest {
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private ReactiveMongoTemplate template;
    @Autowired
    private OccupationService occupationService;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }
    private Unit unit = new Unit.UnitBuilder()
            .status(Unit.Status.VACANT)
            .booked(false)
            .accountNo("TE99")
            .type(Unit.Type.APARTMENT_UNIT)
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

    @Test
    void create() {
        // given
        String unitId = "12";
        String tenantId = "13";
        String paymentId = "15";
        var payment = new Payment(paymentId, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W67",
                "Pay Bill", LocalDateTime.now(), BigDecimal.valueOf(20000), "600638",
                "TE3490", "", "49197.00", "", "254708374147",
                "John", "", "Doe");
        var tenantDto = new TenantDto("John", "middle", "Doe","0700000000",
                "person@gmail.com");
        var dto = new OccupationForNewTenantDto(LocalDate.now(), unitId, paymentId, tenantDto);
        var dtoUnitIdNotExist = new OccupationForNewTenantDto(LocalDate.now(), null, "5", tenantDto);
        var dtoTenantIdNotExist = new OccupationForNewTenantDto(LocalDate.now(), null, unitId, tenantDto);
        var tenant = new Tenant(tenantId, "John", "middle", "Doe", "0700000000",
                "person@gmail.com", LocalDateTime.now(), null, null, null);
        var tenantActive = new Tenant("1", "John", "middle", "Doe",
                "0700000000", "person@gmail.com", LocalDateTime.now(), null, null, null);
        unit.setId(unitId);
        var occupationActive = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        // when
        Mono<Occupation> createOccupation = unitRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(tenantRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"))
//                .then(tenantRepository.save(tenant))
//                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(paymentRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Payments!"))
                .then(paymentRepository.save(payment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto))
                .doOnSuccess(a -> System.out.println("---- Saved " + a));
        // then
        StepVerifier
                .create(createOccupation)
                .expectNextMatches(o -> !o.getId().isEmpty() && !o.getTenantId().isEmpty() &&
                        o.getUnitId().equals(unitId) && o.getStatus().equals(Occupation.Status.CURRENT))
                .verifyComplete();

//        // when
//        Mono<Occupation> createWithUnitIdNonExist = occupationService.create(dtoUnitIdNotExist);
//        // then
//        StepVerifier
//                .create(createWithUnitIdNonExist)
//                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
//                        e.getMessage().equals("Unit with id 5 does not exist!"))
//                .verify();
//
//        // when
//        Mono<Occupation> createWithTenantIdNonExist = occupationService.create(dtoTenantIdNotExist);
//        // then
//        StepVerifier
//                .create(createWithTenantIdNonExist)
//                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
//                        e.getMessage().equals("Tenant already exists!"))
//                .verify();
//
//        // when
//        Mono<Occupation> occupationCurrentThrowsCustomAlreadyExistsException = tenantRepository
//                .save(tenantActive)
//                .doOnSuccess(a -> System.out.println("---- Saved " + a))
//                .then(occupationRepository.save(occupationActive))
//                .doOnSuccess(a -> System.out.println("---- Saved " + a))
//                .then(occupationService.create(dto));
//        // then
//        StepVerifier
//                .create(occupationCurrentThrowsCustomAlreadyExistsException)
//                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
//                        e.getMessage().equals("Occupation already exists!"))
//                .verify();
    }

    @Test
    void update() {
        // given
        String id = "1";
        String unitId = "12";
        String tenantId = "13";
        var dto = new VacateOccupationDto(LocalDate.now());
        var occupation = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        occupation.setId(id);
        var occupationThatIsActive = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        occupationThatIsActive.setId("2");
        var tenant = new Tenant(tenantId, "John", "middle", "Doe", "0700000000",
                "person@gmail.com", LocalDateTime.now(), null, null, null);
        unit.setId(unitId);

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
                .expectNextMatches(occ -> occ.getStatus().equals(Occupation.Status.VACATED))
                .verifyComplete();

        // when
        Mono<Occupation> updateOccupationThatIsActive = occupationRepository.save(occupationThatIsActive)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.update(id, dto));
        // then
        StepVerifier
                .create(updateOccupationThatIsActive)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Occupant already vacated!"))
                .verify();
    }

    @Test
    void findPaginated() {
        // given
        String id = "1";
        String unitId = "1";
        String tenantId= "1";
        var occupation = new Occupation.OccupationBuilder()
                .tenantId(tenantId)
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        occupation.setId(id);
        var occupation2 = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        occupation2.setId("2");
        //when
        Flux<Occupation> findOccupation = occupationRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(occupationService.findPaginated(Optional.of(Occupation.Status.CURRENT),
                        Optional.of(unitId), Optional.of(tenantId), 1, 10, OrderType.ASC));
        // then
        StepVerifier
                .create(findOccupation)
                .expectNextMatches(o -> o.getStatus().equals(Occupation.Status.CURRENT) &&o.getId().equals(id))
                .verifyComplete();

        // when
        Flux<Occupation> findOccupationNotExist = occupationService.findPaginated(Optional.of(Occupation.Status.CURRENT),
                Optional.of("13"), Optional.of("14"), 1, 10, OrderType.ASC);
        // then
        StepVerifier
                .create(findOccupationNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Occupations were not found!"))
                .verify();

        // when
        Flux<Occupation> findAll = occupationRepository.save(occupation2)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(occupationService.findPaginated(Optional.empty(), Optional.empty(),
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
        String unitId = "1";
        String tenantId= "1";
        var occupation = new Occupation.OccupationBuilder()
                .tenantId(tenantId)
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        occupation.setId(id);
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