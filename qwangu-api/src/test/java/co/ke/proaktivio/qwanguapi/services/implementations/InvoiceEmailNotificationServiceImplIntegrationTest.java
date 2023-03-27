package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

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
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Invoice.Type;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.ChargeDto;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.models.Occupation.Status;
import co.ke.proaktivio.qwanguapi.repositories.TenantRepository;
import co.ke.proaktivio.qwanguapi.services.InvoiceEmailNotificationService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class InvoiceEmailNotificationServiceImplIntegrationTest {
    @Autowired
    private CompanyPropertiesConfig cpc;
    @Autowired
    private FreeMarkerTemplatesPropertiesConfig fmpc;
    @Autowired
    private UiPropertiesConfig uiEndPoints;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private InvoiceEmailNotificationService underTest;

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
		var occupation = new Occupation.OccupationBuilder()
				.tenantId(tenantId)
				.build();
		var invoice = new Invoice();
		var previousOT = new OccupationTransaction();
		// when
		Mono<EmailNotification> create = underTest.create(occupation, invoice, previousOT);
		// then
		StepVerifier
			.create(create)
			.expectErrorMatches(e -> e instanceof CustomBadRequestException
					&& e.getMessage().equals("Tenant with id %s does not exist!".formatted(tenantId)))
			.verify();
	}

	@Test
	@SuppressWarnings("serial")
	void create_returnsEmailNotification_whenSuccessful() {
		// given
		String tenantId = "1";
		String unitId = "1";
		String occupationId = "1";
		var invoiceId = "1";
		var occupation = new Occupation.OccupationBuilder()
				.status(Status.CURRENT)
				.startDate(LocalDate.now().withDayOfMonth(1))
				.tenantId(tenantId)
				.unitId(unitId)
				.build();
		occupation.setId(occupationId);
		String emailAddress = "john.doe@somemail.com";
		var tenant = new Tenant.TenantBuilder()
				.firstName("John")
				.middleName("Doe")
				.surname("Doe")
				.emailAddress(emailAddress)
				.mobileNumber("0720000000")
				.build();
		tenant.setId(tenantId);
		var invoice = new Invoice.InvoiceBuilder()
				.currency(Currency.KES)
				.startDate(LocalDate.now().withDayOfMonth(1))
				.endDate(LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()))
				.rentAmount(BigDecimal.valueOf(20000l))
				.securityAmount(BigDecimal.valueOf(500l))
				.garbageAmount(BigDecimal.valueOf(550l))
				.otherAmounts(new HashMap<>() {{
					put("GYM", BigDecimal.valueOf(250l));
				}})
				.number(null, occupation)
				.type(Type.RENT)
				.occupationId(occupationId)
				.build();
		var previousOT = new OccupationTransaction.OccupationTransactionBuilder()
				.totalAmountCarriedForward(BigDecimal.valueOf(5000l))
				.occupationId(occupationId)
				.invoiceId(invoiceId)
				.totalAmountOwed(BigDecimal.valueOf(5000l))
				.build();
		// when
		Mono<EmailNotification> createSuccessfully = tenantRepository.deleteAll()
				.doOnSuccess($ -> System.out.println("--- Deleted all tenants!")).then(tenantRepository.save(tenant))
				.doOnSuccess(t -> System.out.println("--- Created: " + t))
				.then(underTest.create(occupation, invoice, previousOT));
		// then
		StepVerifier
			.create(createSuccessfully)
			.expectNextMatches(email -> email.getId() != null
        			&& email.getTo().get(0).equals(emailAddress)
        			&& email.getStatus().equals(NotificationStatus.PENDING)
        			&& email.getSubject().equals("Rent Invoice")
        			&& email.getTemplate().equals(fmpc.getTemplates().get(3).getName())
        			&& email.getTemplateModel().get("signInUrl").equals(uiEndPoints.getEndPoints().get(2))
        			&& email.getTemplateModel().get("companyUrl").equals(cpc.getUrl())
        			&& email.getTemplateModel().get("companyName").equals(cpc.getName())
        			&& email.getTemplateModel().get("linkedInUrl").equals(cpc.getSocialMedia().getLinkedInUrl())
        			&& email.getTemplateModel().get("twitterUrl").equals(cpc.getSocialMedia().getTwitterUrl())
        			&& email.getTemplateModel().get("facebookUrl").equals(cpc.getSocialMedia().getFacebookUrl())
        			&& email.getTemplateModel().get("instagramUrl").equals(cpc.getSocialMedia().getInstagramUrl())
        			
        			&& email.getTemplateModel().get("endDate") != null
        			&& email.getTemplateModel().get("dueDate") != null
        			&& email.getTemplateModel().get("currentMonth") != null
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(0)).getName().equals("BROUGHT FORWARD")
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(0)).getAmount() == 5000l
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(1)).getName().equals("RENT")
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(1)).getAmount() == 20000l
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(2)).getName().equals("SECURITY")
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(2)).getAmount() == 500l
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(3)).getName().equals("GARBAGE")
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(3)).getAmount() == 550l
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(4)).getName().equals("GYM")
        			&& ((ChargeDto)((ArrayList<?>)email.getTemplateModel().get("amountsEntry")).get(4)).getAmount() == 250l

        			&& email.getResources().get("linkedInImage").equals(fmpc.getTemplates().get(0).getResources().get(3))
        			&& email.getResources().get("twitterImage").equals(fmpc.getTemplates().get(0).getResources().get(4))
        			&& email.getResources().get("facebookImage").equals(fmpc.getTemplates().get(0).getResources().get(5))
        			&& email.getResources().get("instagramImage").equals(fmpc.getTemplates().get(0).getResources().get(6))
        			&& email.getResources().get("invoiceImage").equals(fmpc.getTemplates().get(0).getResources().get(7))

        			&& email.getCreatedOn() != null
        			&& email.getModifiedOn() != null
			)
			.verifyComplete();		
	}

}
