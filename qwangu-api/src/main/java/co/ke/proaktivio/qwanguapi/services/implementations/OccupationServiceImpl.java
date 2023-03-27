package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.services.*;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Log4j2
@Service
@RequiredArgsConstructor
public class OccupationServiceImpl implements OccupationService {
    private final OccupationRepository occupationRepository;
    private final UnitService unitService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    private final TenantService tenantService;
    private final NoticeService noticeService;
    private final ReactiveMongoTemplate template;
    private final PaymentRepository paymentRepository;

    /**
     * // check if unit is vacant
     * // if vacant check if it has a pending_occupation
     * // if no pending_occupation proceed to create tenant and occupation else don't proceed
     * // else if occupied check status of current occupant
     * // if occupant status is current don't create occupation
     * // else if occupant status is pending occupation don't create occupation
     * // else if occupant status is pending vacating check if occupation date is equal to vacating date
     * // if occupation date < vacating date don't create occupation
     * // else check if payment id exists and has not been processed
     * // if payment has been processed don't create occupation
     * // check if payment amount is equal or greater than rent advance((ADVANCE_PER_MONTH * RENT) + OTHER_AMOUNTS + SECURITY + GARBAGE)
     * // if payment amount < expected total don't create occupation
     * // else create occupation
     * // create an invoice for rent advance and rent from the remainder to create an occupation transaction
     * // create a receipt
     * // change payment status to processed
     **/
    @Override
    @Transactional
    public Mono<Occupation> create(OccupationForNewTenantDto dto) {
        String tenantId = dto.getTenantId();
        OccupationDto occupation = dto.getOccupation();
        if (StringUtils.hasText(tenantId)) {
            return create(tenantId, occupation);
        }
        return tenantService
                .create(dto.getTenant())
                .flatMap(tenant -> create(tenant.getId(), occupation));
    }

    @Override
    @Transactional
    public Mono<Occupation> create(String tenantId, OccupationDto occupation) {
        String unitId = occupation.getUnitId();
        String paymentId = occupation.getPaymentId();
        return unitService
                .findById(unitId)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Unit with id %s does not exist!".formatted(unitId))))
                .flatMap(unit -> {
                    if (unit.getStatus().equals(Unit.Status.VACANT)) {
                        var query = new Query()
                                .addCriteria(Criteria
                                        .where("unitId").is(unitId)
                                        .and("status").is(Occupation.Status.PENDING_OCCUPATION));
                        return template.exists(query, Occupation.class)
                                .filter(exists -> !exists)
                                .switchIfEmpty(Mono.error(new CustomBadRequestException("Unit has already been booked!")))
                                .flatMap(exists -> tenantService
                                            .findById(tenantId)
                                            .switchIfEmpty(Mono.error(new CustomBadRequestException("Tenant with id %s does not exist!"
                                                    .formatted(tenantId)))));
                    }
                    Query occupationPendingVacating = new Query()
                            .addCriteria(Criteria
                                    .where("unitId").is(unitId)
                                    .and("status").is(Occupation.Status.PENDING_VACATING));
                    return template
                            .findOne(occupationPendingVacating, Occupation.class)
                            .switchIfEmpty(Mono.error(new CustomBadRequestException("Unit is unavailable. Occupant has not given a vacating notice!")))
                            .flatMap(occupationVacating -> noticeService
                                    .findByOccupationIdAndIsActive(occupationVacating.getId(), Notice.Status.ACTIVE)
                                    .filter(notice -> occupation.getStartDate().isAfter(notice.getVacatingDate()))
                                    .switchIfEmpty(Mono.error(new CustomBadRequestException("Invalid occupation date. It should be after the current occupant's vacating date!"))))
                            .then(tenantService.findById(tenantId))
                            .switchIfEmpty(Mono.error(new CustomBadRequestException("Tenant with id %s does not exist!".formatted(tenantId))));

                })
                .flatMap(tenant -> paymentService
                        .findById(paymentId)
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("Payment with id %s does not exist!".formatted(paymentId))))
                        .filter(payment -> payment.getStatus().equals(PaymentStatus.UNCLAIMED) && StringUtils.hasText(payment.getOccupationNumber()))
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("Payment with id %s has already been processed!".formatted(paymentId)))))
                .flatMap(payment -> {
                    return unitService
                            .findById(unitId)
                            .flatMap(unit -> {
                                var totalRentAdvance = unit.getRentPerMonth().multiply(BigDecimal.valueOf(unit.getAdvanceInMonths()));
                                var totalOtherAdvance = unit.getOtherAmountsAdvance() != null ?
                                        unit.getOtherAmountsAdvance().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add) :
                                        BigDecimal.ZERO;
                                var totalAdvance = totalRentAdvance.add(totalOtherAdvance).add(unit.getSecurityAdvance() != null ?
                                        unit.getSecurityAdvance() :
                                        BigDecimal.ZERO).add(unit.getGarbageAdvance() != null ?
                                        unit.getGarbageAdvance() :
                                        BigDecimal.ZERO);
                                // calculate total payment for next month to be paid
                                BigDecimal totalOtherPerMonth = unit.getOtherAmountsPerMonth() != null ?
                                        unit.getOtherAmountsPerMonth().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add) :
                                        BigDecimal.ZERO;
                                var totalPerMonth = unit.getRentPerMonth()
                                        .add(unit.getSecurityPerMonth())
                                        .add(unit.getGarbagePerMonth())
                                        .add(totalOtherPerMonth);

                                var total = totalAdvance.add(totalPerMonth);
                                var paidAmount = payment.getAmount();

                                if (paidAmount.compareTo(total) < 0) {
                                    return Mono.error(new CustomBadRequestException("Amount to be paid should be %s %s but was %s %s"
                                            .formatted(unit.getCurrency(), total, unit.getCurrency(), paidAmount)));
                                }
                                return Mono.just(payment);
                            });
                })
                .flatMap(payment -> occupationRepository
                        .save(new Occupation.OccupationBuilder()
                                .status(Occupation.Status.PENDING_OCCUPATION)
                                .unitId(unitId)
                                .tenantId(tenantId)
                                .startDate(occupation.getStartDate())
                                .build())
                        .doOnSuccess(a -> log.info("Created: {}", a))
                        .flatMap(occupationPending -> {
                            var startDate = occupationPending.getStartDate();
                            return unitService
                                    .findById(unitId)
                                    .flatMap(unit -> {
                                        return invoiceService
                                                .create(new InvoiceDto(Invoice.Type.RENT_ADVANCE,null, null, null, null, occupationPending.getId()))
                                                .flatMap(invoice -> invoiceService
                                                        .create(new InvoiceDto(Invoice.Type.RENT, startDate, startDate.with(lastDayOfMonth()),null, null,
                                                                occupationPending.getId())))
                                                .flatMap(invoice -> receiptService.create(new ReceiptDto(occupationPending.getId(), paymentId)))
                                                .map($ -> {
                                                    payment.setStatus(PaymentStatus.CLAIMED);
                                                    return payment;
                                                })
                                                .flatMap(paymentRepository::save)
                                                .doOnSuccess(a -> log.info("Updated {}", a));
                                    })
                                    .flatMap(p -> Mono.just(occupationPending));
                        })
                );
    }

    @Override
    public Mono<Occupation> findById(String id) {
        return occupationRepository.findById(id);
    }

    @Override
    public Mono<Occupation> findByUnitId(String unitId) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("unitId").is(unitId)), Occupation.class);
    }

    @Override
    public Mono<Occupation> findByNumber(String number) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("number").is(number)), Occupation.class);
    }

    @Override
    public Mono<Occupation> findByUnitIdAndStatus(String unitId, Occupation.Status status) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("unitId").is(unitId)
                        .and("status").is(status))
                .with(Sort.by(Sort.Direction.DESC, "createdOn")), Occupation.class);
    }

    @Override
    public Flux<Occupation> findByStatus(List<Occupation.Status> statuses) {
        return template
                .find(new Query().addCriteria(Criteria
                        .where("status").in(statuses)), Occupation.class);
    }

    @Override
    public Mono<Occupation> findByUnitIdAndNotBooked(String unitId) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("unitId").is(unitId)
                        .and("status").in(Occupation.Status.CURRENT, Occupation.Status.VACATED))
                .with(Sort.by(Sort.Direction.DESC, "createdOn")), Occupation.class);
    }

    @Override
    public Flux<Occupation> findAll(Occupation.Status status, String occupationNo, String unitId,
                                    String tenantId, OrderType order) {        
        Query query = new Query();
        if(StringUtils.hasText(occupationNo))
            query.addCriteria(Criteria.where("number").regex(".*" + occupationNo.trim() + ".*", "i"));
        if (status != null)
        	query.addCriteria(Criteria.where("status").is(status));
        if (StringUtils.hasText(unitId))
            query.addCriteria(Criteria.where("unitId").is(unitId.trim()));
        if(StringUtils.hasText(tenantId))
            query.addCriteria(Criteria.where("tenantId").is(tenantId.trim()));

        Sort sort = order != null ? order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id")) :
                    Sort.by(Sort.Order.desc("id"));
        query.with(sort);
        return template.find(query, Occupation.class);
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Occupation.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!"
                        .formatted(id))))
                .filter(occupation -> occupation.getStatus().equals(Occupation.Status.VACATED))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Occupation can not be deleted. Status is not %s."
                        .formatted(Occupation.Status.VACATED.getState()))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
