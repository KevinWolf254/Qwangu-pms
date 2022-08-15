package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.Occupation;
//import co.ke.proaktivio.qwanguapi.models.RentInvoice;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//@Component
//@RequiredArgsConstructor
//public class RentJobManager {
//    private final RentTransactionRepository rentTransactionRepository;
//    private final UnitRepository unitRepository;
//    private final ReactiveMongoTemplate template;

    // TODO - UNCOMMENT
//    @Scheduled(cron = "${rent.cronToCreateInvoice}")
//    void createRentInvoice() {
//        create()
//                .subscribeOn(Schedulers.parallel())
//                .subscribe();
//    }


//    public Flux<?> create() {
//        return template.find(new Query()
//                        .addCriteria(Criteria.where("status").is(Occupation.Status.CURRENT)), Occupation.class)
//                .flatMap(o -> unitRepository.findById(o.getUnitId())
//                        .flatMap(u -> template
//                                .findOne(new Query()
//                                        .addCriteria(Criteria.where("occupationId").is(o.getId()))
//                                        .with(Sort.by(Sort.Direction.DESC, "id")), RentInvoice.class)
//                                .switchIfEmpty(Mono.just(new RentInvoice(null, null, BigDecimal.valueOf(u.getRentPerMonth()),
//                                        BigDecimal.valueOf(u.getSecurityPerMonth()), BigDecimal.valueOf(u.getGarbagePerMonth()),
//                                        BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
//                                        BigDecimal.ZERO, LocalDateTime.now(), null, o.getId(), null)))
//                                .map(r -> new RentInvoice(null, RentInvoice.Type.DEBIT, BigDecimal.valueOf(u.getRentPerMonth()),
//                                        BigDecimal.valueOf(u.getSecurityPerMonth()), BigDecimal.valueOf(u.getGarbagePerMonth()),
//                                        r.getRentAmountCarriedForward(), r.getSecurityAmountCarriedForward(),
//                                        r.getGarbageAmountCarriedForward(), r.getPenaltyAmount(),
//                                        r.getPenaltyAmountCarriedForward(), LocalDateTime.now(), null, o.getId(), null))))
//                .flatMap(rentTransactionRepository::save);
//        return null;
//    }
//}
