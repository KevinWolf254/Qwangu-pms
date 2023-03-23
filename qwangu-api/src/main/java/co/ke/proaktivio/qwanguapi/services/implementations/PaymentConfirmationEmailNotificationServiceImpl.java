package co.ke.proaktivio.qwanguapi.services.implementations;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import co.ke.proaktivio.qwanguapi.configs.properties.UiPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.CompanyPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.FreeMarkerTemplatesPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.repositories.EmailNotificationRepository;
import co.ke.proaktivio.qwanguapi.services.PaymentConfirmationEmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentConfirmationEmailNotificationServiceImpl implements PaymentConfirmationEmailNotificationService {
    private final FreeMarkerTemplatesPropertiesConfig fmpc;
    private final CompanyPropertiesConfig cpc;
    private final UiPropertiesConfig apc;
    private final EmailNotificationRepository emailNotificationRepository;
    
	@Override
	public Mono<EmailNotification> create(Tenant tenant) {
        EmailNotification email = new EmailNotification();
        email.setTo(List.of(tenant.getEmailAddress()));
        email.setSubject("Payment Confirmation");
        
        // TODO - GET TEMPLATE TO SEND PAYMENT CONFIRMATION
        email.setTemplate(fmpc.getTemplates().get(3).getName());

        var loginUrl = apc.getEndPoints().get(2);
        Map<String, Object> models = new HashMap<>();
        models.put("companyUrl", cpc.getUrl());
        models.put("loginUrl", loginUrl);
        
        models.put("linkedInUrl", fmpc.getTemplates().get(0).getModels().get(0));
        models.put("twitterUrl", fmpc.getTemplates().get(0).getModels().get(1));
        models.put("facebookUrl", fmpc.getTemplates().get(0).getModels().get(2));
        models.put("instagramUrl", fmpc.getTemplates().get(0).getModels().get(3));
        models.put("designedBy", fmpc.getTemplates().get(0).getModels().get(4));
        email.setTemplateModel(models);

        Map<String, String> resourceMap = new HashMap<>();
        List<String> resources = fmpc.getTemplates().get(0).getResources();
        resourceMap.put("companyLogoImage", resources.get(0));
        resourceMap.put("passwordImage", resources.get(1));
        resourceMap.put("footerImage", resources.get(2));
        resourceMap.put("linkedInImage", resources.get(3));
        resourceMap.put("twitterImage", resources.get(4));
        resourceMap.put("facebookImage", resources.get(5));
        resourceMap.put("instagramImage", resources.get(6));
        email.setResources(resourceMap);
        
        email.setStatus(NotificationStatus.PENDING);
        return emailNotificationRepository.save(email)
        		.doOnSuccess(e -> log.info("Created: " +e));
	}

}
