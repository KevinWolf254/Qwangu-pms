package co.ke.proaktivio.qwanguapi.services.implementations;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.ke.proaktivio.qwanguapi.models.SmsNotification;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.SmsNotificationDto;
import co.ke.proaktivio.qwanguapi.repositories.SmsNotificationRepository;
import co.ke.proaktivio.qwanguapi.services.SmsNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class SmsNotificationServiceImpl implements SmsNotificationService {
    private final ReactiveMongoTemplate template;
    private final SmsNotificationRepository smsNotificationRepository;
    
	@Override
	public Mono<SmsNotification> create(SmsNotificationDto dto) {
		return Mono.just(new SmsNotification.SmsNotificationBuilder().setPhoneNumber(dto.getPhoneNumber()).setMessage(dto.getMessage()).build())
				.flatMap(sms -> smsNotificationRepository.save(sms))
				.doOnSuccess(sms -> log.info("Created: {}", sms));
	}
	
	@Override
	public Flux<SmsNotification> findAll(String mobileNumber, OrderType order) {
        Query query = new Query();
        if(StringUtils.hasText(mobileNumber))
            query.addCriteria(Criteria.where("mobileNumber").regex(".*" +mobileNumber.trim()+ ".*", "i"));

        Sort sort = order != null ?order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id")):
                    Sort.by(Sort.Order.desc("id"));
        query.with(sort);
        return template.find(query, SmsNotification.class);
	}

}
