package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import co.ke.proaktivio.qwanguapi.models.MpesaPayment;
import co.ke.proaktivio.qwanguapi.models.MpesaPayment.MpesaPaymentType;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentResponse;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.MpesaPaymentRepository;
import co.ke.proaktivio.qwanguapi.services.MpesaPaymentService;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
@RequiredArgsConstructor
public class MpesaPaymentServiceImpl implements MpesaPaymentService {
	private final PaymentService paymentService;
	private final MpesaPaymentRepository mpesaPaymentRepository;
	private final ReactiveMongoTemplate template;
	
	@Override
	public Mono<MpesaPaymentResponse> validate(MpesaPaymentDto dto) {
		return Mono.just(new MpesaPaymentResponse(0, "ACCEPTED"));
	}

	@Override
	public Mono<MpesaPaymentResponse> create(MpesaPaymentDto dto) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH);
		LocalDateTime transactionTime = LocalDateTime.parse(dto.getTransactionTime(), formatter)
				.atZone(ZoneId.of("Africa/Nairobi")).toLocalDateTime();

		var mpesaPayment = new MpesaPayment();
		mpesaPayment.setIsProcessed(false);
		mpesaPayment.setType(dto.getTransactionType().equals("Pay Bill") ? MpesaPaymentType.MPESA_PAY_BILL
				: MpesaPaymentType.MPESA_TILL);
		mpesaPayment.setTransactionId(dto.getTransactionId());
		mpesaPayment.setTransactionType(dto.getTransactionType());
		mpesaPayment.setTransactionTime(transactionTime);
		mpesaPayment.setCurrency(Unit.Currency.KES);
		mpesaPayment.setAmount(BigDecimal.valueOf(Double.parseDouble(dto.getAmount())));
		mpesaPayment.setShortCode(dto.getShortCode());
		mpesaPayment.setReferenceNumber(dto.getReferenceNumber());
		mpesaPayment.setInvoiceNo(dto.getInvoiceNo());
		mpesaPayment.setBalance(dto.getAccountBalance());
		mpesaPayment.setThirdPartyId(dto.getThirdPartyId());
		mpesaPayment.setMobileNumber(dto.getMobileNumber());
		mpesaPayment.setFirstName(dto.getFirstName());
		mpesaPayment.setMiddleName(dto.getMiddleName());
		mpesaPayment.setLastName(dto.getLastName());

		return mpesaPaymentRepository.save(mpesaPayment).doOnSuccess(payment -> log.info("Created " + payment))
				.flatMap(mPayment -> {
					var payment = new Payment.PaymentBuilder()
					.setType(PaymentType.MOBILE)
					.occupationNumber(mPayment.getReferenceNumber()).referenceNumber(mPayment.getTransactionId())
					.currency(Currency.KES).amount(mPayment.getAmount()).build();
					

					return paymentService.create(payment)
							.map($ -> {
								mpesaPayment.setIsProcessed(true);
								return mpesaPayment;
							})
							.flatMap(mpesaPaymentRepository::save)
							.doOnSuccess(m -> log.info("Updated: {}", m));
				})
				.then(Mono.just(new MpesaPaymentResponse(0, "ACCEPTED")));
	}

	@Override
	public Mono<MpesaPayment> findById(String mpesaPaymentId) {
		return mpesaPaymentRepository.findById(mpesaPaymentId);
	}

	@Override
	public Flux<MpesaPayment> findAll(String transactionId, String referenceNumber, String shortCode, OrderType order) {
		Query query = new Query();
		if (StringUtils.hasText(transactionId))
            query.addCriteria(Criteria.where("transactionId").regex(".*" +transactionId.trim()+ ".*", "i"));

		if (StringUtils.hasText(referenceNumber))
			query.addCriteria(Criteria.where("referenceNumber").regex(".*" + referenceNumber.trim() + ".*", "i"));

		if (StringUtils.hasText(shortCode))
			query.addCriteria(Criteria.where("type").is(shortCode));

		Sort sort = order != null
				? order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) : Sort.by(Sort.Order.desc("id"))
				: Sort.by(Sort.Order.desc("id"));
		query.with(sort);
		return template.find(query, MpesaPayment.class);
	}

	@Override
	public Mono<MpesaPayment> findByTransactionId(String transactionId) {
		return mpesaPaymentRepository.findByTransactionId(transactionId);
	}

}
