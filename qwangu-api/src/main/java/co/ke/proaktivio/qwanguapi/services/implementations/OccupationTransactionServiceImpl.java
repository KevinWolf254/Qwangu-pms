package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction.OccupationTransactionType;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.pojos.CreditTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.DebitTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OccupationTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.services.*;
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

import java.math.BigDecimal;

@Log4j2
@Service
@RequiredArgsConstructor
public class OccupationTransactionServiceImpl implements OccupationTransactionService {
    private final OccupationTransactionRepository occupationTransactionRepository;
    private final ReactiveMongoTemplate template;
	private final InvoiceEmailNotificationService invoiceEmailNotificationService;
	private final ReceiptEmailNotificationService receiptEmailNotificationService;
	
    @Override
    public Mono<OccupationTransaction> create(OccupationTransactionDto dto) {
    	if(dto.getType().equals(OccupationTransactionType.CREDIT)) 
    		return createCreditTransaction(new CreditTransactionDto(dto.getOccupationId(), dto.getReceiptId()));
    	return createDebitTransaction(new DebitTransactionDto(dto.getOccupationId(), dto.getInvoiceId()));
    }

	private Mono<OccupationTransaction> createDebitTransaction(DebitTransactionDto dto) {
		return findOccupationById(dto.getOccupationId()).flatMap(occupation -> findInvoiceById(dto.getInvoiceId())
				.flatMap(invoice -> findLatestByOccupationId(occupation.getId())
						.switchIfEmpty(Mono.just(new OccupationTransaction.OccupationTransactionBuilder()
								.totalAmountCarriedForward(BigDecimal.ZERO).occupationId(occupation.getId()).build()))
						.flatMap(previousOccupationTransaction -> Mono
								.just(debit(occupation, invoice, previousOccupationTransaction))
								.flatMap(occupationTransactionRepository::save)
								.doOnSuccess(t -> log.info("Created: {}", t))
								.flatMap(ot -> invoiceEmailNotificationService.create(occupation, invoice, previousOccupationTransaction)
										.map($ -> ot))
								)));
	}
    
	private OccupationTransaction debit(Occupation occupation, Invoice invoice,
			OccupationTransaction previousOccupationTransaction) {
		OccupationTransaction ot = new OccupationTransaction();

		BigDecimal rentSecurityGarbage = BigDecimal.ZERO
		        .add(invoice.getRentAmount() != null ? invoice.getRentAmount() : BigDecimal.ZERO)
		        .add(invoice.getSecurityAmount() != null ? invoice.getSecurityAmount() : BigDecimal.ZERO)
		        .add(invoice.getGarbageAmount() != null ? invoice.getGarbageAmount() : BigDecimal.ZERO);
		BigDecimal otherAmounts = invoice.getOtherAmounts() != null ?
		        invoice.getOtherAmounts().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add) :
		        BigDecimal.ZERO;
		BigDecimal totalAmountOwed = BigDecimal.ZERO.add(rentSecurityGarbage).add(otherAmounts);

		BigDecimal amountBroughtForward = previousOccupationTransaction.getTotalAmountCarriedForward();
		BigDecimal totalCarriedForward = BigDecimal.ZERO.add(rentSecurityGarbage).add(otherAmounts).add(amountBroughtForward);
		
		var previousOTId = previousOccupationTransaction.getId();
		
		ot.setType(OccupationTransaction.OccupationTransactionType.DEBIT);
		ot.setOccupationId(occupation.getId());
		ot.setInvoiceId(invoice.getId());
		ot.setTotalAmountOwed(totalAmountOwed);
		ot.setPreviousOccupationTransactionId(StringUtils.hasText(previousOTId) ? previousOTId: null);
		ot.setTotalAmountCarriedForward(totalCarriedForward);
		ot.setTotalAmountPaid(BigDecimal.ZERO);
		return ot;
	}
    
	private Mono<OccupationTransaction> createCreditTransaction(CreditTransactionDto dto) {
		return findOccupationById(dto.getOccupationId()).flatMap(occupation -> findReceiptById(dto.getReceiptId())
				.flatMap(receipt -> findPaymentById(receipt.getPaymentId())
						.flatMap(payment -> findLatestByOccupationId(occupation.getId())
								.switchIfEmpty(Mono.just(new OccupationTransaction.OccupationTransactionBuilder()
										.totalAmountCarriedForward(BigDecimal.ZERO).occupationId(occupation.getId())
										.build()))
								.flatMap(previousOccupationTransaction -> Mono
										.just(credit(occupation, receipt, payment, previousOccupationTransaction))
										.flatMap(occupationTransactionRepository::save)
										.doOnSuccess(t -> log.info("Created: {}", t))
										.flatMap(ot -> receiptEmailNotificationService.create(occupation, payment, previousOccupationTransaction)
												.map($ -> ot))))));
	}

	private OccupationTransaction credit(Occupation occupation, Receipt receipt, Payment payment,
			OccupationTransaction previousOccupationTransaction) {
		OccupationTransaction ot = new OccupationTransaction();

		BigDecimal amount = payment.getAmount();
		BigDecimal totalPayment = BigDecimal.ZERO.add(amount);
		BigDecimal amountBroughtForward = previousOccupationTransaction
				.getTotalAmountCarriedForward();

		BigDecimal totalCarriedForward = totalPayment.subtract(amountBroughtForward);
		
        var previousOTId = previousOccupationTransaction.getId();

		ot.setType(OccupationTransaction.OccupationTransactionType.CREDIT);
		ot.setOccupationId(occupation.getId());
		ot.setReceiptId(receipt.getId());
		ot.setTotalAmountPaid(amount);
        ot.setPreviousOccupationTransactionId(StringUtils.hasText(previousOTId) ? previousOTId: null);
		ot.setTotalAmountCarriedForward(totalCarriedForward);
		ot.setTotalAmountOwed(BigDecimal.ZERO);
		return ot;
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
        return occupationTransactionRepository.findById(occupationTransactionId);
    }

	@Override
	public Flux<OccupationTransaction> findAll(OccupationTransaction.OccupationTransactionType type, String occupationId,
			String invoiceId, String receiptId, OrderType order) {
		boolean hasReceiptId = StringUtils.hasText(receiptId);
		boolean hasInvoiceId = StringUtils.hasText(invoiceId);
		
		if(hasReceiptId && hasInvoiceId)
			return Flux.error(new CustomBadRequestException("Choose either invoiceId or receiptId. Both will not exist!"));

		Query query = new Query();
		if (type != null) {
			if(type.equals(OccupationTransactionType.CREDIT) && hasInvoiceId)
				return Flux.error(new CustomBadRequestException("CREDIT will not have an invoice id!"));
			if(type.equals(OccupationTransactionType.DEBIT) && hasReceiptId)
				return Flux.error(new CustomBadRequestException("DEBIT will not have an receipt id!"));
			
			query.addCriteria(Criteria.where("type").is(type));
		}
		if (StringUtils.hasText(occupationId))
			query.addCriteria(Criteria.where("occupationId").is(occupationId.trim()));
		if (hasInvoiceId)
			query.addCriteria(Criteria.where("invoiceId").is(invoiceId.trim()));
		if (hasReceiptId)
			query.addCriteria(Criteria.where("receiptId").is(receiptId.trim()));		

		Sort sort = order != null
				? order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) : Sort.by(Sort.Order.desc("id"))
				: Sort.by(Sort.Order.desc("id"));
		query.with(sort);

		return template.find(query, OccupationTransaction.class);
	}

    private Mono<Occupation> findOccupationById(String id) {
        return template.findById(id, Occupation.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(id))));
    }

    private Mono<Receipt> findReceiptById(String id) {
        return template.findById(id, Receipt.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Receipt with id %s does not exist!".formatted(id))));
    }

    private Mono<Payment> findPaymentById(String id) {
        return template.findById(id, Payment.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Payment with id %s does not exist!".formatted(id))));
    }
    
    private Mono<Invoice> findInvoiceById(String id) {
        return template.findById(id, Invoice.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Invoice with id %s does not exist!"
                        .formatted(id))));
    }
}
