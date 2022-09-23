package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Receivable;
import co.ke.proaktivio.qwanguapi.pojos.OccupationTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OccupationTransactionServiceImpl implements OccupationTransactionService {
    private final OccupationTransactionRepository occupationTransactionRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<OccupationTransaction>  create(OccupationTransactionDto dto) {
        return occupationTransactionRepository.save(new OccupationTransaction(null, dto.getType(),
                dto.getTotalAmountOwed(), dto.getTotalAmountPaid(), dto.getTotalAmountCarriedForward(), dto.getOccupationId(),
                dto.getReceivableId(), dto.getPaymentId(), LocalDateTime.now()));
    }

    @Override
    public Mono<OccupationTransaction> findLatestByOccupationId(String occupationId) {
        return template
                .findOne(new Query()
                        .addCriteria(Criteria.where("occupationId").is(occupationId))
                        .with(Sort.by(Sort.Direction.DESC, "id")), OccupationTransaction.class);
    }

    @Override
    public Mono<OccupationTransaction> findById(String occupationTransactionId) {
        return template.findById(occupationTransactionId, OccupationTransaction.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("OccupationTransaction with id %s could not be found!".formatted(occupationTransactionId))));
    }

    @Override
    public Flux<OccupationTransaction> findPaginated(Optional<OccupationTransaction.Type> type,
                                          Optional<String> occupationId, Optional<String> receivableId,
                                                     Optional<String> paymentId, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        type.ifPresent(s -> query.addCriteria(Criteria.where("type").is(s)));
        occupationId.ifPresent(i -> query.addCriteria(Criteria.where("occupationId").is(i)));
        receivableId.ifPresent(i -> query.addCriteria(Criteria.where("receivableId").is(i)));
        paymentId.ifPresent(i -> query.addCriteria(Criteria.where("paymentId").is(i)));
        query.with(pageable)
                .with(sort);
        return template
                .find(query, OccupationTransaction.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("OccupationTransactions were not found!")));
    }
}
