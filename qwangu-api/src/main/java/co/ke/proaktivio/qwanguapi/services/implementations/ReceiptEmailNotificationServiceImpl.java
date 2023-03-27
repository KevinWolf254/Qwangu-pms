package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import co.ke.proaktivio.qwanguapi.configs.properties.CompanyPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.FreeMarkerTemplatesPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.UiPropertiesConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.ChargeDto;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.repositories.EmailNotificationRepository;
import co.ke.proaktivio.qwanguapi.services.ReceiptEmailNotificationService;
import co.ke.proaktivio.qwanguapi.services.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReceiptEmailNotificationServiceImpl implements ReceiptEmailNotificationService {
	private final CompanyPropertiesConfig cpc;
	private final UiPropertiesConfig uiEndPoints;
	private final FreeMarkerTemplatesPropertiesConfig fmpc;
	private final EmailNotificationRepository emailNotificationRepository;
	private final TenantService tenantService;
	
	@Override
	public 	Mono<EmailNotification> create(Occupation occupation, Payment payment, OccupationTransaction previousOccupationTransaction) {    	
        String tenantId = occupation.getTenantId();
		return tenantService.findById(tenantId)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Tenant with id %s does not exist!".formatted(tenantId))))
        		.map(tenant -> {
        			var firstName = tenant.getFirstName();
        			var surname = tenant.getSurname();
        			var paidAmount = payment.getAmount().doubleValue();
        	        var signInUrl = uiEndPoints.getEndPoints().get(2);
        	    	var currency = payment.getCurrency().name();

        			BigDecimal amountBroughtForward = previousOccupationTransaction.getTotalAmountCarriedForward();
        			BigDecimal amount = payment.getAmount() != null ? payment.getAmount() : BigDecimal.ZERO;
        			BigDecimal totalAmountPaid = BigDecimal.ZERO.add(amount);
        			BigDecimal totalCarriedForward = totalAmountPaid.subtract(amountBroughtForward);

        			List<ChargeDto> amountsEntry = new ArrayList<>();
        			amountsEntry.add(0, new ChargeDto.ChargeBuilder().name("OWED").amount(amountBroughtForward.doubleValue()).build());
        			amountsEntry.add(0, new ChargeDto.ChargeBuilder().name("PAID").amount(totalAmountPaid.doubleValue()).build());
        			
        	        Map<String, Object> models = new HashMap<>();
        	        models.put("tenantName", firstName + " " +surname);
        	        models.put("currency", currency);
        	        models.put("paidAmount", paidAmount);
        	        models.put("amountsEntry", amountsEntry);
        	        models.put("totalAmount", totalCarriedForward.doubleValue());
        	        // company info
        	        models.put("companyUrl", cpc.getUrl());
        	        models.put("companyName", cpc.getName());
        	        models.put("linkedInUrl", cpc.getSocialMedia().getLinkedInUrl());
        	        models.put("twitterUrl", cpc.getSocialMedia().getTwitterUrl());
        	        models.put("facebookUrl", cpc.getSocialMedia().getFacebookUrl());
        	        models.put("instagramUrl", cpc.getSocialMedia().getInstagramUrl());
        	        // ui info
        	        models.put("signInUrl", signInUrl);
        	        //resources
        	        Map<String, String> resourceMap = new HashMap<>();
        	        List<String> resources = fmpc.getTemplates().get(0).getResources();
        	        resourceMap.put("linkedInImage", resources.get(3));
        	        resourceMap.put("twitterImage", resources.get(4));
        	        resourceMap.put("facebookImage", resources.get(5));
        	        resourceMap.put("instagramImage", resources.get(6));
        	        resourceMap.put("paymentImage", resources.get(8));

        			EmailNotification email = new EmailNotification();
        			email.setTemplate(fmpc.getTemplates().get(4).getName());
        			email.setSubject("Rent Receipt");
        			email.setTo(List.of(tenant.getEmailAddress()));
        	        email.setTemplateModel(models);
        	        email.setResources(resourceMap);
        	        email.setStatus(NotificationStatus.PENDING);
        	        return email;
        		})
        		.flatMap(this::save);
        
    }
	
	private Mono<EmailNotification> save(EmailNotification email) {
        return emailNotificationRepository.save(email)
        		.doOnSuccess(e -> log.info("Created: " +e));
	}

}
