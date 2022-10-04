package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.BookingRefund;
import co.ke.proaktivio.qwanguapi.pojos.BookingRefundDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.BookingRefundRepository;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.services.BookingRefundService;
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

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingRefundServiceImpl implements BookingRefundService {
    private final BookingRefundRepository bookingRefundRepository;
    private final InvoiceRepository invoiceRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<BookingRefund> create(BookingRefundDto dto) {
        String receivableId = dto.getReceivableId();
        return invoiceRepository.findById(receivableId)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Receivable with id %s does not exist!"
                        .formatted(receivableId))))
                .flatMap(receivable -> existsByReceivableId(receivableId)
                        .filter(exists -> !exists)
                        .switchIfEmpty(Mono.error(
                                new CustomNotFoundException("BookingRefund with receivable id %s already exists!"
                                        .formatted(receivableId))))
                        .then(Mono.just(receivable)))
                .filter(receivable -> {
                    Optional<BigDecimal> otherAmounts = receivable.getOtherAmounts().values().stream()
                            .reduce(BigDecimal::add);
                    BigDecimal paid = BigDecimal.ZERO
                            .add(receivable.getRentAmount() != null ? receivable.getRentAmount() : BigDecimal.ZERO)
                            .add(receivable.getSecurityAmount() != null ? receivable.getSecurityAmount() : BigDecimal.ZERO)
                            .add(receivable.getGarbageAmount() != null ? receivable.getGarbageAmount() : BigDecimal.ZERO)
                            .add(otherAmounts.orElse(BigDecimal.ZERO));
                    BigDecimal refund = dto.getAmount();
                    int comparison = paid.compareTo(refund);
                    return comparison >= 0;
                })
                .switchIfEmpty(Mono.error(
                        new CustomBadRequestException("Amount to be refunded cannot be greater than the amount paid!")))
                .map(receivable ->
                        new BookingRefund.BookingRefundBuilder()
                                .amount(dto.getAmount())
                                .refundDetails(dto.getRefundDetails())
                                .receivableId(dto.getReceivableId())
                                .build()
                )
                .flatMap(bookingRefundRepository::save);
    }

    public Mono<Boolean> existsByReceivableId(String receivableId) {
        Query query = new Query()
                .addCriteria(Criteria.where("receivableId").is(receivableId));
        return template.exists(query, BookingRefund.class);
    }

    @Override
    public Flux<BookingRefund> findPaginated(Optional<String> id, Optional<String> receivableId, int page, int pageSize,
                                             OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        id.ifPresent(i -> query.addCriteria(Criteria.where("id").is(i)));
        receivableId.ifPresent(state -> query.addCriteria(Criteria.where("receivableId").is(state)));

        query.with(pageable)
                .with(sort);
        return template
                .find(query, BookingRefund.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Refunds were not found!")));

    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, BookingRefund.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Refund with id %s does not exist!"
                        .formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
