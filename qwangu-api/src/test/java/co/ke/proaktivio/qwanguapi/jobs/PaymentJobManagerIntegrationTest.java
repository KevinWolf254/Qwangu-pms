package co.ke.proaktivio.qwanguapi.jobs;

import java.math.BigDecimal;
import java.time.LocalDate;

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
import co.ke.proaktivio.qwanguapi.models.MpesaPayment;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Occupation.Status;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction.Type;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.models.SmsNotification;
import co.ke.proaktivio.qwanguapi.models.SmsNotification.NotificationStatus;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.repositories.MpesaPaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.ReceiptRepository;
import co.ke.proaktivio.qwanguapi.repositories.SmsNotificationRepository;
import co.ke.proaktivio.qwanguapi.services.SmsNotificationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class PaymentJobManagerIntegrationTest {
	@Autowired
	private SmsNotificationService smsNotificationService;
	@Autowired
	private PaymentJobManager underTest;

	@Autowired
	private PaymentRepository paymentRepository;
	@Autowired
	private OccupationRepository occupationRepository;
	@Autowired
	private MpesaPaymentRepository mpesaPaymentRepository;
	@Autowired
	private ReceiptRepository receiptRepository;
	@Autowired
	private OccupationTransactionRepository occupationTransactionRepository;
	@Autowired
	private SmsNotificationRepository smsNotificationRepository;
	
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

	@NotNull
	private Mono<Void> reset() {
		return paymentRepository.deleteAll()
				.doOnSuccess(t -> System.out.println("---- Deleted all Payments!"))
				.then(occupationRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
				.then(mpesaPaymentRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Mpesa Payments!"))
				.then(receiptRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Mpesa Receipts!"))
				.then(occupationTransactionRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all OccupationTransactions!"))
				.then(smsNotificationRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all SmsNotifications!"));
	}
    
	@Test
	void processMobilePayments_returnsEmpty_whenNoUnClaimedPaymentsExist() {
		// given
		// when
		Flux<SmsNotification> create = underTest.processMobilePayments()
				.thenMany(smsNotificationService.findAll(null, null));
		// then
		StepVerifier
			.create(create)
			.verifyComplete();
	}
    
	@Test
	void processMobilePayments_returnsEmpty_whenClaimedPaymentsExist() {
		// given
		var payment = new Payment.PaymentBuilder().currency(Currency.KES).amount(BigDecimal.valueOf(20000l)).occupationNumber("12345")
				.referenceNumber("234567").setType(PaymentType.MOBILE).build();
		
		// when
		Flux<SmsNotification> create = reset()
				.then(paymentRepository.save(payment))
				.doOnSuccess(t -> System.out.println("---- Created: " +t))
				.thenMany(underTest.processMobilePayments())
				.thenMany(smsNotificationService.findAll(null, null));
		// then
		StepVerifier
			.create(create)
			.verifyComplete();
	}
    
	@Test
	void processMobilePayments_updatesPaymentToClaimed_whenUnClaimedPaymentsExist() {
		// given
		var occupation = new Occupation.OccupationBuilder().unitId("1").tenantId("1").status(Status.CURRENT)
				.startDate(LocalDate.now()).build();
		occupation.setId("1");
		occupation.setNumber("1");
		
		var payment = new Payment.PaymentBuilder().currency(Currency.KES).amount(BigDecimal.valueOf(20000l))
				.occupationNumber("1").referenceNumber("234567").setType(PaymentType.MOBILE).build();
		payment.setStatus(PaymentStatus.UNCLAIMED);
		payment.setId("1");
		
		var mpesaPayment = new MpesaPayment();
		mpesaPayment.setAmount(BigDecimal.valueOf(20000l));
		mpesaPayment.setTransactionId("234567");
		mpesaPayment.setReferenceNumber("HSE10001");
		mpesaPayment.setMobileNumber("0720000000");
		mpesaPayment.setCurrency(Currency.KES);
		
		// when
		Flux<Payment> create = reset()
				.then(occupationRepository.save(occupation))
				.doOnSuccess(t -> System.out.println("---- Created: " +t))
				.then(paymentRepository.save(payment))
				.doOnSuccess(t -> System.out.println("---- Created: " +t))
				.then(mpesaPaymentRepository.save(mpesaPayment))
				.doOnSuccess(t -> System.out.println("---- Created: " +t))
				.thenMany(underTest.processMobilePayments())
				.thenMany(paymentRepository.findAll())
				.doOnNext(t -> System.out.println("---- Found: " +t));
		
		// then
		StepVerifier
			.create(create)
			.expectNextMatches(p -> p.getStatus().equals(PaymentStatus.CLAIMED))
			.verifyComplete();
	}

	@Test
	void processMobilePayments_createsReceipt_whenUnClaimedPaymentsExist() {
		// when
		processMobilePayments_updatesPaymentToClaimed_whenUnClaimedPaymentsExist();
		Flux<Receipt> receipts = receiptRepository.findAll()
				.doOnNext(t -> System.out.println("---- Found: " +t));
		
		// then
		StepVerifier
			.create(receipts)
			.expectNextMatches(r -> r.getOccupationId().equals("1") && r.getPaymentId().equals("1"))
			.verifyComplete();		
	}

	@Test
	void processMobilePayments_createsOccupationTransaction_whenUnClaimedPaymentsExist() {
		// when
		processMobilePayments_updatesPaymentToClaimed_whenUnClaimedPaymentsExist();
		Flux<OccupationTransaction> receipts = occupationTransactionRepository.findAll()
				.doOnNext(t -> System.out.println("---- Found: " +t));
		
		// then
		StepVerifier
			.create(receipts)
			.expectNextMatches(ot -> ot.getOccupationId().equals("1") && ot.getType().equals(Type.CREDIT)
					&& ot.getTotalAmountPaid().intValue() == 20000)
			.verifyComplete();		
	}

	@Test
	void processMobilePayments_createsSmsNotification_whenUnClaimedPaymentsExist() {
		// when
		processMobilePayments_updatesPaymentToClaimed_whenUnClaimedPaymentsExist();
		Flux<SmsNotification> smsNotifications = smsNotificationRepository.findAll()
				.doOnNext(t -> System.out.println("---- Found: " +t));
		
		// then
		StepVerifier
			.create(smsNotifications)
			.expectNextMatches(sn -> !sn.getId().isEmpty() && sn.getPhoneNumber().equals("0720000000")
					&& !sn.getMessage().isEmpty() && sn.getStatus().equals(NotificationStatus.PENDING))
			.verifyComplete();		
	}
}
