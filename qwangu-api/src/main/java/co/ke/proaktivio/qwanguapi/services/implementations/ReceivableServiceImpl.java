package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Receivable;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.ReceivableDto;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.ReceivableRepository;
import co.ke.proaktivio.qwanguapi.services.ReceivableService;
import com.mongodb.client.result.DeleteResult;
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
public class ReceivableServiceImpl implements ReceivableService {
    private final ReceivableRepository receivableRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Receivable> create(ReceivableDto dto) {
        return receivableRepository.save(new Receivable(null, dto.getType(), dto.getPeriod(), dto.getRentAmount(),
                dto.getSecurityAmount(), dto.getGarbageAmount(), dto.getOtherAmounts(), LocalDateTime.now(), null));
    }

    @Override
    public Mono<Receivable> update(String id, ReceivableDto dto) {
        return receivableRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Receivable with id %s does not exist!"
                        .formatted(id))))
                .map(receivable -> {
                    receivable.setType(dto.getType());
                    receivable.setPeriod(dto.getPeriod());
                    receivable.setRentAmount(dto.getRentAmount());
                    receivable.setSecurityAmount(dto.getSecurityAmount());
                    receivable.setGarbageAmount(dto.getGarbageAmount());
                    receivable.setOtherAmounts(dto.getOtherAmounts());
                    return receivable;
                })
                .flatMap(receivableRepository::save);
    }

    @Override
    public Flux<Receivable> findPaginated(Optional<String> id, Optional<Receivable.Type> type,
                                          Optional<LocalDate> period, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        id.ifPresent(i -> query.addCriteria(Criteria.where("id").is(i)));
        type.ifPresent(s -> query.addCriteria(Criteria.where("type").is(s)));
        period.ifPresent(i -> query.addCriteria(Criteria.where("period").is(i)));
        query.with(pageable)
                .with(sort);
        return template
                .find(query, Receivable.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Receivable were not found!")));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Receivable.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Receivable with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
