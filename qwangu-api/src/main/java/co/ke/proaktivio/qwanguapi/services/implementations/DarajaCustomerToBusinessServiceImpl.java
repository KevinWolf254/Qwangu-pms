package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import co.ke.proaktivio.qwanguapi.repositories.*;
import co.ke.proaktivio.qwanguapi.services.DarajaCustomerToBusinessService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class DarajaCustomerToBusinessServiceImpl implements DarajaCustomerToBusinessService {
    private final PaymentRepository paymentRepository;
    private final TenantRepository tenantRepository;
    private final OccupationRepository occupationRepository;
    private final UnitRepository unitRepository;
    private final ReceivableRepository receivableRepository;
    private final OccupationTransactionRepository occupationTransactionRepository;
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
                .map(p -> {
                    if (p.getReferenceNo() != null && !p.getReferenceNo().trim().isEmpty() && !p.getReferenceNo().trim().isBlank() && p.getReferenceNo().startsWith(bookingRegEx)) {
                        return paymentRepository.save(p)
                                .flatMap(payment -> template.findOne(new Query()
                                                .addCriteria(Criteria
                                                        .where("accountNo").is(payment.getReferenceNo().substring(3))), Unit.class)
                                        .filter(unit -> !unit.getIsBooked())
                                        .flatMap(unit -> template.findOne(new Query()
                                                        .addCriteria(Criteria
                                                                .where("unitId").is(unit.getId())
                                                                .and("status").is(Occupation.Status.CURRENT)
                                                                .orOperator(new Criteria()
                                                                        .and("status").is(Occupation.Status.PREVIOUS))), Occupation.class)
                                                .flatMap(occupation -> template.findOne(new Query()
                                                                .addCriteria(Criteria
                                                                        .where("occupationId").is(occupation.getId())), Notice.class)
                                                        .filter(Notice::getIsActive)
                                                        .filter(notice -> notice.getVacatingOn().isAfter(LocalDate.now()) || notice.getVacatingOn().isEqual(LocalDate.now()))
                                                        .flatMap(notice -> template.findOne(new Query()
                                                                        .addCriteria(Criteria
                                                                                .where("mobileNumber").is(payment.getMobileNumber())), Tenant.class)
                                                                .filter(Objects::nonNull)
                                                                .switchIfEmpty(Mono.just(new Tenant(null, payment.getFirstName(), payment.getMiddleName(), payment.getLastName(),
                                                                        payment.getMobileNumber(), null, LocalDateTime.now(), null)))
                                                                .flatMap(tenantRepository::save)
                                                                .map(tenant -> new Occupation(null, Occupation.Status.BOOKED, null, null, tenant.getId(), unit.getId(), LocalDateTime.now(), null))
                                                                .flatMap(newOccupation -> occupationRepository.save(newOccupation)
                                                                        .map(o -> {
                                                                            unit.setIsBooked(true);
                                                                            return unit;
                                                                        })
                                                                        .flatMap(unitRepository::save)
                                                                        .map($ -> {
                                                                            Map<String, BigDecimal> otherAmounts = new HashMap<>();
                                                                            otherAmounts.put("Booking", BigDecimal.valueOf(Long.getLong(dto.getAmount())));
                                                                            return new Receivable(null, Receivable.Type.BOOKING, null, null, null, null, otherAmounts, LocalDateTime.now(), null);
                                                                        })
                                                                        .flatMap(receivable -> receivableRepository.save(receivable)
                                                                                .map(savedReceivable -> new OccupationTransaction(null, BigDecimal.ZERO, receivable.getOtherAmounts().get("Booking"), BigDecimal.ZERO, newOccupation.getId(), receivable.getId(), payment.getId(), LocalDateTime.now())))
                                                                )
                                                        )
                                                )
                                        )
                                )
                                .then(Mono.just(p));
                    }
                    return paymentRepository.save(p);
                })
                .then(Mono.just(new DarajaCustomerToBusinessResponse<Integer>(0, "ACCEPTED")));
    }
}
