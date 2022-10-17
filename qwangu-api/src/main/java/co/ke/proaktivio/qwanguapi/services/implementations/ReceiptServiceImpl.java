package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Occupation;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {
    private final OccupationTransactionService occupationTransactionService;
    private final PaymentService paymentService;
    private final ReceiptRepository receiptRepository;
    private final ReactiveMongoTemplate template;

    private Mono<Occupation> findOccupationById(String id) {
        return template.findById(id, Occupation.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(id))));
    }

    @Override
    public Mono<Receipt> create(ReceiptDto dto) {
        String occupationId = dto.getOccupationId();
        return findOccupationById(occupationId)
                .flatMap($ -> paymentService.findById(dto.getPaymentId()))
                .flatMap($ -> receiptRepository.save(new Receipt.ReceiptBuilder()
                        // TODO - GENERATE RANDOM RECEIPT
                        // RCPT22101705BE
                        .receiptNo(UUID.randomUUID().toString())
                        .occupationId(occupationId)
                        .paymentId(dto.getPaymentId())
                        .build())
                )
                .doOnSuccess(t -> log.info(" Created: {}", t))
                .flatMap(receipt -> occupationTransactionService
                        .createCreditTransaction(new CreditTransactionDto(occupationId, receipt.getId()))
                        .doOnSuccess(t -> log.info(" Created: {}", t))
                        .then(Mono.just(receipt)));
    }

    @Override
    public Mono<Receipt> findById(String id) {
        return receiptRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Receipt with id %s does not exist!".formatted(id))));
    }

    @Override
    public Flux<Receipt> findPaginated(Optional<String> occupationId, Optional<String> paymentId, int page, int pageSize,
                                       OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        occupationId.ifPresent(oId -> query.addCriteria(Criteria.where("occupationId").is(oId)));
        paymentId.ifPresent(pId -> query.addCriteria(Criteria.where("paymentId").is(pId)));

        query.with(pageable)
                .with(sort);
        return template
                .find(query, Receipt.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Receipts were not found!")));
    }
}
