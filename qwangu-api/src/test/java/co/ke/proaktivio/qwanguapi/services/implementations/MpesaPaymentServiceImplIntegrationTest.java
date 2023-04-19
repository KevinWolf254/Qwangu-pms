package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import co.ke.proaktivio.qwanguapi.models.MpesaPayment;
import co.ke.proaktivio.qwanguapi.models.MpesaPayment.MpesaPaymentType;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentResponse;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.MpesaPaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.services.MpesaPaymentService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class MpesaPaymentServiceImplIntegrationTest {
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;
    
    @Autowired
    private MpesaPaymentService underTest;
    @Autowired
	private MpesaPaymentRepository mpesaPaymentRepository;
    @Autowired
    private PaymentRepository paymentRepository;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }
    
	@Test
	void validate_returnsMpesaPaymentResponse_whenSuccessful() {
		// given
		var dto = new MpesaPaymentDto();
		// then
		StepVerifier
			.create(underTest.validate(dto))
			.expectNextMatches(r -> (Integer) r.getCode() == 0 && r.getDescription().equals("ACCEPTED"))
			.verifyComplete();
	}

	@Test
	void create_returnsMpesaPaymentResponse_whenTransactionTimeIsNotValid() {
		// given
		MpesaPaymentDto dto = new MpesaPaymentDto("RKTQDM7W6S", "Pay Bill", "201122063845", "10000.0", "600638",
	            "T903", "", "49197.00", "", "254708374149", "John", "", "Doe");
	    // when
		Mono<MpesaPaymentResponse> createMPesaPayment = mpesaPaymentRepository.deleteAll()
				.doOnSuccess(t -> System.out.println("---- Deleted all MpesaPayments!"))
				.then(paymentRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Payments!"))
				.then(underTest.create(dto));
		// then
		StepVerifier
			.create(createMPesaPayment)
			.expectNextMatches(r -> (Integer) r.getCode() == 0 && r.getDescription().equals("ACCEPTED"))
			.verifyComplete();
	}

	@Test
	void create_returnsMpesaPaymentResponse_andCreatesMpesaPayment_whenTransactionTimeIsNotValid() {
		// given
		var mpesaTill = MpesaPaymentType.MPESA_PAY_BILL;
		var transactionId = "RKTQDM7W6S";
		var transactionType = "Pay Bill";
		var amount = 10000;
		var shortCode = "600638";
		var referenceNumber = "T903";
		var accountBalance = "49197.00";
		var mobileNumber = "254708374149";
		var firstName = "John";
		var lastName = "Doe";
	    // when
		create_returnsMpesaPaymentResponse_whenTransactionTimeIsNotValid();
		Flux<MpesaPayment> findAll = mpesaPaymentRepository.findAll();
		
		// then
		StepVerifier
			.create(findAll)
			.expectNextMatches(p -> p.getId() != null 
					&& p.getType().equals(mpesaTill)
					&& p.getIsProcessed()
					&& p.getTransactionId().equals(transactionId)
					&& p.getTransactionType().equals(transactionType)
					&& p.getTransactionTime() != null
					&& p.getCurrency().equals(Currency.KES)
					&& p.getAmount().intValue() == amount
					&& p.getShortCode().equals(shortCode)
					&& p.getMobileNumber().equals(mobileNumber)
					&& p.getFirstName().equals(firstName)
					&& p.getLastName().equals(lastName)
					&& p.getReferenceNumber().equals(referenceNumber)
					&& p.getBalance().equals(accountBalance)
					&& p.getCreatedOn() != null && p.getModifiedOn() != null)
			.verifyComplete();
	}

	@Test
	void create_returnsMpesaPaymentResponse_andCreatesMpesaPayment_andCreatesPayment_whenTransactionTimeIsNotValid() {
		// given
		// when
		create_returnsMpesaPaymentResponse_andCreatesMpesaPayment_whenTransactionTimeIsNotValid();
		Flux<Payment> findAll = paymentRepository.findAll();
		// then
		StepVerifier
			.create(findAll)
			.expectNextMatches(p -> p.getId() != null
			&& p.getStatus().equals(PaymentStatus.UNCLAIMED)
			&& p.getType().equals(PaymentType.MOBILE)
			&& p.getOccupationNumber().equals("T903")
			&& p.getReferenceNumber().equals("RKTQDM7W6S")
			&& p.getCurrency().equals(Currency.KES)
			&& p.getAmount().intValue() == 10000
			&& p.getCreatedOn() != null && p.getModifiedOn() != null)
			.verifyComplete();
	}
	
	@Test
	void findAll_returnEmptyList_whenMpesaPaymentsDoNotExist() {
		// given
		// when
		Flux<MpesaPayment> findEmptyList = mpesaPaymentRepository.deleteAll()
				.doOnSuccess(t -> System.out.println("---- Deleted all MpesaPayments!"))
				.thenMany(underTest.findAll(null, null, null, null));
		// then
		StepVerifier
			.create(findEmptyList)
			.verifyComplete();		
	}

	@Test
	void findAll_returnsPayment_whenSuccessful() {
		// given
    	MpesaPaymentType mpesaTill = MpesaPaymentType.MPESA_TILL;
    	String transactionId = "QWERGOEJEOD9E";
    	String transactionType = "WMSOSJ";
    	Currency kes = Currency.KES;
    	BigDecimal amount = BigDecimal.valueOf(20000l);
    	String shortCode = "12345";
    	String referenceNumber = "KMDPNWPQ2444O4";
    	String balance = "200000";
    	String mobileNumber = "0720000000";
    	String firstName = "John";
    	String middleName = "Doe";
    	
    	var mpesaPayment = new MpesaPayment();
		mpesaPayment.setType(mpesaTill);
    	mpesaPayment.setIsProcessed(true);
		mpesaPayment.setTransactionId(transactionId);
		mpesaPayment.setTransactionType(transactionType);
    	mpesaPayment.setTransactionTime(LocalDateTime.now());
		mpesaPayment.setCurrency(kes);
		mpesaPayment.setAmount(amount);
		mpesaPayment.setShortCode(shortCode);
		mpesaPayment.setReferenceNumber(referenceNumber);
    	mpesaPayment.setInvoiceNo(shortCode);
		mpesaPayment.setBalance(balance);
		mpesaPayment.setMobileNumber(mobileNumber);
		mpesaPayment.setFirstName(firstName);
		mpesaPayment.setMiddleName(middleName);
		
		// when
		Flux<MpesaPayment> findAll = mpesaPaymentRepository.deleteAll()
				.doOnSuccess(t -> System.out.println("---- Deleted all MpesaPayments!"))
				.then(mpesaPaymentRepository.save(mpesaPayment))
				.doOnSuccess(t -> System.out.println("Created: " + t))
				.thenMany(underTest.findAll(transactionId, referenceNumber, shortCode, OrderType.ASC))
				.doOnNext(t -> System.out.println("Found: " + t));
		// then
		StepVerifier
			.create(findAll)
			.expectNextMatches(p -> p.getId() != null 
			&& p.getType().equals(mpesaTill)
			&& p.getIsProcessed()
			&& p.getTransactionId().equals(transactionId)
			&& p.getTransactionType().equals(transactionType)
			&& p.getTransactionTime() != null
			&& p.getCurrency().equals(kes)
			&& p.getAmount().equals(amount)
			&& p.getShortCode().equals(shortCode)
			&& p.getInvoiceNo().equals(shortCode)
			&& p.getBalance().equals(balance)
			&& p.getMobileNumber().equals(mobileNumber)
			&& p.getFirstName().equals(firstName)
			&& p.getMiddleName().equals(middleName)
			&& p.getReferenceNumber().equals(referenceNumber)
			&& p.getCreatedOn() != null && p.getModifiedOn() != null)
			.verifyComplete();
	}

}
