package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.util.HashMap;

import org.jetbrains.annotations.NotNull;
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
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction.Type;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.CreditTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.DebitTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.ReceiptRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OccupationTransactionServiceImplIntegrationTest {
	@Autowired
	private OccupationTransactionRepository occupationTransactionRepository;
	@Autowired
	private OccupationTransactionService occupationTransactionService;
	@Autowired
	private OccupationRepository occupationRepository;
	@Autowired
	private InvoiceRepository invoiceRepository;
	@Autowired
	private ReceiptRepository receiptRepository;
	@Autowired
	private PaymentRepository paymentRepository;
	
	@MockBean
	private BootstrapConfig bootstrapConfig;
	@MockBean
	private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(
			DockerImageName.parse("mongo:latest"));

	@DynamicPropertySource
	public static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
	}

	@NotNull
	private Mono<Void> reset() {
		return occupationTransactionRepository.deleteAll()
				.doOnSuccess(t -> System.out.println("---- Deleted all OccupationTransactions!"))
				.then(occupationRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
				.then(invoiceRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Invoices!"))
				.then(receiptRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Receipts!"))
				.then(paymentRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Payments!"));
	}
	
	@Test
	void createDebitTransaction_returnsCustomNotFoundException_whenOccupationIdDoesNotExist() {
		// given
		var dto = new DebitTransactionDto();
		String occupationId = "2000";
		dto.setOccupationId(occupationId);
		dto.setInvoiceId("1000");
		// when
		Mono<OccupationTransaction> createOccupationIdDoesNotExist = reset()
				.then(occupationTransactionService.createDebitTransaction(dto));
		// then
		StepVerifier
			.create(createOccupationIdDoesNotExist)
			.expectErrorMatches(e -> e instanceof CustomNotFoundException &&
					e.getMessage().equals("Occupation with id %s does not exist!".formatted(occupationId)))
			.verify();
	}
	
	@Test
	void createDebitTransaction_returnsCustomNotFoundException_whenInvoiceIdDoesNotExist() {
		// given
		String occupationId = "2000";
		var occupation = new Occupation();
		occupation.setId(occupationId);
		var dto = new DebitTransactionDto();
		dto.setOccupationId(occupationId);
		String invoiceId = "1000";
		dto.setInvoiceId(invoiceId);
		// when
		Mono<OccupationTransaction> createInvoiceIdDoesNotExist = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationTransactionService.createDebitTransaction(dto));
		// then
		StepVerifier
			.create(createInvoiceIdDoesNotExist)
			.expectErrorMatches(e -> e instanceof CustomNotFoundException &&
					e.getMessage().equals("Invoice with id %s does not exist!".formatted(invoiceId)))
			.verify();
	}
	
	@Test
	void createDebitTransaction_returnsSuccessfully() {
		// given
		String occupationId = "2000";
		var occupation = new Occupation();
		occupation.setId(occupationId);
		
		String invoiceId = "1000";
		var invoice = new Invoice();
		invoice.setId(invoiceId);
		invoice.setRentAmount(BigDecimal.valueOf(25000l));
		invoice.setSecurityAmount(BigDecimal.valueOf(100l));
		invoice.setGarbageAmount(BigDecimal.valueOf(1000l));
		invoice.setOtherAmounts(new HashMap<>() {{
			put("GYM", BigDecimal.valueOf(500l));
		}});
		var dto = new DebitTransactionDto();
		dto.setOccupationId(occupationId);
		dto.setInvoiceId(invoiceId);
		// when
		Mono<OccupationTransaction> createInvoiceIdDoesNotExist = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(invoiceRepository.save(invoice))
				.doOnSuccess(i -> System.out.println("--- Created: "+i))
				.then(occupationTransactionService.createDebitTransaction(dto));
		// then
		StepVerifier
			.create(createInvoiceIdDoesNotExist)
			.expectNextMatches(ot -> ot.getType().equals(Type.DEBIT) &&
					ot.getOccupationId().equals(occupationId) && ot.getInvoiceId().equals(invoiceId) &&
					ot.getReceiptId() == null && ot.getTotalAmountOwed().intValue() == 26600 &&
					ot.getTotalAmountPaid().intValue() == 0 && 
					ot.getTotalAmountCarriedForward().intValue() == 26600 &&
					ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
					ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM"))
			.verifyComplete();
	}
	
	@Test
	void createCreditTransaction_returnsCustomNotFoundException_whenOccupationIdDoesNotExist() {
		// given
		var dto = new CreditTransactionDto();
		String occupationId = "2000";
		dto.setOccupationId(occupationId);
		dto.setReceiptId("1000");
		// when
		Mono<OccupationTransaction> createOccupationIdDoesNotExist = reset()
				.then(occupationTransactionService.createCreditTransaction(dto));
		// then
		StepVerifier
			.create(createOccupationIdDoesNotExist)
			.expectErrorMatches(e -> e instanceof CustomNotFoundException &&
					e.getMessage().equals("Occupation with id %s does not exist!".formatted(occupationId)))
			.verify();
	}
	
	@Test
	void createCreditTransaction_returnsCustomNotFoundException_whenReceiptIdDoesNotExist() {
		// given
		String occupationId = "2000";
		var occupation = new Occupation();
		occupation.setId(occupationId);
		var dto = new CreditTransactionDto();
		dto.setOccupationId(occupationId);
		String receiptId = "1000";
		dto.setReceiptId(receiptId);
		// when
		Mono<OccupationTransaction> createReceiptIdDoesNotExist = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationTransactionService.createCreditTransaction(dto));
		// then
		StepVerifier
			.create(createReceiptIdDoesNotExist)
			.expectErrorMatches(e -> e instanceof CustomNotFoundException &&
					e.getMessage().equals("Receipt with id %s does not exist!".formatted(receiptId)))
			.verify();
	}
	
	@Test
	void createCreditTransaction_returnsCustomNotFoundException_whenPaymentIdDoesNotExist() {
		// given
		String occupationId = "2000";
		var occupation = new Occupation();
		occupation.setNumber("12345");
		occupation.setId(occupationId);

		String paymentId = "5001";

		String receiptId = "1000";
		var receipt = new Receipt();
		receipt.setId(receiptId);
		receipt.setNumber("12345");
		receipt.setPaymentId(paymentId);
		
		var dto = new CreditTransactionDto();
		dto.setOccupationId(occupationId);
		dto.setReceiptId(receiptId);
		// when
		Mono<OccupationTransaction> createReceiptIdDoesNotExist = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(receiptRepository.save(receipt))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationTransactionService.createCreditTransaction(dto));
		// then
		StepVerifier
			.create(createReceiptIdDoesNotExist)
			.expectErrorMatches(e -> e instanceof CustomNotFoundException &&
					e.getMessage().equals("Payment with id %s does not exist!".formatted(paymentId)))
			.verify();
	}
	
	@Test
	void createCreditTransaction_returnsSuccessfully() {
		// given
		String occupationId = "2000";
		var occupation = new Occupation();
		occupation.setNumber("12345");
		occupation.setId(occupationId);

		String paymentId = "5001";
		var payment = new Payment();
		payment.setId(paymentId);
		payment.setAmount(BigDecimal.valueOf(20000));
		payment.setCurrency(Currency.KES);
		payment.setReferenceNo("23456");
		payment.setType(Payment.Type.MPESA_TILL);
		
		String receiptId = "1000";
		var receipt = new Receipt();
		receipt.setId(receiptId);
		receipt.setNumber("12345");
		receipt.setPaymentId(paymentId);
		
		var dto = new CreditTransactionDto();
		dto.setOccupationId(occupationId);
		dto.setReceiptId(receiptId);
		// when
		Mono<OccupationTransaction> createSuccessfully = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(paymentRepository.save(payment))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(receiptRepository.save(receipt))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationTransactionService.createCreditTransaction(dto));
		// then
		StepVerifier
			.create(createSuccessfully)
			.expectNextMatches(o -> o.getId() != null &&
			o.getType().equals(Type.CREDIT) && o.getOccupationId().equals(occupationId) &&
			o.getInvoiceId() == null && o.getReceiptId().equals(receiptId) && o.getTotalAmountOwed().intValue() == 0 &&
			o.getTotalAmountPaid().intValue() == 20000 && o.getTotalAmountCarriedForward().intValue() == 20000 &&
			o.getCreatedOn() != null && o.getCreatedBy().equals("SYSTEM") && o.getModifiedOn() != null &&
			o.getModifiedBy().equals("SYSTEM")
			)			
			.verifyComplete();
	}

	@Test
	void findById_returnsSuccessfuly_whenOccupationTransactionExists() {
		// given
		String occupationId = "1";
		var receiptId = "25";
		Type typeCredit = Type.CREDIT;
		var occupationTransactionId = "10";
		var occupationTransaction = new OccupationTransaction.OccupationTransactionBuilder().type(typeCredit)
				.occupationId(occupationId).receiptId(receiptId).totalAmountOwed(BigDecimal.ZERO)
				.totalAmountPaid(BigDecimal.valueOf(5000l)).totalAmountCarriedForward(BigDecimal.valueOf(-5000l))
				.build();
		occupationTransaction.setId(occupationTransactionId);
		// when
		Flux<OccupationTransaction> findByIdExists = reset()
				.then(occupationTransactionRepository.save(occupationTransaction))
				.doOnSuccess(ot -> System.out.println("--- Created: " + ot))
				.thenMany(occupationTransactionService.findById(occupationTransactionId))
				.doOnNext(ot -> System.out.println("--- Found: " + ot));
		// then
		StepVerifier
			.create(findByIdExists)
			.expectNextMatches(ot -> ot.getId().equals(occupationTransactionId))
			.verifyComplete();
	}
	
	@Test
	void findById_returnsEmpty_whenIdDoesNotExist() {
		// given
		var occupationTransactionId = "2000";
		// when
		Mono<OccupationTransaction> findByIdDoesNotExist = reset()
				.then(occupationTransactionService.findById(occupationTransactionId));
		// then
		StepVerifier.create(findByIdDoesNotExist).expectComplete().verify();
	}

	@Test
	void findAll_returnsSuccessfuly_whenOccupationTransactionExists() {
		// given
		Type typeCredit = Type.CREDIT;
		String occupationId = "1";
		var occupationTransactionId = "10";
		var receiptId = "25";
		var occupationTransaction = new OccupationTransaction.OccupationTransactionBuilder().type(typeCredit)
				.occupationId(occupationId).receiptId(receiptId).totalAmountOwed(BigDecimal.ZERO)
				.totalAmountPaid(BigDecimal.valueOf(5000l)).totalAmountCarriedForward(BigDecimal.valueOf(-5000l))
				.build();
		occupationTransaction.setId(occupationTransactionId);
		// when
		Flux<OccupationTransaction> findExists = occupationTransactionRepository.save(occupationTransaction)
				.doOnSuccess(t -> System.out.println("---- Created: " + t))
				.thenMany(
						occupationTransactionService.findAll(typeCredit, occupationId, null, receiptId, OrderType.ASC))
				.doOnNext(t -> System.out.println("---- Found: " + t));
		// then
		StepVerifier.create(findExists).expectNextMatches(ot -> ot.getId().equals(occupationTransactionId))
				.verifyComplete();
	}
	

	@Test
	void findAll_returnsEmpty_whenNonExist() {
		// when
		Flux<OccupationTransaction> findNonExist = reset()
				.thenMany(occupationTransactionService.findAll(null, null, null, null, null));
		// then
		StepVerifier.create(findNonExist).expectComplete().verify();
		
	}

	@Test
	void findAll_returnsCustomBadRequestException_whenInvoiceIdUsedToFindCreditType() {
		// when
		Flux<OccupationTransaction> invoiceIdUsedToFindCreditType = occupationTransactionService.findAll(Type.CREDIT, null, "1", null, null);
		// then
		StepVerifier.create(invoiceIdUsedToFindCreditType)
					.expectErrorMatches(e -> e instanceof CustomBadRequestException &&
							e.getMessage().equals("CREDIT will not have an invoice id!"))
					.verify();
	}

	@Test
	void findAll_returnsCustomBadRequestException_whenReceiptIdUsedToFindDebitType() {
		// when
		Flux<OccupationTransaction> receiptIdUsedToFindCreditType = occupationTransactionService.findAll(Type.DEBIT, null, null, "1", null);
		// then
		StepVerifier.create(receiptIdUsedToFindCreditType)
					.expectErrorMatches(e -> e instanceof CustomBadRequestException &&
							e.getMessage().equals("DEBIT will not have an receipt id!"))
					.verify();
	}

	@Test
	void findAll_returnsCustomBadRequestException_whenBothReceiptIdAndInvoiceIdUsed() {
		// when
		Flux<OccupationTransaction> invoiceIdUsedToFindCreditType = occupationTransactionService.findAll(null, null, "1", "1", null);
		// then
		StepVerifier.create(invoiceIdUsedToFindCreditType)
					.expectErrorMatches(e -> e instanceof CustomBadRequestException &&
							e.getMessage().equals("Choose either invoiceId or receiptId. Both will not exist!"))
					.verify();
	}

}
