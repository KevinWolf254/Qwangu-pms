package co.ke.proaktivio.qwanguapi.services.implementations;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.EmailNotificationRepository;
import co.ke.proaktivio.qwanguapi.services.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class EmailNotificationServiceImpl implements EmailNotificationService {
	private final EmailNotificationRepository emailNotificationRepository;
	private final ReactiveMongoTemplate template;
	
	@Override
	public Mono<EmailNotification> update(String emailNotificationId, EmailNotification emailNotification) {
		return emailNotificationRepository.findById(emailNotificationId)
				.switchIfEmpty(Mono.error(new CustomNotFoundException("EmailNotification with id %s does not exist!".formatted(emailNotificationId))))
				.map(email -> {
					email.setStatus(emailNotification.getStatus());
					return email;
				})
				.flatMap(emailNotificationRepository::save)
				.doOnSuccess(email -> log.info("Updated: " +email));
	}

	@Override
	public Flux<EmailNotification> findAll(NotificationStatus status, String phoneNumber, OrderType order) {
		Query query = new Query();
		if (status != null)
			query.addCriteria(Criteria.where("status").is(status));

		if (StringUtils.hasText(phoneNumber))
			query.addCriteria(Criteria.where("phoneNumber").regex(".*" + phoneNumber.trim() + ".*", "i"));

		Sort sort = order != null
				? order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) : Sort.by(Sort.Order.desc("id"))
				: Sort.by(Sort.Order.desc("id"));
		query.with(sort);
		return template.find(query, EmailNotification.class);
	}

}
