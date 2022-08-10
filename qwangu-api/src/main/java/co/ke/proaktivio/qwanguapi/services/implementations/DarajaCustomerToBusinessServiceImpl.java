package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.services.DarajaCustomerToBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class DarajaCustomerToBusinessServiceImpl implements DarajaCustomerToBusinessService {
    private final PaymentRepository paymentRepository;

    @Override
    public Mono<DarajaCustomerToBusinessResponse> validate(DarajaCustomerToBusinessDto dto) {
        return Mono.just(new DarajaCustomerToBusinessResponse<Integer>(0, "ACCEPTED"));
    }

    @Override
    public Mono<DarajaCustomerToBusinessResponse> confirm(DarajaCustomerToBusinessDto dto) {
//        String bookingRegEx = "^(BOOK|book)";
        return Mono.just(dto)
                .map(r -> {
//                    Payment.Status status = r.getReferenceNumber() != null && !r.getReferenceNumber().trim().isEmpty() && !r.getReferenceNumber().trim().isBlank() ?
//                            r.getReferenceNumber().startsWith(bookingRegEx) ? Payment.Status.BOOKING_NEW : Payment.Status.RENT_NEW : Payment.Status.RENT_NEW;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH);
                    LocalDateTime transactionTime = LocalDateTime.parse(dto.getTransactionTime(), formatter).atZone(ZoneId.of("Africa/Nairobi")).toLocalDateTime();
                    return new Payment(null, dto.getTransactionType().equals("Pay Bill") ? Payment.Type.MPESA_PAY_BILL : Payment.Type.MPESA_TILL, r.getTransactionId(), r.getTransactionType(), transactionTime,
                            BigDecimal.valueOf(Double.parseDouble(r.getAmount())), r.getShortCode(), r.getReferenceNumber(), r.getInvoiceNo(), r.getAccountBalance(),
                                r.getThirdPartyId(), r.getMobileNumber(), r.getFirstName(), r.getMiddleName(), r.getLastName(),
                                LocalDateTime.now(), null);
                })
                .flatMap(paymentRepository::save)
                .then(Mono.just(new DarajaCustomerToBusinessResponse<Integer>(0, "ACCEPTED")));
    }
}
