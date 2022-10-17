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
public class DarajaCustomerToBusinessServiceImpl implements DarajaCustomerToBusinessService {
    private final PaymentRepository paymentRepository;
    private final OccupationService occupationService;
    private final ReceiptService receiptService;

    private final BiPredicate<String, String> checkReferenceNo = (RegEx, referenceNo) -> referenceNo != null &&
            !referenceNo.trim().isEmpty() && !referenceNo.trim().isBlank() &&
            referenceNo.trim().toLowerCase().startsWith(RegEx);

    @Override
    public Mono<DarajaCustomerToBusinessResponse> validate(DarajaCustomerToBusinessDto dto) {
        return Mono.just(new DarajaCustomerToBusinessResponse<>(0, "ACCEPTED"));
    }

    @Override
    @Transactional
    public Mono<DarajaCustomerToBusinessResponse> confirm(DarajaCustomerToBusinessDto dto) {
        return Mono.just(dto)
                .map(r -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH);
                    LocalDateTime transactionTime = LocalDateTime.parse(dto.getTransactionTime(), formatter).atZone(ZoneId.of("Africa/Nairobi")).toLocalDateTime();
                    return new Payment(null, Payment.Status.NEW, dto.getTransactionType().equals("Pay Bill") ? Payment.Type.MPESA_PAY_BILL : Payment.Type.MPESA_TILL, r.getTransactionId(), r.getTransactionType(), transactionTime,
                            BigDecimal.valueOf(Double.parseDouble(r.getAmount())), r.getShortCode(), r.getReferenceNumber(), r.getInvoiceNo(), r.getAccountBalance(),
                            r.getThirdPartyId(), r.getMobileNumber(), r.getFirstName(), r.getMiddleName(), r.getLastName());
                })
                .flatMap(paymentRepository::save)
                .filter(payment -> !checkReferenceNo.test("^(ADVANCE|advance)", payment.getReferenceNo()))
                .flatMap(this::processPayment)
                .then(Mono.just(new DarajaCustomerToBusinessResponse<>(0, "ACCEPTED")));
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
                .doOnSuccess(t -> log.info(" Created: {}", t));
    }
}
