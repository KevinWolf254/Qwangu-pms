package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import co.ke.proaktivio.qwanguapi.repositories.*;
import co.ke.proaktivio.qwanguapi.services.DarajaCustomerToBusinessService;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class DarajaCustomerToBusinessServiceImplIntegrationTest {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private OccupationTransactionRepository occupationTransactionRepository;
    @Autowired
    private DarajaCustomerToBusinessService darajaCustomerToBusinessService;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

    private final DarajaCustomerToBusinessDto dto = new DarajaCustomerToBusinessDto("RKTQDM7W6S",
            "Pay Bill", "20191122063845", "10", "600638",
            "T903", "", "49197.00", "", "254708374149",
            "John", "", "Doe");

    private final DarajaCustomerToBusinessDto dtoBooking = new DarajaCustomerToBusinessDto("RKTQDM7W67",
            "Pay Bill", "20191122063845", "10", "600638",
            "BOOKT903", "", "49197.00", "", "254708374147",
            "John", "", "Doe");

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @BeforeEach
    void resetDb() {
        paymentRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all payments!"))
                .subscribe();
    }

    @Test
    void validate() {
        // when
        var validate = darajaCustomerToBusinessService.validate(dto);
        // then
        StepVerifier
                .create(validate)
                .expectNextMatches(r -> (Integer) r.getCode() == 0 && r.getDescription().equals("ACCEPTED"))
                .verifyComplete();
    }

    @Test
    void confirm() {
        // when
        var confirmRentPayment = darajaCustomerToBusinessService.confirm(dto);
        // then
        StepVerifier
                .create(confirmRentPayment)
                .expectNextMatches(r -> (Integer) r.getCode() == 0 && r.getDescription().equals("ACCEPTED"))
                .verifyComplete();

        // when
        var confirmBookingPayment = darajaCustomerToBusinessService.confirm(dtoBooking);
        // then
        StepVerifier
                .create(confirmBookingPayment)
                .expectNextMatches(r -> (Integer) r.getCode() == 0 && r.getDescription().equals("ACCEPTED"))
                .verifyComplete();
        // then
        Flux<Payment> findAllPayments = paymentRepository.findAll();
        StepVerifier
                .create(findAllPayments)
                .expectNextMatches(p -> p.getTransactionId().equals("RKTQDM7W6S") && p.getTransactionType().equals("Pay Bill") &&
                        p.getTransactionTime() != null && p.getReferenceNo().equals("T903") && p.getBalance().equals("49197.00") &&
                        p.getMobileNumber().equals("254708374149") && p.getFirstName().equals("John") && p.getLastName().equals("Doe"))
                .expectNextMatches(p -> p.getTransactionId().equals("RKTQDM7W67") && p.getTransactionType().equals("Pay Bill") &&
                        p.getTransactionTime() != null && p.getReferenceNo().equals("BOOKT903") && p.getBalance().equals("49197.00") &&
                        p.getMobileNumber().equals("254708374147") && p.getFirstName().equals("John") && p.getLastName().equals("Doe") &&
                        p.getAmount().equals(BigDecimal.valueOf(10.0)))
                .verifyComplete();
    }

    @Test
    void processBooking() {
        // given
        var now = LocalDateTime.now();
        var today = LocalDate.now();
        var payment = new Payment(null, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W67",
                "Pay Bill", LocalDateTime.now(), BigDecimal.valueOf(20000), "600638",
                "BOOKTE34", "", "49197.00", "", "254708374147",
                "John", "", "Doe", LocalDateTime.now(), null);
//        var unit = new Unit("1", Unit.Status.OCCUPIED, false, "TE34", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.B, 2, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(27000), BigDecimal.valueOf(510), BigDecimal.valueOf(300), LocalDateTime.now(),
//                null, "1");
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.OCCUPIED)
                .booked(false)
                .accountNo("TE34")
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
        unit.setId("1");

        var occupation = new Occupation("1", Occupation.Status.CURRENT, LocalDateTime.now(), null,
                "1", "1", LocalDateTime.now(), null, null, null);
        var notice = new Notice("1", true, now, today.plusDays(40), now, null,
                null, null, "1");

        // when
        Mono<Payment> processBookingPayment = unitRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(noticeRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all notices!"))
                .then(unitRepository.save(unit))
                .doOnSuccess(u -> System.out.println("---- Created: " + u))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(u -> System.out.println("---- Created: " + u))
                .then(noticeRepository.save(notice))
                .doOnSuccess(u -> System.out.println("---- Created: " + u))
                .then(paymentRepository.save(payment))
                .doOnSuccess(u -> System.out.println("---- Created: " + u))
                .flatMap(p -> darajaCustomerToBusinessService.processBooking(p));
        // then
        StepVerifier
                .create(processBookingPayment)
                .expectNextMatches(p -> p.getStatus().equals(Payment.Status.PROCESSED))
                .verifyComplete();

        // when
        Mono<Unit> findUnit = unitRepository.findById(unit.getId());
        // then
        StepVerifier
                .create(findUnit)
                .expectNextMatches(Unit::getIsBooked)
                .verifyComplete();
    }

    @Test
    void processRent() {
        // given
        LocalDateTime now = LocalDateTime.now();
//        var unit = new Unit("1", Unit.Status.OCCUPIED, false, "TE3489", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.A, 2, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(27000), BigDecimal.valueOf(500), BigDecimal.valueOf(300), now,
//                null, "1");

        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.OCCUPIED)
                .booked(false)
                .accountNo("TE3489")
                .type(Unit.Type.APARTMENT_UNIT)
                .identifier(Unit.Identifier.A)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        unit.setId("1");
        var occupation = new Occupation("1", Occupation.Status.CURRENT, now, null,
                "1", unit.getId(), now, null, null, null);
        var occupationTransaction = new OccupationTransaction("1", OccupationTransaction.Type.DEBIT,
                BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.valueOf(5000), occupation.getId(), null,
                "1", null);
        var payment = new Payment(null, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W67",
                "Pay Bill", LocalDateTime.now(), BigDecimal.valueOf(20000), "600638",
                "TE3489", "", "49197.00", "", "254708374147",
                "John", "", "Doe", LocalDateTime.now(), null);

//        var unit2 = new Unit("2", Unit.Status.OCCUPIED, false, "TE3490", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.B, 2, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(27000), BigDecimal.valueOf(500), BigDecimal.valueOf(300), now,
//                null, "1");
        var unit2 = new Unit.UnitBuilder()
                .status(Unit.Status.OCCUPIED)
                .booked(false)
                .accountNo("TE3490")
                .type(Unit.Type.APARTMENT_UNIT)
                .identifier(Unit.Identifier.B)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        unit2.setId("2");
        var occupation2 = new Occupation("2", Occupation.Status.CURRENT, now, null, "2", unit2.getId(),
                now, null, null, null);
        var payment2 = new Payment(null, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W67",
                "Pay Bill", LocalDateTime.now(), BigDecimal.valueOf(20000), "600638",
                "TE3490", "", "49197.00", "", "254708374147",
                "John", "", "Doe", LocalDateTime.now(), null);

        Mono<Payment> processPayments = unitRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(occupationTransactionRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all OccupationTransactions!"))
                .thenMany(unitRepository.saveAll(List.of(unit, unit2)))
                .doOnNext(t -> System.out.println("---- Created: " + t))
                .thenMany(occupationRepository.saveAll(List.of(occupation, occupation2)))
                .doOnNext(t -> System.out.println("---- Created: " + t))
                .then(occupationTransactionRepository.save(occupationTransaction))
                .doOnSuccess(t -> System.out.println("---- Created: " + t))
                .then(darajaCustomerToBusinessService.processRent(payment))
                .then(darajaCustomerToBusinessService.processRent(payment2));
        // then
        StepVerifier
                .create(processPayments)
                .expectNextCount(1)
                .verifyComplete();

        // then
        Flux<OccupationTransaction> allOccupationTransactions = occupationTransactionRepository.findAll();
        StepVerifier
                .create(allOccupationTransactions)
                .expectNextMatches(ot -> (ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(15000)) &&
                        ot.getOccupationId().equals("1")) || (
                                ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(20000)) &&
                        ot.getOccupationId().equals("2")) || (
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(5000)) &&
                                ot.getOccupationId().equals("1")))
                .expectNextMatches(ot -> (ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(15000)) &&
                        ot.getOccupationId().equals("1")) || (
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(20000)) &&
                                ot.getOccupationId().equals("2")) || (
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(5000)) &&
                                ot.getOccupationId().equals("1")))
                .expectNextMatches(ot -> (ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(15000)) &&
                        ot.getOccupationId().equals("1")) || (
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(20000)) &&
                                ot.getOccupationId().equals("2")) || (
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(5000)) &&
                                ot.getOccupationId().equals("1")))
                .verifyComplete();

        // then
        Flux<Payment> allPayments = paymentRepository.findAll();
        StepVerifier
                .create(allPayments)
                .expectNextCount(2)
                .verifyComplete();
    }
}