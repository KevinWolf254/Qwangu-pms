package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.*;
import co.ke.proaktivio.qwanguapi.services.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiPredicate;

@Log4j2
@Service
@RequiredArgsConstructor
public class DarajaCustomerToBusinessServiceImpl implements MpesaC2BService {
    private final PaymentRepository paymentRepository;
    private final OccupationService occupationService;
    private final ReceiptService receiptService;

    private final BiPredicate<String, String> checkReferenceNo = (RegEx, referenceNo) -> referenceNo != null &&
            !referenceNo.trim().isEmpty() && !referenceNo.trim().isBlank() &&
            referenceNo.trim().toLowerCase().startsWith(RegEx);

    @SuppressWarnings("rawtypes")
	@Override
    public Mono<MpesaC2BResponse> validate(MpesaC2BDto dto) {
        return Mono.just(new MpesaC2BResponse<>(0, "ACCEPTED"));
    }

    @SuppressWarnings("rawtypes")
	@Override
    public Mono<MpesaC2BResponse> confirm(MpesaC2BDto dto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH);
        LocalDateTime transactionTime = LocalDateTime.parse(dto.getTransactionTime(), formatter).atZone(ZoneId.of("Africa/Nairobi")).toLocalDateTime();

        var payment = new Payment.PaymentBuilder()
                .status(Payment.Status.NEW)
                .type(dto.getTransactionType().equals("Pay Bill") ?
                        Payment.Type.MPESA_PAY_BILL :
                        Payment.Type.MPESA_TILL)
                .transactionId(dto.getTransactionId())
                .transactionType(dto.getTransactionType())
                .transactionTime(transactionTime)
                .currency(Unit.Currency.KES)
                .amount(BigDecimal.valueOf(Double.parseDouble(dto.getAmount())))
                .shortCode(dto.getShortCode())
                .referenceNo(dto.getReferenceNumber())
                .invoiceNo(dto.getInvoiceNo())
                .balance(dto.getAccountBalance())
                .thirdPartyId(dto.getThirdPartyId())
                .mobileNumber(dto.getMobileNumber())
                .firstName(dto.getFirstName())
                .build();
        return paymentRepository.save(payment)
                .filter(p -> !checkReferenceNo.test("(?i)^(ADV#)", p.getReferenceNo()))
                .flatMap(this::processPayment)
                .then(Mono.just(new MpesaC2BResponse<>(0, "ACCEPTED")));
    }

    @Override
    @Transactional
    public Mono<Payment> processPayment(Payment payment) {
        return occupationService.findByOccupationNo(payment.getReferenceNo())
                .filter(Objects::nonNull)
                .flatMap(occupation -> receiptService.create(new ReceiptDto(occupation.getId(), payment.getId())))
                .map($ -> {
                    payment.setStatus(Payment.Status.PROCESSED);
                    return payment;
                })
                .flatMap(paymentRepository::save)
                .doOnSuccess(t -> log.info(" Updated: {}", t));
    }
}
