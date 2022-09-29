package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.properties.BookingPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.*;
import co.ke.proaktivio.qwanguapi.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiPredicate;

@Service
@RequiredArgsConstructor
public class DarajaCustomerToBusinessServiceImpl implements DarajaCustomerToBusinessService {
    private final PaymentRepository paymentRepository;
    private final UnitService unitService;
    private final OccupationService occupationService;
    private final NoticeService noticeService;
    private final TenantService tenantService;
    private final ReceivableService receivableService;
    private final OccupationTransactionService occupationTransactionService;
    private final RentAdvanceService rentAdvanceService;
    private final UnitRepository unitRepository;
    private final BookingPropertiesConfig bookingProperties;

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
                .flatMap(payment -> {
                    if (checkReferenceNo.test("^(BOOK|book)", payment.getReferenceNo()))
                        return this.processBooking(payment);
                    if (checkReferenceNo.test("^(ADVANCE|advance)", payment.getReferenceNo()))
                        return this.processRentAdvance(payment);
                    return this.processRent(payment);
                })
                .then(Mono.just(new DarajaCustomerToBusinessResponse<>(0, "ACCEPTED")));
    }

    @Override
    @Transactional
    public Mono<Payment> processBooking(Payment payment) {
        String accountNo = payment.getReferenceNo().substring(4);
        return unitService
                .findByAccountNoAndIsBooked(accountNo, false)
                .doOnSuccess(u -> System.out.println("---- Found: " + u))
                .filter(unit -> {
                    double bookingPercentage = bookingProperties.getPercentage() / 100;
                    double bookingAmountRequired = bookingPercentage * unit.getRentPerMonth().doubleValue();
                    return payment.getAmount().doubleValue() >= bookingAmountRequired;
                })
                .flatMap(unit -> occupationService
                        .findByUnitIdAndNotBooked(unit.getId())
                        .doOnSuccess(u -> System.out.println("---- Found: " + u))
                        .flatMap(occupation -> noticeService.findByOccupationIdAndIsActive(occupation.getId(),
                                true))
                        .doOnSuccess(u -> System.out.println("---- Found: " + u))
                        .flatMap(notice -> tenantService.findTenantByMobileNumber(payment.getMobileNumber())
                                .doOnSuccess(u -> System.out.println("---- Found: " + u)))
                        .switchIfEmpty(tenantService.create(new TenantDto(payment.getFirstName(), payment.getMiddleName(),
                                payment.getLastName(), payment.getMobileNumber(), null)))
                        .doOnSuccess(u -> System.out.println("---- Created: " + u))
                        .flatMap(tenant -> occupationService.create(new OccupationDto(Occupation.Status.BOOKED,
                                null, null, tenant.getId(), unit.getId())))
                        .doOnSuccess(u -> System.out.println("---- Created: " + u))
                        .flatMap(occupation -> {
                            Map<String, BigDecimal> amounts = new HashMap<>(1);
                            amounts.put("booking", payment.getAmount());
                            return receivableService
                                    .create(new ReceivableDto(Receivable.Type.BOOKING, null,
                                            null, null, null, amounts))
                                    .doOnSuccess(u -> System.out.println("---- Created: " + u))
                                    .flatMap(receivable -> occupationTransactionService.create(
                                                    new OccupationTransactionDto(OccupationTransaction.Type.CREDIT, BigDecimal.ZERO,
                                                            receivable.getOtherAmounts().get("booking"),
                                                            BigDecimal.ZERO.subtract(receivable.getOtherAmounts().get("booking")),
                                                            occupation.getId(), receivable.getId(), payment.getId()))
                                            .doOnSuccess(u -> System.out.println("---- Created: " + u))
                                    );
                        })
                        .map($ -> {
                            unit.setIsBooked(true);
                            return unit;
                        })
                )
                .flatMap(unitRepository::save)
                .doOnSuccess(u -> System.out.println("---- Saved: " + u))
                .map($ -> {
                    payment.setStatus(Payment.Status.PROCESSED);
                    return payment;
                })
                .flatMap(paymentRepository::save)
                .doOnSuccess(u -> System.out.println("---- Saved: " + u));
    }

    @Override
    @Transactional
    public Mono<Payment> processRent(Payment payment) {
        return unitService
                .findByAccountNo(payment.getReferenceNo())
                .flatMap(unit -> occupationService.findByUnitIdAndStatus(unit.getId(), Occupation.Status.CURRENT)
                        .doOnSuccess(t -> System.out.println("---- Found: " + t))
                        .flatMap(occupation -> occupationTransactionService
                                .findLatestByOccupationId(occupation.getId())
                                .switchIfEmpty(Mono.just(new OccupationTransaction(null, null, BigDecimal.ZERO,
                                        BigDecimal.ZERO, BigDecimal.ZERO, occupation.getId(), null, "1")))
                                .doOnSuccess(t -> System.out.println("---- Found: " + t))
                                .flatMap(previousOT -> occupationTransactionService
                                        .create(new OccupationTransactionDto(OccupationTransaction.Type.CREDIT,
                                                BigDecimal.ZERO, payment.getAmount(),
                                                payment.getAmount().subtract(previousOT.getTotalAmountCarriedForward()),
                                                occupation.getId(), null, payment.getId()))
                                        .doOnSuccess(u -> System.out.println("---- Created: " + u))))
                )
                .map($ -> {
                    payment.setStatus(Payment.Status.PROCESSED);
                    return payment;
                })
                .flatMap(paymentRepository::save)
                .doOnSuccess(u -> System.out.println("---- Updated: " + u));
    }

    @Override
    @Transactional
    public Mono<Payment> processRentAdvance(Payment payment) {
        String accountNo = payment.getReferenceNo().substring(7);
        return unitService
                .findByAccountNoAndIsBooked(accountNo, false)
                .doOnSuccess(u -> System.out.println("---- Found: " + u))
                .flatMap(unit -> rentAdvanceService.create(new RentAdvanceDto(RentAdvance.Status.HOLDING,
                        null, payment.getId())))
                .doOnSuccess(u -> System.out.println("---- Saved: " + u))
                .map($ -> {
                    payment.setStatus(Payment.Status.PROCESSED);
                    return payment;
                })
                .flatMap(paymentRepository::save)
                .doOnSuccess(u -> System.out.println("---- Saved: " + u));
    }
}
