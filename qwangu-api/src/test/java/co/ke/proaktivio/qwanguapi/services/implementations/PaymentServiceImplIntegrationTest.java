package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
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
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentServiceImplIntegrationTest {
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private PaymentRepository paymentRepository;
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

    @BeforeEach
    void deleteAll() {
        paymentRepository.deleteAll()
                .doOnSuccess(r -> System.out.println("---- Deleted all payments!"))
                .subscribe();
    }

    @Test
    void findPaginated() {
        // given
//        var payment = new Payment(null, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W67",
//                "Pay Bill", LocalDateTime.now(), BigDecimal.valueOf(20000), "600638",
//                "TE34", "", "49197.00", "", "254708374147",
//                "John", "", "Doe");
//        var payment2 = new Payment(null, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W77",
//                "Pay Bill", LocalDateTime.now(), BigDecimal.valueOf(20000), "600638",
//                "BOOKTE34", "", "49197.00", "", "254708374147",
//                "John", "", "Doe");

        var payment = new Payment.PaymentBuilder()
                .status(Payment.Status.NEW)
                .type(Payment.Type.MPESA_PAY_BILL)
                .transactionId("RKTQDM7W67")
                .transactionType("Pay Bill")
                .transactionTime(LocalDateTime.now())
                .currency(Unit.Currency.KES)
                .amount(BigDecimal.valueOf(20000))
                .shortCode("600638")
                .referenceNo("TE34")
                .balance("49197.00")
                .mobileNumber("254708374147")
                .firstName("John")
                .build();
        var payment2 = new Payment.PaymentBuilder()
                .status(Payment.Status.NEW)
                .type(Payment.Type.MPESA_PAY_BILL)
                .transactionId("RKTQDM7W77")
                .transactionType("Pay Bill")
                .transactionTime(LocalDateTime.now())
                .currency(Unit.Currency.KES)
                .amount(BigDecimal.valueOf(20000))
                .shortCode("600638")
                .referenceNo("BOOKTE34")
                .balance("49197.00")
                .mobileNumber("254708374147")
                .firstName("John")
                .build();
        // when
        Flux<Payment> saveAll = paymentRepository.saveAll(List.of(payment, payment2))
                .doOnNext(p -> System.out.println("---- Created: " + p))
                .thenMany(paymentService.findAll(Optional.empty(),
                        Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                        OrderType.DESC))
                .doOnNext(p -> System.out.println("---- Found: " +p));
        // then
        StepVerifier
                .create(saveAll)
                .expectNextMatches(p -> p.getReferenceNo().equals("BOOKTE34"))
                .expectNextMatches(p -> p.getReferenceNo().equals("TE34"))
                .verifyComplete();
    }
}