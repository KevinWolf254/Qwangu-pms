package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.pojos.CreditTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.repositories.ReceiptRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import co.ke.proaktivio.qwanguapi.services.ReceiptService;
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
public class ReceiptServiceImpl implements ReceiptService {
    private final OccupationTransactionService occupationTransactionService;
    private final PaymentService paymentService;
    private final ReceiptRepository receiptRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Receipt> create(ReceiptDto dto) {
        String occupationId = dto.getOccupationId();
		String paymentId = dto.getPaymentId();
        return findOccupationById(occupationId)
                .flatMap(occupation -> {
					return findPaymentById(paymentId)
					        .flatMap($ -> template
					                .findOne(new Query()
					                        .addCriteria(Criteria.where("occupationId").is(occupation.getId()))
					                        .with(Sort.by(Sort.Direction.DESC, "id")), Receipt.class)
					                .switchIfEmpty(Mono.just(new Receipt()))
					                .flatMap(previousReceipt -> receiptRepository.save(new Receipt.ReceiptBuilder()
					                        .number(previousReceipt, occupation)
					                        .occupationId(occupationId)
					                        .paymentId(paymentId)
					                        .build()))
					        );
				})
                .doOnSuccess(t -> log.info("Created: {}", t))
                .flatMap(receipt -> occupationTransactionService
                        .createCreditTransaction(new CreditTransactionDto(occupationId, receipt.getId()))
                        .doOnSuccess(t -> log.info("Created: {}", t))
                        .then(Mono.just(receipt)));
    }

    @Override
    public Mono<Receipt> findById(String id) {
        return receiptRepository.findById(id);
    }

    @Override
    public Flux<Receipt> findAll(String occupationId, String paymentId, OrderType order) {
        Query query = new Query();
        
        if(StringUtils.hasText(occupationId))
        	query.addCriteria(Criteria.where("occupationId").is(occupationId.trim()));
        if(StringUtils.hasText(paymentId))
        	query.addCriteria(Criteria.where("paymentId").is(paymentId.trim()));
        
		Sort sort = order != null
				? order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) : Sort.by(Sort.Order.desc("id"))
				: Sort.by(Sort.Order.desc("id"));

        query.with(sort);
        return template.find(query, Receipt.class);
    }

	private Mono<Payment> findPaymentById(String paymentId) {
		return paymentService.findById(paymentId)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Payment with id %s does not exist!"
                        .formatted(paymentId))));
	}
	
    private Mono<Occupation> findOccupationById(String occupationId) {
        return template.findById(occupationId, Occupation.class)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Occupation with id %s does not exist!"
                        .formatted(occupationId))));
    }
}
