package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.DarajaCustomerToBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<DarajaCustomerToBusinessResponse> validate(DarajaCustomerToBusinessDto dto) {
        return Mono.just(new DarajaCustomerToBusinessResponse<Integer>(0, "ACCEPTED"));
    }

    @Override
    public Mono<DarajaCustomerToBusinessResponse> confirm(DarajaCustomerToBusinessDto dto) {
        String bookingRegEx = "^(BOOK|book)";
        return Mono.just(dto)
                .map(r -> {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ENGLISH);
                    LocalDateTime transactionTime = LocalDateTime.parse(dto.getTransactionTime(), formatter).atZone(ZoneId.of("Africa/Nairobi")).toLocalDateTime();
                    return new Payment(null, dto.getTransactionType().equals("Pay Bill") ? Payment.Type.MPESA_PAY_BILL : Payment.Type.MPESA_TILL, r.getTransactionId(), r.getTransactionType(), transactionTime,
                            BigDecimal.valueOf(Double.parseDouble(r.getAmount())), r.getShortCode(), r.getReferenceNumber(), r.getInvoiceNo(), r.getAccountBalance(),
                            r.getThirdPartyId(), r.getMobileNumber(), r.getFirstName(), r.getMiddleName(), r.getLastName(),
                            LocalDateTime.now(), null);
                })
                .map(payment -> {
                    if (payment.getReferenceNo() != null && !payment.getReferenceNo().trim().isEmpty() && !payment.getReferenceNo().trim().isBlank() && payment.getReferenceNo().startsWith(bookingRegEx)) {
                        return paymentRepository.save(payment)
                                .flatMap(p -> {
                                    return template.findOne(new Query()
                                            .addCriteria(Criteria
                                                    .where("accountNo").is(payment.getReferenceNo().substring(3))), Unit.class);
                                })
                                .filter(unit -> !unit.getIsBooked())
                                .flatMap(unit -> {
                                    return template
                                            .findOne(new Query()
                                                    .addCriteria(Criteria
                                                            .where("unitId").is(unit.getId())
                                                            .and("status").is(Occupation.Status.CURRENT)
                                                            .orOperator(new Criteria()
                                                                    .and("status").is(Occupation.Status.PREVIOUS))), Occupation.class)
                                            .flatMap(occupation -> {
                                                return template.findOne(new Query()
                                                        .addCriteria(Criteria
                                                                .where("occupationId").is(occupation.getId())), Notice.class);
                                            })
                                            .flatMap(notice -> {
                                                return null;
                                            });
                                })
                                .then(Mono.just(payment));
                    }
                    return paymentRepository.save(payment);
                })
                .then(Mono.just(new DarajaCustomerToBusinessResponse<Integer>(0, "ACCEPTED")));
    }
}
