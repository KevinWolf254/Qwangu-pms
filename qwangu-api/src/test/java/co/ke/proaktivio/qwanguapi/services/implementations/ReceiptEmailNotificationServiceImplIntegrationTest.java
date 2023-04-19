package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;

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
import co.ke.proaktivio.qwanguapi.configs.properties.CompanyPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.FreeMarkerTemplatesPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.UiPropertiesConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Occupation.Status;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.ChargeDto;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.repositories.TenantRepository;
import co.ke.proaktivio.qwanguapi.services.ReceiptEmailNotificationService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ReceiptEmailNotificationServiceImplIntegrationTest {
	@Autowired
	private CompanyPropertiesConfig cpc;
	@Autowired
	private FreeMarkerTemplatesPropertiesConfig fmpc;
	@Autowired
	private UiPropertiesConfig uiEndPoints;
	@Autowired
	private TenantRepository tenantRepository;
	@Autowired
	private ReceiptEmailNotificationService underTest;

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

	@Test
	void create_returnsCustomBadRequestException_whenTenantDOesNotExist() {
		// given
		String tenantId = "12345";
		var occupation = new Occupation.OccupationBuilder().tenantId(tenantId).build();
		var payment = new Payment();
		var previousOT = new OccupationTransaction();
		// when
		Mono<EmailNotification> create = underTest.create(occupation, payment, previousOT);
		// then
		StepVerifier.create(create).expectErrorMatches(e -> e instanceof CustomBadRequestException
				&& e.getMessage().equals("Tenant with id %s does not exist!".formatted(tenantId))).verify();
	}

	@Test
	void create_returnsEmailNotification_whenSuccessful() {
		// given
		String tenantId = "1";
		String unitId = "1";
		String occupationId = "1";
		var invoiceId = "1";
		var occupation = new Occupation.OccupationBuilder().status(Status.CURRENT)
				.startDate(LocalDate.now().withDayOfMonth(1)).tenantId(tenantId).unitId(unitId).build();
		occupation.setId(occupationId);
		String emailAddress = "john.doe@somemail.com";
		String firstName = "John";
		String surname = "Doe";
		var tenant = new Tenant.TenantBuilder().firstName(firstName).middleName(surname).surname(surname)
				.emailAddress(emailAddress).mobileNumber("0720000000").build();
		tenant.setId(tenantId);
		var payment = new Payment.PaymentBuilder().amount(BigDecimal.valueOf(20000)).currency(Currency.KES)
				.type(PaymentType.MOBILE).referenceNumber("123456").occupationNumber("23456").build();
		var previousOT = new OccupationTransaction.OccupationTransactionBuilder()
				.totalAmountCarriedForward(BigDecimal.valueOf(5000l)).occupationId(occupationId).invoiceId(invoiceId)
				.totalAmountOwed(BigDecimal.valueOf(5000l)).build();
		// when
		Mono<EmailNotification> createSuccessfully = tenantRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("--- Deleted all tenants!")).then(tenantRepository.save(tenant))
				.doOnSuccess(t -> System.out.println("--- Created: " + t))
				.then(underTest.create(occupation, payment, previousOT));
		// then
		StepVerifier.create(createSuccessfully)
				.expectNextMatches(email -> email.getId() != null
						&& email.getTemplateModel().get("tenantName").equals(firstName + " " + surname)
						&& email.getTemplateModel().get("currency").equals(Currency.KES.name())
	        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(0)).getName().equals("PAID")
	        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(0)).getAmount() == 20000l
	        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(1)).getName().equals("OWED")
	        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(1)).getAmount() == 5000l
	        			&& (Double) email.getTemplateModel().get("totalAmount") == 15000l
						&& email.getTemplateModel().get("companyUrl").equals(cpc.getUrl())
						&& email.getTemplateModel().get("companyName").equals(cpc.getName())
						&& email.getTemplateModel().get("linkedInUrl").equals(cpc.getSocialMedia().getLinkedInUrl())
						&& email.getTemplateModel().get("twitterUrl").equals(cpc.getSocialMedia().getTwitterUrl())
						&& email.getTemplateModel().get("facebookUrl").equals(cpc.getSocialMedia().getFacebookUrl())
						&& email.getTemplateModel().get("instagramUrl").equals(cpc.getSocialMedia().getInstagramUrl())
						&& email.getTemplateModel().get("signInUrl").equals(uiEndPoints.getEndPoints().get(2))
						&& email.getResources().get("linkedInImage").equals(fmpc.getTemplates().get(0).getResources().get(3))
						&& email.getResources().get("twitterImage").equals(fmpc.getTemplates().get(0).getResources().get(4))
						&& email.getResources().get("facebookImage").equals(fmpc.getTemplates().get(0).getResources().get(5))
						&& email.getResources().get("instagramImage").equals(fmpc.getTemplates().get(0).getResources().get(6))
						&& email.getResources().get("paymentImage").equals(fmpc.getTemplates().get(0).getResources().get(8))
						&& email.getTo().get(0).equals(emailAddress)
						&& email.getTemplate().equals(fmpc.getTemplates().get(4).getName())
						&& email.getSubject().equals("Rent Receipt")
						&& email.getStatus().equals(NotificationStatus.PENDING) && email.getCreatedOn() != null
						&& email.getModifiedOn() != null)
				.verifyComplete();
	}
}
