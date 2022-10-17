package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.pojos.CreditTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.DebitTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.services.*;
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

import java.math.BigDecimal;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class OccupationTransactionServiceImpl implements OccupationTransactionService {
    private final OccupationTransactionRepository occupationTransactionRepository;
    private final PaymentService paymentService;
    private final ReactiveMongoTemplate template;

    private Mono<Invoice> findInvoiceById(String id) {
        return template.findById(id, Invoice.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Invoice with id %s does not exist!"
                        .formatted(id))));
    }

    @Override
    public Mono<OccupationTransaction> createDebitTransaction(DebitTransactionDto dto) {
        return findOccupationById(dto.getOccupationId())
                .flatMap(occupation -> findInvoiceById(dto.getInvoiceId())
                        .flatMap(invoice -> findLatestByOccupationId(occupation.getId())
                                .switchIfEmpty(Mono.just(
                                        new OccupationTransaction.OccupationTransactionBuilder()
                                                .totalAmountCarriedForward(BigDecimal.ZERO)
                                                .occupationId(occupation.getId())
                                                .build()))
                                .flatMap(previousOccupationTransaction -> {
                                    OccupationTransaction ot = new OccupationTransaction();

                                    BigDecimal rentSecurityGarbage = BigDecimal.ZERO.add(invoice.getRentAmount()).add(invoice.getSecurityAmount()).add(invoice.getGarbageAmount());
                                    BigDecimal otherAmounts = invoice.getOtherAmounts() != null ? invoice.getOtherAmounts().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add) : BigDecimal.ZERO;
                                    BigDecimal totalAmountOwed = BigDecimal.ZERO.add(rentSecurityGarbage).add(otherAmounts);

                                    BigDecimal amountBroughtForward = previousOccupationTransaction.getTotalAmountCarriedForward();
                                    BigDecimal totalCarriedForward = BigDecimal.ZERO.add(rentSecurityGarbage).add(otherAmounts).add(amountBroughtForward);

                                    ot.setType(OccupationTransaction.Type.DEBIT);
                                    ot.setOccupationId(occupation.getId());
                                    ot.setInvoiceId(invoice.getId());
                                    ot.setTotalAmountOwed(totalAmountOwed);
                                    ot.setTotalAmountCarriedForward(totalCarriedForward);
                                    ot.setTotalAmountPaid(BigDecimal.ZERO);

                                    return occupationTransactionRepository.save(ot);
                                })
                                .doOnSuccess(t -> log.info(" Created: {}", t))
                        )
                );
    }

    private Mono<Occupation> findOccupationById(String id) {
        return template.findById(id, Occupation.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(id))));
    }

    private Mono<Receipt> findReceiptById(String id) {
        return template.findById(id, Receipt.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Receipt with id %s does not exist!".formatted(id))));
    }

    @Override
    public Mono<OccupationTransaction> createCreditTransaction(CreditTransactionDto dto) {
        return findOccupationById(dto.getOccupationId())
                .flatMap(occupation -> findReceiptById(dto.getReceiptId())
                                .flatMap(receipt -> paymentService.findById(receipt.getPaymentId())
                                                .flatMap(payment -> findLatestByOccupationId(occupation.getId())
                                                        .switchIfEmpty(Mono.just(
                                                                new OccupationTransaction.OccupationTransactionBuilder()
                                                                        .totalAmountCarriedForward(BigDecimal.ZERO)
                                                                        .occupationId(occupation.getId())
                                                                        .build()))
                                                        .flatMap(previousOccupationTransaction -> {
                                                            OccupationTransaction ot = new OccupationTransaction();

                                                            BigDecimal totalPayment = BigDecimal.ZERO.add(payment.getAmount());
                                                            BigDecimal amountBroughtForward = previousOccupationTransaction.getTotalAmountCarriedForward();

                                                            BigDecimal totalCarriedForward = totalPayment.subtract(amountBroughtForward);

                                                            ot.setType(OccupationTransaction.Type.CREDIT);
                                                            ot.setOccupationId(occupation.getId());
                                                            ot.setReceiptId(receipt.getId());
                                                            ot.setTotalAmountPaid(payment.getAmount());
                                                            ot.setTotalAmountCarriedForward(totalCarriedForward);
                                                            ot.setTotalAmountOwed(BigDecimal.ZERO);
                                                            return occupationTransactionRepository.save(ot);
                                                        })
                                                        .doOnSuccess(t -> log.info(" Created: {}", t))
                                                )
                                )
                );

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
    public Flux<OccupationTransaction> findPaginated(Optional<OccupationTransaction.Type> type, Optional<String> occupationId,
                                                     Optional<String> receivableId, Optional<String> paymentId, int page,
                                                     int pageSize, OrderType order) {
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
