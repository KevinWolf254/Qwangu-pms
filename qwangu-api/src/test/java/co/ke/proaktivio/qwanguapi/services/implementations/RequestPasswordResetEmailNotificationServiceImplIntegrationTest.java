package co.ke.proaktivio.qwanguapi.services.implementations;

import java.time.LocalDateTime;
import java.util.UUID;

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
import co.ke.proaktivio.qwanguapi.configs.properties.UiPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.CompanyPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.FreeMarkerTemplatesPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.services.RequestPasswordResetEmailNotificationService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RequestPasswordResetEmailNotificationServiceImplIntegrationTest {
    @Autowired
    private CompanyPropertiesConfig cpc;
    @Autowired
    private FreeMarkerTemplatesPropertiesConfig fmpc;
    @Autowired
    private UiPropertiesConfig apc;
    @Autowired
    private RequestPasswordResetEmailNotificationService underTest;

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
    void create_returnsEmailNotification_whenSuccessful() {
        // given
        String id = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        User user = new User(id, person, emailAddress, "1", null, false, false, false, true, LocalDateTime.now(), null, null ,null);
        var token = UUID.randomUUID().toString();
        
        var resetPasswordUrl = apc.getEndPoints().get(1) + token;

        // when
        Mono<EmailNotification> create = underTest.create(user, token);
        // then
        
        StepVerifier
        	.create(create)
        	.expectNextMatches(email -> !email.getId().isEmpty()
        			&& email.getStatus().equals(NotificationStatus.PENDING)
        			&& email.getTo().get(0).equals(emailAddress)
        			&& email.getSubject().equals("Password Reset")
        			&& email.getTemplate().equals(fmpc.getTemplates().get(2).getName())
        			&& email.getTemplateModel().get("companyUrl").equals(cpc.getUrl())
        			&& email.getTemplateModel().get("resetPasswordUrl").equals(resetPasswordUrl)
        			
        			&& email.getTemplateModel().get("linkedInUrl").equals(fmpc.getTemplates().get(0).getModels().get(0))
        			&& email.getTemplateModel().get("twitterUrl").equals(fmpc.getTemplates().get(0).getModels().get(1))
        			&& email.getTemplateModel().get("facebookUrl").equals(fmpc.getTemplates().get(0).getModels().get(2))
        			&& email.getTemplateModel().get("instagramUrl").equals(fmpc.getTemplates().get(0).getModels().get(3))
        			&& email.getTemplateModel().get("designedBy").equals(fmpc.getTemplates().get(0).getModels().get(4))
        			
        			&& email.getResources().get("companyLogoImage").equals(fmpc.getTemplates().get(0).getResources().get(0))
        			&& email.getResources().get("passwordImage").equals(fmpc.getTemplates().get(2).getResources().get(0))
        			&& email.getResources().get("footerImage").equals(fmpc.getTemplates().get(0).getResources().get(2))
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
