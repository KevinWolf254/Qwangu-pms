package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
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

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
class DarajaCustomerToBusinessServiceImplIntegrationTest {
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private DarajaCustomerToBusinessServiceImpl darajaCustomerToBusinessService;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    private final DarajaCustomerToBusinessDto dto = new DarajaCustomerToBusinessDto("RKTQDM7W6S", "Pay Bill", "20191122063845", "10", "600638",
            "T903", "", "49197.00", "", "254708374149", "John", "", "Doe");

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
        // given
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
        // given
        // when
        Mono<DarajaCustomerToBusinessResponse> validate = darajaCustomerToBusinessService.confirm(dto);
        // then
        StepVerifier
                .create(validate)
                .expectNextMatches(r -> (Integer) r.getCode() == 0 && r.getDescription().equals("ACCEPTED"))
                .verifyComplete();
        // then
        Flux<Payment> findAllPayments = paymentRepository.findAll();
        StepVerifier
                .create(findAllPayments)
                .expectNextMatches(p -> p.getTransactionId().equals("RKTQDM7W6S") && p.getTransactionType().equals("Pay Bill") &&
                        p.getTransactionTime() != null && p.getReferenceNo().equals("T903") && p.getBalance().equals("49197.00") &&
                        p.getMobileNumber().equals("254708374149") && p.getFirstName().equals("John") && p.getLastName().equals("Doe")
//                                &&
//                        p.getType().equals(Payment.Type.MPESA_PAY_BILL) && p.getAmount().intValue() == 10 && p.getStatus().equals(Payment.Status.RENT_NEW)
                        )
                .verifyComplete();
    }
}