package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.DebitTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final OccupationTransactionService occupationTransactionService;
    private final ReactiveMongoTemplate template;

    @Override
    @Transactional
    public Mono<Invoice> create(InvoiceDto dto) {
        if (dto.getType().equals(Invoice.Type.RENT))
            return createRentInvoice(dto);
        else if (dto.getType().equals(Invoice.Type.RENT_ADVANCE))
            return createRentAdvanceInvoice(dto);
        else if (dto.getType().equals(Invoice.Type.PENALTY))
            return createPenaltyInvoice(dto);
        else
            return Mono.error(new CustomBadRequestException("Invoice type does not exist!"));
    }

    private Mono<Invoice> createRentAdvanceInvoice(InvoiceDto dto) {
        String occupationId = dto.getOccupationId();
        return findOccupationById(occupationId)
                .map(occupation ->
                        new Invoice.InvoiceBuilder()
                                .type(Invoice.Type.RENT_ADVANCE)
                                // TODO GENERATE UNIQUE INVOICE NO
                                .invoiceNo(UUID.randomUUID().toString())
                                .rentAmount(dto.getRentAmount())
                                .securityAmount(dto.getSecurityAmount())
                                .garbageAmount(dto.getGarbageAmount())
                                .otherAmounts(dto.getOtherAmounts() != null ? dto.getOtherAmounts() : null)
                                .occupationId(occupation.getId())
                                .build()
                )
                .flatMap(invoiceRepository::save)
                .flatMap(invoice -> occupationTransactionService
                        .createDebitTransaction(new DebitTransactionDto(occupationId, invoice.getId()))
                        .then(Mono.just(invoice))
                );
    }

    private Mono<Invoice> createRentInvoice(InvoiceDto dto) {
        String occupationId = dto.getOccupationId();
        return findOccupationById(occupationId)
                .map(occupation ->
                        new Invoice.InvoiceBuilder()
                                .type(Invoice.Type.RENT)
                                // TODO GENERATE UNIQUE INVOICE NO
                                .invoiceNo(UUID.randomUUID().toString())
                                .period(dto.getFromDate())
                                .rentAmount(dto.getRentAmount())
                                .securityAmount(dto.getSecurityAmount())
                                .garbageAmount(dto.getGarbageAmount())
                                .otherAmounts(dto.getOtherAmounts())
                                .occupationId(occupation.getId())
                                .build()
                )
                .flatMap(invoiceRepository::save)
                .flatMap(invoice -> occupationTransactionService
                        .createDebitTransaction(new DebitTransactionDto(occupationId, invoice.getId()))
                        .then(Mono.just(invoice))
                );
    }

    private Mono<Invoice> createPenaltyInvoice(InvoiceDto dto) {
        String occupationId = dto.getOccupationId();
        return findOccupationById(occupationId)
                .map(occupation ->
                        new Invoice.InvoiceBuilder()
                                .type(Invoice.Type.PENALTY)
                                // TODO GENERATE UNIQUE INVOICE NO
                                .invoiceNo(UUID.randomUUID().toString())
                                .otherAmounts(dto.getOtherAmounts())
                                .occupationId(occupation.getId())
                                .build()
                )
                .flatMap(invoiceRepository::save)
                .flatMap(invoice -> occupationTransactionService
                        .createDebitTransaction(new DebitTransactionDto(occupationId, invoice.getId()))
                        .then(Mono.just(invoice))
                );
    }

    private Mono<Occupation> findOccupationById(String id) {
        return template.findById(id, Occupation.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(id))));
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
                    receivable.setPeriod(dto.getFromDate());
                    receivable.setRentAmount(dto.getRentAmount());
                    receivable.setSecurityAmount(dto.getSecurityAmount());
                    receivable.setGarbageAmount(dto.getGarbageAmount());
                    receivable.setOtherAmounts(dto.getOtherAmounts());
                    return receivable;
                })
                .flatMap(invoiceRepository::save);
    }

    @Override
    public Flux<Invoice> findPaginated( Optional<Invoice.Type> type, Optional<String> month, int page, int pageSize,
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
