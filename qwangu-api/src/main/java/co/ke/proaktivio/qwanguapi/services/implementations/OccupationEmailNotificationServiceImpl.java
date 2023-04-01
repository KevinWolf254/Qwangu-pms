package co.ke.proaktivio.qwanguapi.services.implementations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import co.ke.proaktivio.qwanguapi.configs.properties.CompanyPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.FreeMarkerTemplatesPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.UiPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.repositories.EmailNotificationRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationEmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class OccupationEmailNotificationServiceImpl implements OccupationEmailNotificationService {
	private final CompanyPropertiesConfig cpc;
    private final UiPropertiesConfig uiEndPoints;
    private final FreeMarkerTemplatesPropertiesConfig fmpc;
	private final EmailNotificationRepository emailNotificationRepository;

	@Override
	public Mono<EmailNotification> create(Unit unit, Tenant tenant, Occupation occupation) {
        var signInUrl = uiEndPoints.getEndPoints().get(2);

        Map<String, Object> models = new HashMap<>();
        models.put("tenantName", tenant.getFirstName() + " " + tenant.getMiddleName() + " " + tenant.getSurname());
        models.put("startDate", occupation.getStartDate());
        // ui info
        models.put("signInUrl", signInUrl);
        // company info
        models.put("companyUrl", cpc.getUrl());
        models.put("companyName", cpc.getName());
        models.put("linkedInUrl", fmpc.getTemplates().get(0).getModels().get(0));
        models.put("twitterUrl", fmpc.getTemplates().get(0).getModels().get(1));
        models.put("facebookUrl", fmpc.getTemplates().get(0).getModels().get(2));
        models.put("instagramUrl", fmpc.getTemplates().get(0).getModels().get(3));
        
        
        Map<String, String> resourceMap = new HashMap<>();
        List<String> resources = fmpc.getTemplates().get(0).getResources();
        resourceMap.put("linkedInImage", resources.get(3));
        resourceMap.put("twitterImage", resources.get(4));
        resourceMap.put("facebookImage", resources.get(5));
        resourceMap.put("instagramImage", resources.get(6));
        resourceMap.put("welcomeImage", resources.get(9));
        
		EmailNotification email = new EmailNotification();
		email.setTemplate(fmpc.getTemplates().get(5).getName());
		email.setSubject("Welcome");
		email.setTo(List.of(tenant.getEmailAddress()));
        email.setTemplateModel(models);
        email.setResources(resourceMap);
        email.setStatus(NotificationStatus.PENDING);
        
        return save(email);
	}
	
	private Mono<EmailNotification> save(EmailNotification email) {
        return emailNotificationRepository.save(email)
        		.doOnSuccess(e -> log.info("Created: " +e));
	}

}
