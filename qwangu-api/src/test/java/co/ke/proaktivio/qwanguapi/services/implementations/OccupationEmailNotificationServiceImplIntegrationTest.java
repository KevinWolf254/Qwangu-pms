package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDate;
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
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.services.OccupationEmailNotificationService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OccupationEmailNotificationServiceImplIntegrationTest {
    @Autowired
    private CompanyPropertiesConfig cpc;
    @Autowired
    private FreeMarkerTemplatesPropertiesConfig fmpc;
    @Autowired
    private UiPropertiesConfig uiEndPoints;
    @Autowired
    private OccupationEmailNotificationService underTest;
    
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
	
    @SuppressWarnings("serial")
	@Test
    void create_returnsEmailNotification_whenSuccessful() {
    	// given
		var tenantId = "12345";
		var unitId = "123456";
		var occupation = new Occupation.OccupationBuilder()
				.startDate(LocalDate.now()).status(Occupation.Status.PENDING_OCCUPATION)
				.tenantId(tenantId).unitId(unitId).build();
		var unit = new Unit.UnitBuilder().status(Unit.Status.VACANT).number("12345")
				.type(Unit.UnitType.APARTMENT_UNIT).identifier(Unit.Identifier.A)
				.floorNo(2).currency(Currency.KES).rentPerMonth(BigDecimal.valueOf(25000l))
				.securityPerMonth(BigDecimal.valueOf(500l)).garbagePerMonth(BigDecimal.valueOf(550l))
				.otherAmounts(new HashMap<String, BigDecimal>(){{
					put("GYM", BigDecimal.valueOf(1000l));
				}}).advanceInMonths(1).securityAdvance(BigDecimal.valueOf(2000)).garbageAdvance(BigDecimal.valueOf(1500))
				.otherAmountsAdvance(new HashMap<String, BigDecimal>() {{
					put("GYM", BigDecimal.valueOf(1000l));
				}}).build();
		unit.setId(unitId);
		String emailAddress = "john.doe@someplace.com";
		var tenant = new Tenant.TenantBuilder().firstName("John").middleName("Doe")
				.surname("Doe").mobileNumber("0720000000")
				.emailAddress(emailAddress).build();
		tenant.setId(tenantId);

        // when
        Mono<EmailNotification> create = underTest.create(unit, tenant, occupation);
        // then
        
        StepVerifier
        	.create(create)
        	.expectNextMatches(email -> !email.getId().isEmpty()
        			&& email.getTo().get(0).equals(emailAddress)
        			&& email.getStatus().equals(NotificationStatus.PENDING)
        			&& email.getSubject().equals("Welcome")
        			&& email.getTemplate().equals(fmpc.getTemplates().get(5).getName())
        			&& email.getTemplateModel().get("companyUrl").equals(cpc.getUrl())
        			&& email.getTemplateModel().get("companyName").equals(cpc.getName())
        			&& email.getTemplateModel().get("signInUrl").equals(uiEndPoints.getEndPoints().get(2))
        			&& email.getTemplateModel().get("linkedInUrl").equals(fmpc.getTemplates().get(0).getModels().get(0))
        			&& email.getTemplateModel().get("twitterUrl").equals(fmpc.getTemplates().get(0).getModels().get(1))
        			&& email.getTemplateModel().get("facebookUrl").equals(fmpc.getTemplates().get(0).getModels().get(2))
        			&& email.getTemplateModel().get("instagramUrl").equals(fmpc.getTemplates().get(0).getModels().get(3))
        			&& email.getTemplateModel().get("startDate").equals(occupation.getStartDate())
        			&& email.getTemplateModel().get("tenantName").equals(tenant.getFirstName() + " " + tenant.getMiddleName() + " " + tenant.getSurname())

        			&& email.getResources().get("welcomeImage").equals(fmpc.getTemplates().get(0).getResources().get(9))
        			&& email.getResources().get("linkedInImage").equals(fmpc.getTemplates().get(0).getResources().get(3))
        			&& email.getResources().get("twitterImage").equals(fmpc.getTemplates().get(0).getResources().get(4))
        			&& email.getResources().get("facebookImage").equals(fmpc.getTemplates().get(0).getResources().get(5))
        			&& email.getResources().get("instagramImage").equals(fmpc.getTemplates().get(0).getResources().get(6))

        			&& email.getCreatedOn() != null
        			&& email.getModifiedOn() != null
        			)
        	.verifyComplete();
    }

}
