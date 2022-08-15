package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import co.ke.proaktivio.qwanguapi.repositories.NoticeRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.DarajaCustomerToBusinessService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
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

@Testcontainers
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
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
    private DarajaCustomerToBusinessService darajaCustomerToBusinessService;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    private final DarajaCustomerToBusinessDto dto = new DarajaCustomerToBusinessDto("RKTQDM7W6S", "Pay Bill", "20191122063845", "10", "600638",
            "T903", "", "49197.00", "", "254708374149", "John", "", "Doe");

    private final DarajaCustomerToBusinessDto dtoBooking = new DarajaCustomerToBusinessDto("RKTQDM7W67", "Pay Bill", "20191122063845", "10", "600638",
            "BOOKT903", "", "49197.00", "", "254708374147", "John", "", "Doe");

    private final Unit unit = new Unit("9999", Unit.Status.VACANT, false, "TE99", Unit.Type.APARTMENT_UNIT, Unit.Identifier.B,
            2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");

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
        Mono<DarajaCustomerToBusinessResponse> validate = darajaCustomerToBusinessService.validate(dto);
        // then
        StepVerifier
                .create(validate)
                .expectNextMatches(r -> (Integer) r.getCode() == 0 && r.getDescription().equals("ACCEPTED"))
                .verifyComplete();
    }

    @Test
    void confirm() {
        // when
        Mono<DarajaCustomerToBusinessResponse> confirmRentPayment = darajaCustomerToBusinessService.confirm(dto);
        // then
        StepVerifier
                .create(confirmRentPayment)
                .expectNextMatches(r -> (Integer) r.getCode() == 0 && r.getDescription().equals("ACCEPTED"))
                .verifyComplete();

        // when
        Mono<DarajaCustomerToBusinessResponse> confirmBookingPayment = darajaCustomerToBusinessService.confirm(dtoBooking);
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
        var payment = new Payment(null, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W67", "Pay Bill",
                LocalDateTime.now(), BigDecimal.valueOf(20000), "600638", "BOOKTE34", "", "49197.00", "", "254708374147",
                "John", "", "Doe", LocalDateTime.now(), null);
        var unit = new Unit("1", Unit.Status.OCCUPIED, false, "TE34", Unit.Type.APARTMENT_UNIT, Unit.Identifier.B,
                2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");
        var occupation = new Occupation("1", Occupation.Status.CURRENT, LocalDateTime.now(), null, "1", "1", LocalDateTime.now(), null);
        var notice = new Notice("1", true, now, today.plusDays(40), now, null, "1");

        // when
        Mono<Unit> processBookingPayment = unitRepository.save(unit)
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
                .expectNextMatches(Unit::getIsBooked)
                .verifyComplete();
    }
}