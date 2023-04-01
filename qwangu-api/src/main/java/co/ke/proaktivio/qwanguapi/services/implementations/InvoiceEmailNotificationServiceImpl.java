package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import co.ke.proaktivio.qwanguapi.configs.properties.CompanyPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.FreeMarkerTemplatesPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.RentPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.UiPropertiesConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.pojos.ChargeDto;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.repositories.EmailNotificationRepository;
import co.ke.proaktivio.qwanguapi.services.InvoiceEmailNotificationService;
import co.ke.proaktivio.qwanguapi.services.TenantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class InvoiceEmailNotificationServiceImpl implements InvoiceEmailNotificationService {
	private final CompanyPropertiesConfig cpc;
    private final RentPropertiesConfig config;
    private final TenantService tenantService;
    private final UiPropertiesConfig uiEndPoints;
    private final FreeMarkerTemplatesPropertiesConfig fmpc;
	private final EmailNotificationRepository emailNotificationRepository;
	
	@Override
	public Mono<EmailNotification> create(Occupation occupation, Invoice invoice, OccupationTransaction previousOccupationTransaction) {
    	String tenantId = occupation.getTenantId();
		return tenantService.findById(tenantId)
            .switchIfEmpty(Mono.error(new CustomBadRequestException("Tenant with id %s does not exist!".formatted(tenantId))))
    		.map(tenant -> {
    			var firstName = tenant.getFirstName();
    			var surname = tenant.getSurname();
    			var startDate = invoice.getStartDate() != null ? invoice.getStartDate() : LocalDate.now();
    			var endDate = invoice.getEndDate() != null ? invoice.getEndDate() : startDate.withDayOfMonth(startDate.lengthOfMonth());
    	    	var currency = invoice.getCurrency().name();

    			var currentMonth = startDate.getMonth();
    			
    			var dueDay = config.getDueDatePerMonth();
    			var dueDate = LocalDate.of(startDate.getYear(), currentMonth, dueDay);
    	    	
    	    	BigDecimal rentAmount = invoice.getRentAmount() != null ? invoice.getRentAmount() : BigDecimal.ZERO;
    			BigDecimal securityAmount = invoice.getSecurityAmount() != null ? invoice.getSecurityAmount() : BigDecimal.ZERO;
    			BigDecimal garbageAmount = invoice.getGarbageAmount() != null ? invoice.getGarbageAmount() : BigDecimal.ZERO;
    			
    			BigDecimal rentSecurityGarbage = BigDecimal.ZERO.add(rentAmount).add(securityAmount).add(garbageAmount);
    			BigDecimal otherAmounts = invoice.getOtherAmounts() != null ?
    			        invoice.getOtherAmounts().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add) :
    			        BigDecimal.ZERO;
    			BigDecimal totalAmountOwed = BigDecimal.ZERO.add(rentSecurityGarbage).add(otherAmounts);
    			BigDecimal amountBroughtForward = previousOccupationTransaction.getTotalAmountCarriedForward();
    			
    			BigDecimal totalCarriedForward = BigDecimal.ZERO.add(totalAmountOwed).add(amountBroughtForward);

    			List<ChargeDto> amountsEntry = new ArrayList<>();
    			amountsEntry.add(0, new ChargeDto.ChargeBuilder().name("BROUGHT FORWARD").amount(amountBroughtForward.doubleValue()).build());
    			amountsEntry.add(1, new ChargeDto.ChargeBuilder().name("RENT").amount(rentAmount.doubleValue()).build());
    			amountsEntry.add(2, new ChargeDto.ChargeBuilder().name("SECURITY").amount(securityAmount.doubleValue()).build());
    			amountsEntry.add(3, new ChargeDto.ChargeBuilder().name("GARBAGE").amount(garbageAmount.doubleValue()).build());
    			if(invoice.getOtherAmounts() != null)
	    			invoice.getOtherAmounts().forEach((name, otherAmount) -> {
	        			amountsEntry.add(new ChargeDto.ChargeBuilder().name(name.toUpperCase()).amount(otherAmount.doubleValue()).build());
	    			});
    			
    	        var signInUrl = uiEndPoints.getEndPoints().get(2);
    	        
    	        Map<String, Object> models = new HashMap<>();
    	        models.put("startDate", startDate);
    	        models.put("endDate", endDate);
    	        models.put("tenantName", firstName + " " +surname);
    	        models.put("dueDate", dueDate);
    	        models.put("currentMonth", currentMonth);
    	        models.put("currency", currency);
    	        models.put("amountsEntry", amountsEntry);
    	        models.put("totalAmount", totalCarriedForward.doubleValue());
    	        // ui info
    	        models.put("signInUrl", signInUrl);
    	        // company info
    	        models.put("companyUrl", cpc.getUrl());
    	        models.put("companyName", cpc.getName());
    	        models.put("linkedInUrl", cpc.getSocialMedia().getLinkedInUrl());
    	        models.put("twitterUrl", cpc.getSocialMedia().getTwitterUrl());
    	        models.put("facebookUrl", cpc.getSocialMedia().getFacebookUrl());
    	        models.put("instagramUrl", cpc.getSocialMedia().getInstagramUrl());
    	        
    	        Map<String, String> resourceMap = new HashMap<>();
    	        List<String> resources = fmpc.getTemplates().get(0).getResources();
    	        resourceMap.put("linkedInImage", resources.get(3));
    	        resourceMap.put("twitterImage", resources.get(4));
    	        resourceMap.put("facebookImage", resources.get(5));
    	        resourceMap.put("instagramImage", resources.get(6));
    	        resourceMap.put("invoiceImage", resources.get(7));
    	        
    			EmailNotification email = new EmailNotification();
    			email.setTemplate(fmpc.getTemplates().get(3).getName());
    			email.setSubject("Rent Invoice");
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
