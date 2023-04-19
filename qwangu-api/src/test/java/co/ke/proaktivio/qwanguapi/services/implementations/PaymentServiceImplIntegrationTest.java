package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;

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

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentServiceImplIntegrationTest {
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private PaymentService underTest;
	
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
    void create_returnsCustomBadRequestException_whenReferenceNumberAlreadyExists() {
    	// given
    	String referenceNumber = "123456";
		var payment = new Payment.PaymentBuilder()
    			.referenceNumber(referenceNumber)
    			.build();
    	var paymentWithSimilarRefNo = new Payment.PaymentBuilder()
    			.referenceNumber(referenceNumber)
    			.build();
    	// when
		Mono<Payment> alreadyExists = paymentRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("Deleted all payments!"))
				.then(paymentRepository.save(payment))
				.doOnSuccess(p -> System.out.println("Created: " + p))
				.then(underTest.create(paymentWithSimilarRefNo));
		// then
		StepVerifier
			.create(alreadyExists)
			.expectErrorMatches(e -> e instanceof CustomBadRequestException
					&& e.getMessage().equals("Payment already exists with reference no. %s!".formatted(referenceNumber)))
			.verify();
    }

    @Test
    void create_returnsPayment_whenSuccessful() {
    	// given
    	String referenceNumber = "123456";
		var payment = new Payment.PaymentBuilder()
				.type(PaymentType.MOBILE)
				.occupationNumber("ASD")
    			.referenceNumber(referenceNumber)
    			.currency(Currency.KES)
    			.amount(BigDecimal.valueOf(20000l))
    			.build();
    	// when
		Mono<Payment> success = paymentRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("Deleted all payments!"))
				.then(underTest.create(payment));
		// then
		StepVerifier
			.create(success)
			.expectNextMatches(p -> p.getId() != null && p.getCreatedOn() != null 
			&& p.getAmount().longValue() == 20000l && p.getReferenceNumber().equals(referenceNumber)
			&& p.getCreatedOn() != null && p.getModifiedOn() != null)
			.verifyComplete();
    }

    @Test
    void update_returnsCustomBadRequestException_whenIdDoesNotExist() {
    	// given
    	String referenceNumber = "123456";
		var idDoesNotExist = new Payment.PaymentBuilder()
    			.referenceNumber(referenceNumber)
    			.build();
		idDoesNotExist.setId("23");
    	// when
		Mono<Payment> alreadyExists = paymentRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("Deleted all payments!"))
				.then(underTest.update(idDoesNotExist));
		// then
		StepVerifier
			.create(alreadyExists)
			.expectErrorMatches(e -> e instanceof CustomNotFoundException
					&& e.getMessage().equals("Payment with id $s does not exist!".formatted("23")))
			.verify();
    }

    @Test
    void update_returnsPayment_whenIdSuccessful() {
    	// given
    	String referenceNumber = "123456";
		var payment = new Payment.PaymentBuilder()
				.type(PaymentType.MOBILE)
				.occupationNumber("ASD")
    			.referenceNumber(referenceNumber)
    			.currency(Currency.KES)
    			.amount(BigDecimal.valueOf(20000l))
    			.build();
    	// when
		Mono<Payment> success = paymentRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("Deleted all payments!"))
				.then(underTest.create(payment))
				.map(p -> {
					p.setStatus(PaymentStatus.CLAIMED);
					return p;
				})
				.flatMap(p -> underTest.update(p));
		// then
		StepVerifier
			.create(success)
			.expectNextMatches(p -> p.getStatus().equals(PaymentStatus.CLAIMED) && p.getId() != null && p.getCreatedOn() != null 
			&& p.getAmount().longValue() == 20000l && p.getReferenceNumber().equals(referenceNumber)
			&& p.getCreatedOn() != null && p.getModifiedOn() != null)
			.verifyComplete();
    }

	@Test
	void findAll_returnsEmpty_whenPaymentDoesNotExist() {
		// when
		Flux<Payment> isEmpty = paymentRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("Deleted all payments!"))
				.thenMany(underTest.findAll(null, null, null, null));

		// then
		StepVerifier.create(isEmpty).verifyComplete();
	}

	@Test
	void findAll_returnsPayment_whenSuccessful() {
    	// given
    	String referenceNumber = "123456";
		var payment = new Payment.PaymentBuilder()
				.type(PaymentType.MOBILE)
				.occupationNumber("ASD")
    			.referenceNumber(referenceNumber)
    			.currency(Currency.KES)
    			.amount(BigDecimal.valueOf(20000l))
    			.build();
		// when
		Flux<Payment> success = paymentRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("Deleted all payments!"))
				.then(underTest.create(payment))
				.thenMany(underTest.findAll(PaymentStatus.UNCLAIMED, PaymentType.MOBILE, referenceNumber, OrderType.ASC));

		// then
		StepVerifier
			.create(success)
			.expectNextMatches(p -> p.getStatus().equals(PaymentStatus.UNCLAIMED) && p.getId() != null && p.getCreatedOn() != null 
			&& p.getAmount().longValue() == 20000l && p.getReferenceNumber().equals(referenceNumber)
			&& p.getCreatedOn() != null && p.getModifiedOn() != null)
			.verifyComplete();
	}
}
