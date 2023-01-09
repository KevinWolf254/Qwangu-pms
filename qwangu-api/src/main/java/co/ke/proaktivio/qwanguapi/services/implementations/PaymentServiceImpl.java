package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Payment> findById(String paymentId) {
        return template.findById(paymentId, Payment.class);
    }

    @Override
    public Flux<Payment> findAll(Optional<Payment.Status> status, Optional<Payment.Type> type,
                                 Optional<String> shortCode, Optional<String> transactionId,
                                 Optional<String> referenceNo,
                                 Optional<String> mobileNumber, OrderType order) {
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        status.ifPresent(s -> query.addCriteria(Criteria.where("status").is(s)));
        shortCode.ifPresent(i -> {
            if(StringUtils.hasText(i))
                query.addCriteria(Criteria.where("shortCode").is(i));
        });
        transactionId.ifPresent(i -> {
            if(StringUtils.hasText(i))
                query.addCriteria(Criteria.where("transactionId").regex(".*" +i.trim()+ ".*", "i"));
        });
        referenceNo.ifPresent(i -> {
            if(StringUtils.hasText(i))
                query.addCriteria(Criteria.where("referenceNo").regex(".*" +i.trim()+ ".*", "i"));
        });
        mobileNumber.ifPresent(i -> {
            if(StringUtils.hasText(i))
                query.addCriteria(Criteria.where("mobileNumber").regex(".*" +i.trim()+ ".*", "i"));
        });
        query.with(sort);
        return template
                .find(query, Payment.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Payments were not found!")));
    }
}
