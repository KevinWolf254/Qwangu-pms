package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
	private final ReactiveMongoTemplate template;

	@Override
	public Mono<Payment> findById(String paymentId) {
		return template.findById(paymentId, Payment.class);
	}

	@Override
	public Flux<Payment> findAll(PaymentStatus status, PaymentType type, String referenceNumber, String mpesaPaymentId,
			OrderType order) {

		Query query = new Query();
		if (status != null)
			query.addCriteria(Criteria.where("status").is(status));

		if (type != null)
			query.addCriteria(Criteria.where("type").is(type));

		if (StringUtils.hasText(referenceNumber))
			query.addCriteria(Criteria.where("referenceNumber").regex(".*" + referenceNumber.trim() + ".*", "i"));

		if (StringUtils.hasText(mpesaPaymentId))
			query.addCriteria(Criteria.where("mpesaPaymentId").is(mpesaPaymentId.trim()));

		Sort sort = order != null
				? order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) : Sort.by(Sort.Order.desc("id"))
				: Sort.by(Sort.Order.desc("id"));
		query.with(sort);
		return template.find(query, Payment.class);
	}
}
