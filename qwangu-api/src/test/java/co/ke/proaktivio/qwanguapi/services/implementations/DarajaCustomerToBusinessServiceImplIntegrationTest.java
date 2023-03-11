package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.pojos.MpesaC2BDto;
import co.ke.proaktivio.qwanguapi.repositories.*;
import co.ke.proaktivio.qwanguapi.services.MpesaC2BService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
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
    private InvoiceRepository invoiceRepository;
    @Autowired
    private OccupationTransactionRepository occupationTransactionRepository;
    @Autowired
    private MpesaC2BService darajaCustomerToBusinessService;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

    private final MpesaC2BDto dto = new MpesaC2BDto("RKTQDM7W6S",
            "Pay Bill", "20191122063845", "10", "600638",
            "T903", "", "49197.00", "", "254708374149",
            "John", "", "Doe");

    private final MpesaC2BDto dtoBooking = new MpesaC2BDto("RKTQDM7W67",
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

//    @Test
//    void processBooking() {
//        // given
//        var now = LocalDateTime.now();
//        var today = LocalDate.now();
//        var payment = new Payment(null, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W67",
//                "Pay Bill", LocalDateTime.now(), BigDecimal.valueOf(20000), "600638",
//                "BOOKTE34", "", "49197.00", "", "254708374147",
//                "John", "", "Doe");
//        var unit = new Unit.UnitBuilder()
//                .status(Unit.Status.OCCUPIED)
//                .booked(false)
//                .accountNo("TE34")
//                .type(Unit.Type.APARTMENT_UNIT)
//                .identifier(Unit.Identifier.B)
//                .floorNo(2)
//                .noOfBedrooms(2)
//                .noOfBathrooms(1)
//                .advanceInMonths(2)
//                .currency(Unit.Currency.KES)
//                .rentPerMonth(BigDecimal.valueOf(27000))
//                .securityPerMonth(BigDecimal.valueOf(510))
//                .garbagePerMonth(BigDecimal.valueOf(300))
//                .apartmentId("1").build();
//        unit.setId("1");
//        var occupation = new Occupation.OccupationBuilder()
//                .tenantId("1")
//                .startedOn(LocalDateTime.now())
//                .status(Occupation.Status.CURRENT).unitId("1")
//                .build();
//        occupation.setId("1");
//        var notice = new Notice("1", true, now, today.plusDays(40), now, null,
//                null, null, "1");
//
//        // when
//        Mono<Payment> processBookingPayment = unitRepository.deleteAll()
//                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
//                .then(occupationRepository.deleteAll())
//                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
//                .then(noticeRepository.deleteAll())
//                .doOnSuccess(t -> System.out.println("---- Deleted all notices!"))
//                .then(unitRepository.save(unit))
//                .doOnSuccess(u -> System.out.println("---- Created: " + u))
//                .then(occupationRepository.save(occupation))
//                .doOnSuccess(u -> System.out.println("---- Created: " + u))
//                .then(noticeRepository.save(notice))
//                .doOnSuccess(u -> System.out.println("---- Created: " + u))
//                .then(paymentRepository.save(payment))
//                .doOnSuccess(u -> System.out.println("---- Created: " + u))
//                .flatMap(p -> darajaCustomerToBusinessService.processBooking(p));
//        // then
//        StepVerifier
//                .create(processBookingPayment)
//                .expectNextMatches(p -> p.getStatus().equals(Payment.Status.PROCESSED))
//                .verifyComplete();
//
//        // when
//        Mono<Unit> findUnit = unitRepository.findById(unit.getId());
//        // then
//        StepVerifier
//                .create(findUnit)
//                .expectNextMatches(Unit::getIsBooked)
//                .verifyComplete();
//    }

    @Test
    void makePayment_returnReceiptAndCreateCreditOccupationTransaction_whenSuccessful() {
        // given
        LocalDate now = LocalDate.now();
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.OCCUPIED)
//                .booked(false)
                .number("TE3489")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.A)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .propertyId("1").build();
        unit.setId("1");
        var occupation = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(now)
                .unitId("1")
                .build();
        occupation.setId("1");
        occupation.setStatus(Occupation.Status.CURRENT);
        occupation.setNumber("YRT2345");
        var invoice = new Invoice.InvoiceBuilder()
                .type(Invoice.Type.RENT)
                .number(new Invoice(), occupation)
                .startDate(LocalDate.now())
                .rentAmount(BigDecimal.valueOf(27000))
                .securityAmount(BigDecimal.valueOf(500))
                .garbageAmount(BigDecimal.valueOf(300))
                .occupationId(occupation.getId())
                .build();
        invoice.setId("1");
        var occupationTransaction = new OccupationTransaction.OccupationTransactionBuilder()
                .type(OccupationTransaction.Type.DEBIT)
                .occupationId(occupation.getId())
                .invoiceId("1")
                .totalAmountOwed(BigDecimal.valueOf(27800))
                .totalAmountPaid(BigDecimal.ZERO)
                .totalAmountCarriedForward(BigDecimal.valueOf(27800))
                .build();

        var payment = new Payment.PaymentBuilder()
                .status(Payment.Status.NEW)
                .type(Payment.Type.MPESA_PAY_BILL)
                .transactionId("RKTQDM7W67")
                .transactionType("Pay Bill")
                .transactionTime(LocalDateTime.now())
                .currency(Unit.Currency.KES)
                .amount(BigDecimal.valueOf(30000))
                .shortCode("600638")
                .referenceNo("YRT2345")
                .balance("49197.00")
                .mobileNumber("254708374147")
                .firstName("John")
                .build();

        var unit2 = new Unit.UnitBuilder()
                .status(Unit.Status.OCCUPIED)
                .number("TE3490")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.B)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .propertyId("1").build();
        unit2.setId("2");
        var occupation2 = new Occupation.OccupationBuilder()
                .tenantId("2")
                .unitId("2")
                .startDate(now)
                .unitId("1")
                .build();
        occupation2.setId("2");
        occupation2.setStatus(Occupation.Status.CURRENT);
        occupation2.setNumber("B23756");

        var paymentForNonExistingOccupationNo = new Payment.PaymentBuilder()
                .status(Payment.Status.NEW)
                .type(Payment.Type.MPESA_PAY_BILL)
                .transactionId("RKTQDM7W67")
                .transactionType("Pay Bill")
                .transactionTime(LocalDateTime.now())
                .currency(Unit.Currency.KES)
                .amount(BigDecimal.valueOf(20000))
                .shortCode("600638")
                .referenceNo("AFDER345345")
                .balance("49197.00")
                .mobileNumber("254708374147")
                .firstName("John")
                .build();

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
                .then(invoiceRepository.save(invoice))
                .doOnNext(t -> System.out.println("---- Created: " + t))
                .then(occupationTransactionRepository.save(occupationTransaction))
                .doOnSuccess(t -> System.out.println("---- Created: " + t))
                .thenMany(paymentRepository.saveAll(List.of(payment, paymentForNonExistingOccupationNo)))
                .doOnNext(t -> System.out.println("---- Created: " + t))
                .then(darajaCustomerToBusinessService.processPayment(payment));
        // then
        StepVerifier
                .create(processPayments)
                .expectNextMatches(pmt -> !pmt.getId().isEmpty() && pmt.getTransactionId().equals(payment.getTransactionId()) &&
                        pmt.getReferenceNo().equals(occupation.getNumber()) && pmt.getStatus().equals(Payment.Status.PROCESSED))
                .verifyComplete();

        Mono<Payment> expectNull = darajaCustomerToBusinessService.processPayment(paymentForNonExistingOccupationNo);

        // then
        StepVerifier
                .create(expectNull)
                .verifyComplete();

        // then
        Flux<OccupationTransaction> expectTwoOccupationTransactions = occupationTransactionRepository
                .findAll(Sort.by(Sort.Order.desc("createdOn")))
                .doOnNext(t -> System.out.println("---- Found: " + t));

        StepVerifier
                .create(expectTwoOccupationTransactions)
                .expectNextMatches(ot ->
                        ot.getTotalAmountPaid().equals(BigDecimal.valueOf(30000)) &&
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(2200)) &&
                        ot.getOccupationId().equals("1"))
                .expectNextMatches(ot ->
                        ot.getTotalAmountOwed().equals(BigDecimal.valueOf(27800)) &&
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(27800)) &&
                        ot.getOccupationId().equals("1"))
                .verifyComplete();
    }
}