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
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction.OccupationTransactionType;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.OccupationTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.ReceiptRepository;
import co.ke.proaktivio.qwanguapi.repositories.TenantRepository;
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
	@Autowired
	private TenantRepository tenantRepository;
	
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
				.doOnSuccess(t -> System.out.println("---- Deleted all Payments!"))
				.then(tenantRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"));
	}
	
	@Test
	void createDebitTransaction_returnsCustomNotFoundException_whenOccupationIdDoesNotExist() {
		// given
		String occupationId = "2000";
		var invoiceId = "1000";
		var dto = new OccupationTransactionDto.OccupationTransactionDtoBuilder()
				.type(OccupationTransactionType.DEBIT)
				.occupationId(occupationId)
				.invoiceId(invoiceId)
				.build();
		// when
		Mono<OccupationTransaction> createOccupationIdDoesNotExist = reset()
				.then(occupationTransactionService.create(dto));
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
		String invoiceId = "1000";
		var occupation = new Occupation();
		occupation.setId(occupationId);
		
		var dto = new OccupationTransactionDto.OccupationTransactionDtoBuilder()
				.type(OccupationTransactionType.DEBIT)
				.occupationId(occupationId)
				.invoiceId(invoiceId)
				.build();
		// when
		Mono<OccupationTransaction> createInvoiceIdDoesNotExist = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationTransactionService.create(dto));
		// then
		StepVerifier
			.create(createInvoiceIdDoesNotExist)
			.expectErrorMatches(e -> e instanceof CustomNotFoundException &&
					e.getMessage().equals("Invoice with id %s does not exist!".formatted(invoiceId)))
			.verify();
	}
	
	@SuppressWarnings("serial")
	@Test
	void createDebitTransaction_returnsSuccessfully() {
		// given
		// given
		var tenantId = "1";
        var tenant = new Tenant.TenantBuilder()
                .firstName("John")
                .middleName("Doe")
                .surname("Jane")
                .mobileNumber("0720000001")
                .emailAddress("jJane@mail.co.ke")
                .build();
        tenant.setId(tenantId);
        
		String occupationId = "2000";
		var occupation = new Occupation();
		occupation.setId(occupationId);
		occupation.setTenantId(tenantId);
		
		String invoiceId = "1000";
		var invoice = new Invoice();
		invoice.setId(invoiceId);
		invoice.setCurrency(Currency.KES);
		invoice.setRentAmount(BigDecimal.valueOf(25000l));
		invoice.setSecurityAmount(BigDecimal.valueOf(100l));
		invoice.setGarbageAmount(BigDecimal.valueOf(1000l));
		invoice.setOtherAmounts(new HashMap<>() {{
			put("GYM", BigDecimal.valueOf(500l));
		}});

		var dto = new OccupationTransactionDto.OccupationTransactionDtoBuilder()
				.type(OccupationTransactionType.DEBIT)
				.occupationId(occupationId)
				.invoiceId(invoiceId)
				.build();
		// when
		Mono<OccupationTransaction> createInvoiceIdDoesNotExist = reset()
				.then(tenantRepository.save(tenant))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(invoiceRepository.save(invoice))
				.doOnSuccess(i -> System.out.println("--- Created: "+i))
				.then(occupationTransactionService.create(dto));
		// then
		StepVerifier
			.create(createInvoiceIdDoesNotExist)
			.expectNextMatches(ot -> ot.getType().equals(OccupationTransactionType.DEBIT) &&
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
		String occupationId = "2000";
		String receiptId = "1000";

		var dto = new OccupationTransactionDto.OccupationTransactionDtoBuilder()
				.type(OccupationTransactionType.CREDIT)
				.occupationId(occupationId)
				.receiptId(receiptId)
				.build();
		// when
		Mono<OccupationTransaction> createOccupationIdDoesNotExist = reset()
				.then(occupationTransactionService.create(dto));
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
		String receiptId = "1000";
		var occupation = new Occupation();
		occupation.setId(occupationId);

		var dto = new OccupationTransactionDto.OccupationTransactionDtoBuilder()
				.type(OccupationTransactionType.CREDIT)
				.occupationId(occupationId)
				.receiptId(receiptId)
				.build();
		// when
		Mono<OccupationTransaction> createReceiptIdDoesNotExist = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationTransactionService.create(dto));
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

		var dto = new OccupationTransactionDto.OccupationTransactionDtoBuilder()
				.type(OccupationTransactionType.CREDIT)
				.occupationId(occupationId)
				.receiptId(receiptId)
				.build();
		// when
		Mono<OccupationTransaction> createReceiptIdDoesNotExist = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(receiptRepository.save(receipt))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationTransactionService.create(dto));
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
		var tenantId = "1";
        var tenant = new Tenant.TenantBuilder()
                .firstName("John")
                .middleName("Doe")
                .surname("Jane")
                .mobileNumber("0720000001")
                .emailAddress("jJane@mail.co.ke")
                .build();
        tenant.setId(tenantId);
        
		String occupationId = "2000";
		var occupation = new Occupation();
		occupation.setNumber("12345");
		occupation.setId(occupationId);
		occupation.setTenantId(tenantId);

		String paymentId = "5001";
		var payment = new Payment();
		payment.setId(paymentId);
		payment.setAmount(BigDecimal.valueOf(20000));
		payment.setCurrency(Currency.KES);
		payment.setOccupationNumber(occupation.getNumber());
		payment.setReferenceNumber("23456");
		payment.setType(Payment.PaymentType.MOBILE);
		
		String receiptId = "1000";
		var receipt = new Receipt();
		receipt.setId(receiptId);
		receipt.setNumber("12345");
		receipt.setPaymentId(paymentId);
        
		var dto = new OccupationTransactionDto.OccupationTransactionDtoBuilder()
				.type(OccupationTransactionType.CREDIT)
				.occupationId(occupationId)
				.receiptId(receiptId)
				.build();
		// when
		Mono<OccupationTransaction> createSuccessfully = reset()
				.then(tenantRepository.save(tenant))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(paymentRepository.save(payment))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(receiptRepository.save(receipt))
				.doOnSuccess(o -> System.out.println("--- Created: "+o))
				.then(occupationTransactionService.create(dto));
		// then
		StepVerifier
			.create(createSuccessfully)
			.expectNextMatches(o -> o.getId() != null &&
			o.getType().equals(OccupationTransactionType.CREDIT) && o.getOccupationId().equals(occupationId) &&
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
		OccupationTransactionType typeCredit = OccupationTransactionType.CREDIT;
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
		OccupationTransactionType typeCredit = OccupationTransactionType.CREDIT;
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
		Flux<OccupationTransaction> invoiceIdUsedToFindCreditType = occupationTransactionService.findAll(OccupationTransactionType.CREDIT, null, "1", null, null);
		// then
		StepVerifier.create(invoiceIdUsedToFindCreditType)
					.expectErrorMatches(e -> e instanceof CustomBadRequestException &&
							e.getMessage().equals("CREDIT will not have an invoice id!"))
					.verify();
	}

	@Test
	void findAll_returnsCustomBadRequestException_whenReceiptIdUsedToFindDebitType() {
		// when
		Flux<OccupationTransaction> receiptIdUsedToFindCreditType = occupationTransactionService.findAll(OccupationTransactionType.DEBIT, null, null, "1", null);
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
