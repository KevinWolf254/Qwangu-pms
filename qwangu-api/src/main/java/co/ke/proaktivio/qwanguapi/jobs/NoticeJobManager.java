package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.configs.properties.NoticePropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;

@Log4j2
@Component
@RequiredArgsConstructor
public class NoticeJobManager {
    private final RefundRepository refundRepository;
    private final NoticeRepository noticeRepository;
    private final OccupationRepository occupationRepository;
    private final UnitRepository unitRepository;
    private final ReactiveMongoTemplate template;
    private final BookingRefundRepository bookingRefundRepository;
    private final InvoiceRepository invoiceRepository;
    private final NoticePropertiesConfig npc;

    // TODO - UNCOMMENT
    @Scheduled(cron = "0 0/1 * * * ?")
    void processNotices() {
        vacate()
                .subscribeOn(Schedulers.parallel())
                .subscribe();
    }

    /**
     * find all notices that are active and vacating date is yesterday
     * find the occupation for those notices
     * find the units for those occupants that have status of OCCUPIED
     * find the invoice for that occupation
     * calculate amount to be refunded depending on the number of days for notice period
     * change status of unit to VACANT
     * change status of occupation to VACATED
     * change status of notice to not active
     *
     * @return
     */
    public Flux<Notice> vacate() {
        return Mono.just(new Query()
                        .addCriteria(Criteria
                                .where("status").is("ACTIVE")
                                .and("vacatingDate").is(LocalDate.now().minusDays(1))))
                .flatMapMany(query -> template.find(query, Notice.class))
                .doOnNext(n -> log.info(" Found " + n))
                .flatMap(notice -> occupationRepository.findById(notice.getOccupationId())
                        .filter(Objects::nonNull)
                        .doOnSuccess(occupation -> log.info(" Found " + occupation))
                        .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
                        .flatMap(occupation -> unitRepository
                                .findById(occupation.getUnitId())
                                .doOnSuccess(unit -> log.info(" Found " + unit))
                                .flatMap(unit -> template.findOne(new Query()
                                                .addCriteria(Criteria
                                                        .where("type").is("RENT_ADVANCE")
                                                        .and("occupationId").is(occupation.getId())), Invoice.class)
                                        .map(invoice -> {
                                            var refund = new Refund();
                                            refund.setCurrency(invoice.getCurrency());

                                            Long noOfDays = getDiffInDays.apply(notice.getVacatingDate(), notice.getNotificationDate());
                                            Map<String, BigDecimal> amounts = invoice.getOtherAmounts();
                                            Map<String, BigDecimal> otherAmounts = new HashMap<>(amounts != null ?
                                                    amounts.size() : 0);
                                            float penaltyPercentage;

                                            if (noOfDays >= 28) {
                                                penaltyPercentage = npc.getLevelOne();
                                            } else if (noOfDays >= 21) {
                                                penaltyPercentage = npc.getLevelTwo();
                                            } else if (noOfDays >= 14) {
                                                penaltyPercentage = npc.getLevelThree();
                                            } else if (noOfDays >= 7) {
                                                penaltyPercentage = npc.getLevelFour();
                                            } else {
                                                penaltyPercentage = npc.getLevelFive();
                                            }

                                            refund.setRent(invoice.getRentAmount().multiply(BigDecimal.valueOf(penaltyPercentage)));
                                            refund.setSecurity(invoice.getSecurityAmount().multiply(BigDecimal.valueOf(penaltyPercentage)));
                                            refund.setGarbage(invoice.getGarbageAmount().multiply(BigDecimal.valueOf(penaltyPercentage)));

                                            if(amounts != null) {
                                                for (Map.Entry<String, BigDecimal> entry : amounts.entrySet()) {
                                                    otherAmounts.put(entry.getKey(), entry.getValue().multiply(BigDecimal.valueOf(penaltyPercentage)));
                                                }
                                                refund.setOthers(otherAmounts);
                                            }

                                            refund.setStatus(Refund.Status.PENDING_REVISION);
                                            refund.setInvoiceId(invoice.getId());
                                            refund.setOccupationId(occupation.getId());
                                            return refund;
                                        })
                                        .flatMap(refundRepository::save)
                                        .doOnNext(n -> log.info(" Created " + n))
                                .map(refund -> {
                                            unit.setStatus(Unit.Status.VACANT);
                                            return unit;
                                        }))
                                .flatMap(unitRepository::save)
                                .doOnNext(n -> log.info(" Updated " + n))
                                .map(unit -> {
                                    occupation.setStatus(Occupation.Status.VACATED);
                                    occupation.setEndDate(notice.getVacatingDate());
                                    return occupation;
                                }))
                        .flatMap(occupationRepository::save)
                        .doOnNext(n -> log.info(" Updated " + n))
                        .map(occupation -> {
                            notice.setStatus(Notice.Status.FULFILLED);
                            return notice;
                        }))
                .flatMap(noticeRepository::save)
                .doOnNext(n -> log.info(" Updated " + n));
    }


    private final BiFunction<LocalDate, LocalDate, Long> getDiffInDays = (futureDate, pastDate) ->
            Math.abs(ChronoUnit.DAYS.between(futureDate, pastDate)) + 1;

    /**
     * find all notices that are active and vacating date is yesterday
     * find the occupation for those notices
     * find the units for those occupants that have status of OCCUPIED
     * change status of unit to VACANT
     * change status of occupation to VACATED
     * change status of notice to not active
     * @return
     */
//    public Flux<Notice> vacate() {
//        return Mono.just(new Query()
//                        .addCriteria(Criteria
//                                .where("isActive").is(true)
//                                .and("vacatingDate").is(LocalDate.now().minusDays(1))))
//                .flatMapMany(query -> template.find(query, Notice.class))
//                .doOnNext(n -> log.info(" Found " +n))
//                .flatMap(notice -> occupationRepository.findById(notice.getOccupationId())
//                        .filter(Objects::nonNull)
//                        .doOnSuccess(occupation -> log.info(" Found " + occupation))
//                        .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
//                        .flatMap(occupation -> unitRepository.findById(occupation.getUnitId())
//                                .filter(Objects::nonNull)
//                                .doOnSuccess(unit -> log.info(" Found " + unit))
//                                .filter(unit -> unit.getStatus().equals(Unit.Status.OCCUPIED))
//                                .map(unit -> {
//                                    unit.setStatus(Unit.Status.VACANT);
//                                    return unit;
//                                })
//                                .flatMap(unitRepository::save)
//                                .doOnNext(u -> log.info(" Saved " +u))
//                                .then(Mono.just(occupation)))
//                        .map(occupation -> {
//                            occupation.setStatus(Occupation.Status.VACATED);
//                            return occupation;
//                        })
//                        .flatMap(occupationRepository::save)
//                        .doOnNext(occupation -> log.info(" Saved " +occupation))
//                        .then(Mono.just(notice)))
//                .map(notice -> {
//                    notice.setIsActive(false);
//                    return notice;
//                })
//                .flatMap(noticeRepository::save)
//                .doOnNext(n -> log.info(" Saved " +n));
//    }
}
