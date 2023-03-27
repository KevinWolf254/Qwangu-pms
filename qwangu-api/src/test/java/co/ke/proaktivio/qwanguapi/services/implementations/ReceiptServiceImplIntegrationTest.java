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
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction.OccupationTransactionType;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.ReceiptRepository;
import co.ke.proaktivio.qwanguapi.services.ReceiptService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReceiptServiceImplIntegrationTest {
	@Autowired
	private ReceiptService receiptService;
	@Autowired
	private ReceiptRepository receiptRepository;
	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private OccupationRepository occupationRepository;
	@Autowired
	private OccupationTransactionRepository occupationTransactionRepository;
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

	private Mono<Void> reset() {
		return paymentRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("--- Deleted payments!"))
				.then(receiptRepository.deleteAll())
				.doOnSuccess($ -> System.out.println("--- Deleted receipts!"))
				.then(occupationTransactionRepository.deleteAll())
				.doOnSuccess($ -> System.out.println("--- Deleted Occupation Transactions!"))
				.then(occupationRepository.deleteAll())
				.doOnSuccess($ -> System.out.println("--- Deleted Occupations!"));
	}
	
	@Test
	void create_returnsCustomBadRequestException_whenOccupationDoesNotExist() {
		// given
		var occupationId = "1";
		var dto = new ReceiptDto();
		dto.setOccupationId(occupationId);
		dto.setPaymentId("1");
		
		// when
		Mono<Receipt> createReceipt = reset()
				.then(occupationRepository.deleteAll())
				.doOnSuccess($ -> System.out.println("--- Deleted occupations!"))
				.then(receiptRepository.deleteAll())
				.doOnSuccess($ -> System.out.println("--- Deleted payments!"))
				.then(receiptRepository.deleteAll())
				.doOnSuccess($ -> System.out.println("--- Deleted receipts!")).then(receiptService.create(dto));
		
		// then
		StepVerifier.create(createReceipt)
				.expectErrorMatches(e -> e instanceof CustomBadRequestException
						&& e.getMessage().equals("Occupation with id %s does not exist!".formatted(occupationId)))
				.verify();
	}

	@Test
	void create_returnsCustomBadRequestException_whenPaymentDoesNotExist() {
		// given
		var occupationId = "1";
		var occupation = new Occupation();
		occupation.setId(occupationId);
		occupation.setTenantId("1");
		occupation.setUnitId("1");

		var paymentId = "1";
		var dto = new ReceiptDto();
		dto.setOccupationId(occupationId);
		dto.setPaymentId(paymentId);
		
		// when
		Mono<Receipt> createReceipt = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: " + o)).then(receiptService.create(dto));
		
		// then
		StepVerifier.create(createReceipt).expectErrorMatches(e -> e instanceof CustomBadRequestException
				&& e.getMessage().equals("Payment with id %s does not exist!".formatted(paymentId)))
		.verify();
	}
	
	@Test
	void create_returnsReceipt_whenSuccessful() {
		// given
		var occupationId = "1";
		var occupation = new Occupation();
		occupation.setId(occupationId);
		occupation.setTenantId("1");
		occupation.setUnitId("1");
		
		var paymentId = "1";
		var payment = new Payment();
		payment.setId(paymentId);
		payment.setAmount(BigDecimal.valueOf(25000l));
		
		var dto = new ReceiptDto();
		dto.setOccupationId(occupationId);
		dto.setPaymentId(paymentId);

		// when
		Mono<Receipt> createReceipt = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: " + o))
				.then(paymentRepository.save(payment))
				.doOnSuccess(p -> System.out.println("--- Created: " + p))
				.then(receiptService.create(dto));
		
		// then
		StepVerifier.create(createReceipt)
				.expectNextMatches(r -> r.getId() != null && r.getNumber() != null
						&& r.getOccupationId().equals(occupationId) && r.getPaymentId().equals(paymentId))
				.verifyComplete();		
	}

	@Test
	void create_returnsCustomBadRequestException_whenPaymentIsLinkedToOtherReceipt() {
		// given
		var occupationId = "1";
		var paymentId = "1";

		var dtoWithSamePayment = new ReceiptDto();
		dtoWithSamePayment.setOccupationId(occupationId);
		dtoWithSamePayment.setPaymentId(paymentId);
		
		// when
		create_returnsReceipt_whenSuccessful();
		Mono<Receipt> createReceipt = receiptService.create(dtoWithSamePayment);
		
		// then
		StepVerifier.create(createReceipt).expectErrorMatches(e -> e instanceof CustomBadRequestException
				&& e.getMessage().equals("Payment with id %s already used!".formatted(paymentId)))
		.verify();
	}

	@Test
	void create_createsOccupationTransaction_whenSuccessfull() {
		// given
		create_returnsReceipt_whenSuccessful();
		// when
		Flux<OccupationTransaction> findOccupationTransaction = occupationTransactionRepository.findAll()
		.doOnNext(ot -> System.out.println("--- Found: "+ot));
		// then
		StepVerifier.create(findOccupationTransaction)
				.expectNextMatches(ot -> ot.getId() != null && ot.getType().equals(OccupationTransactionType.CREDIT)
						&& ot.getOccupationId().equals("1") && ot.getInvoiceId() == null && ot.getReceiptId() != null
						&& ot.getTotalAmountOwed().intValue() == 0 && ot.getTotalAmountPaid().intValue() == 25000
						&& ot.getTotalAmountCarriedForward().intValue() == 25000 && ot.getCreatedOn() != null
						&& ot.getCreatedBy().equals("SYSTEM") && ot.getModifiedOn() != null
						&& ot.getModifiedBy().equals("SYSTEM"))
				.verifyComplete();
	}
	
	@Test
	void findById_returnsEmpty_whenReceiptDoesNotExist() {
		// given
		var receiptId = "2001";
		// when
		Mono<Receipt> findNonExistant = reset()
				.then(receiptService.findById(receiptId))
				.doOnNext(r -> System.out.println("-- Found: " + r));
		// then
		StepVerifier.create(findNonExistant).verifyComplete();		
	}
	
	@Test
	void findById_returnReceipt_whenSuccessful() {
		// given
		var receiptId = "1";
		var receipt = new Receipt();
		receipt.setId(receiptId);
		// when
		Mono<Receipt> find = reset()
				.then(receiptRepository.save(receipt))
				.doOnSuccess(r -> System.out.println("--- Created: "+r))
				.then(receiptService.findById(receiptId))
				.doOnNext(r -> System.out.println("-- Found: " + r));
		// then
		StepVerifier.create(find).expectNextMatches(r -> r.getId().equals(receiptId)).verifyComplete();	
	}

	@Test
	void findAll_returnsEmpty_whenNoReceiptExists() {
		// when
		Flux<Receipt> findNon = reset()
				.thenMany(receiptService.findAll(null, null, null))
				.doOnNext(r -> System.out.println("--- Found: " + r));
		// then
		StepVerifier.create(findNon).verifyComplete();
	}

	@Test
	void findAll_returnsReceipt_whenSuccessful() {
		// given
		var occupationId = "1";
		var paymentId = "1";
		create_returnsReceipt_whenSuccessful();
		// when
		Flux<Receipt> findAll = receiptService.findAll(occupationId, paymentId, OrderType.DESC)
				.doOnNext(r -> System.out.println("--- Found: " + r));
		// then
		StepVerifier.create(findAll).expectNextMatches(r -> r.getId() != null
				&& r.getOccupationId().equals(occupationId) && r.getPaymentId().equals(paymentId))
			.verifyComplete();
	}

}
