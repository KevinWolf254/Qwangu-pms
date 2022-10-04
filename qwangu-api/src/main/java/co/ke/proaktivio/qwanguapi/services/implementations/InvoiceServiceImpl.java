package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
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
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OccupationRepository occupationRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Invoice> create(InvoiceDto dto) {
        return occupationRepository.findById(dto.getOccupationId())
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s could not be found!"
                        .formatted(dto.getOccupationId()))))
                .map(occupation -> new Invoice.InvoiceBuilder()
                .type(dto.getType())
                .period(dto.getPeriod())
                .rentAmount(dto.getRentAmount())
                .securityAmount(dto.getSecurityAmount())
                .garbageAmount(dto.getGarbageAmount())
                .otherAmounts(dto.getOtherAmounts())
                        // TODO GENERATE UNIQUE INVOICE NO
                .invoiceNo(UUID.randomUUID().toString())
                .occupationId(occupation.getId())
                .build())
                .flatMap(invoiceRepository::save);
    }

    @Override
    public Mono<Invoice> update(String id, InvoiceDto dto) {
        return invoiceRepository.findById(id)
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
                .flatMap(invoiceRepository::save);
    }

    @Override
    public Flux<Invoice> findPaginated(Optional<String> id, Optional<Invoice.Type> type,
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
                .find(query, Invoice.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Receivable were not found!")));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Invoice.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Receivable with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
