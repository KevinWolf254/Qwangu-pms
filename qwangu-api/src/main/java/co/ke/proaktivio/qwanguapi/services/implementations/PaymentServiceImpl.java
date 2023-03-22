package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
	private final PaymentRepository paymentRepository;
	private final ReactiveMongoTemplate template;

	@Override
	public Mono<Payment> create(Payment payment) {
		return existsByReferenceNumber(payment.getReferenceNumber())
				.filter(isTrue -> !isTrue)
				.switchIfEmpty(Mono.error(new CustomBadRequestException("Payment already exists with reference no. %s!".formatted(payment.getReferenceNumber()))))
				.flatMap($ -> paymentRepository.save(payment))
				.doOnSuccess(s -> log.info("Created " + s));
	}

	@Override
	public Mono<Payment> update(Payment payment) {
		return findById(payment.getId())
			.switchIfEmpty(Mono.error(new CustomNotFoundException("Payment with id $s does not exist!".formatted(payment.getId()))))
			.doOnSuccess(p -> p.setStatus(payment.getStatus()))
			.flatMap(paymentRepository::save)
			.doOnSuccess(s -> log.info("Updated " + s));
	}

    private Mono<Boolean> existsByReferenceNumber(String referenceNumber) {
        return template.exists(new Query()
                .addCriteria(Criteria.where("referenceNumber").is(referenceNumber)), Payment.class);
    }
    
	@Override
	public Mono<Payment> findById(String paymentId) {
		return template.findById(paymentId, Payment.class);
	}

	@Override
	public Flux<Payment> findAll(PaymentStatus status, PaymentType type, String referenceNumber, OrderType order) {

		Query query = new Query();
		if (status != null)
			query.addCriteria(Criteria.where("status").is(status));

		if (type != null)
			query.addCriteria(Criteria.where("type").is(type));

		if (StringUtils.hasText(referenceNumber))
			query.addCriteria(Criteria.where("referenceNumber").regex(".*" + referenceNumber.trim() + ".*", "i"));

		Sort sort = order != null
				? order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) : Sort.by(Sort.Order.desc("id"))
				: Sort.by(Sort.Order.desc("id"));
		query.with(sort);
		return template.find(query, Payment.class);
	}
}
