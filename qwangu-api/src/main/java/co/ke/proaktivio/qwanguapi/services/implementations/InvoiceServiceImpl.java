package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.DebitTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.BiFunction;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OccupationRepository occupationRepository;
    private final OccupationTransactionService occupationTransactionService;
    private final ReactiveMongoTemplate template;

    @Override
    @Transactional
    public Mono<Invoice> create(InvoiceDto dto) {
        String occupationId = dto.getOccupationId();
        // TODO INCLUDE BEGINNING_OF_MONTH OR MIDDLE_OF_MONTH IN INVOICE
        return occupationRepository.findById(occupationId)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!"
                        .formatted(occupationId))))
                .flatMap(occupation -> template
                        .findOne(new Query()
                                .addCriteria(Criteria.where("occupationId").is(occupation.getId()))
                                .with(Sort.by(Sort.Direction.DESC, "id")), Invoice.class)
                        .switchIfEmpty(Mono.just(new Invoice()))
                        .map(previousInvoice -> new Invoice.InvoiceBuilder()
                                .number(previousInvoice, occupation)
                                .type(dto.getType())
                                .startDate(occupation.getStartDate())
                                .endDate(!dto.getType().equals(Invoice.Type.RENT_ADVANCE) && dto.getEndDate() == null ?
                                        occupation.getStartDate().withDayOfMonth(occupation.getStartDate()
                                                .getMonth().length(occupation.getStartDate().isLeapYear())) :
                                        dto.getEndDate())
                                .rentAmount(dto.getRentAmount())
                                .securityAmount(dto.getSecurityAmount())
                                .garbageAmount(dto.getGarbageAmount())
                                .otherAmounts(dto.getOtherAmounts() != null ?
                                        dto.getOtherAmounts() :
                                        null)
                                .occupationId(occupationId)
                                .build()))
                .flatMap(invoiceRepository::save)
                .flatMap(invoice -> occupationTransactionService
                        .createDebitTransaction(new DebitTransactionDto(occupationId, invoice.getId()))
                        .then(Mono.just(invoice))
                );
    }

    @Override
    public Mono<Invoice> findById(String id) {
        return invoiceRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Invoice with id %s does not exist!"
                        .formatted(id))));
    }

    @Override
    public Mono<Invoice> update(String id, InvoiceDto dto) {
        return findById(id)
                .map(receivable -> {
                    receivable.setType(dto.getType());
                    receivable.setStartDate(dto.getStartDate());
                    receivable.setRentAmount(dto.getRentAmount());
                    receivable.setSecurityAmount(dto.getSecurityAmount());
                    receivable.setGarbageAmount(dto.getGarbageAmount());
                    receivable.setOtherAmounts(dto.getOtherAmounts());
                    return receivable;
                })
                .flatMap(invoiceRepository::save);
    }

    @Override
    public Flux<Invoice> findPaginated(Optional<Invoice.Type> type, Optional<String> month, int page, int pageSize,
                                       OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        type.ifPresent(s -> query.addCriteria(Criteria.where("type").is(s)));
        month.ifPresent(i -> query.addCriteria(Criteria.where("period").is(i)));
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
