package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.Booking;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.repositories.BookingRepository;
import co.ke.proaktivio.qwanguapi.repositories.NoticeRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class NoticeJobManager {
    private final NoticeRepository noticeRepository;
    private final OccupationRepository occupationRepository;
    private final UnitRepository unitRepository;
    private final ReactiveMongoTemplate template;
    private final BookingRepository bookingRepository;

    // TODO - UNCOMMENT
//    @Scheduled(cron = "0 0/1 * * * ?")
//    void processNotices() {
//        vacate()
//                .subscribeOn(Schedulers.parallel())
//                .subscribe();
//    }

    public Flux<Notice> vacate() {
        return Mono.just(new Query()
                        .addCriteria(Criteria
                                .where("status").is(Notice.Status.AWAITING_EXIT)
                                .and("vacatingDate").is(LocalDate.now().minusDays(1))))
                .flatMapMany(query -> template.find(query, Notice.class))
                .doOnNext(n -> System.out.println("---- Found: " +n))
                .flatMap(notice -> occupationRepository.findById(notice.getOccupationId())
                        .doOnSuccess(o -> System.out.println("---- Found:" + o))
                        .filter(Objects::nonNull)
                        .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
                        .flatMap(occupation -> unitRepository.findById(occupation.getUnitId())
                                .filter(Objects::nonNull)
                                .filter(unit -> unit.getStatus().equals(Unit.Status.OCCUPIED))
                                .flatMap(unit -> Mono.just(new Query()
                                                .addCriteria(Criteria
                                                        .where("status").is(Booking.Status.BOOKED)
                                                        .and("unitId").is(unit.getId())))
                                        .flatMap(query -> template.findOne(query, Booking.class))
                                        .filter(booking -> booking.getPaymentId() != null)
                                        .filter(Objects::nonNull)
                                        .map(booking -> {
                                            booking.setStatus(Booking.Status.PENDING_OCCUPATION);
                                            booking.setModified(LocalDateTime.now());
                                            return booking;
                                        })
                                        .flatMap(bookingRepository::save)
                                        .doOnNext(b -> System.out.println("---- Saved: " +b))
                                        .then(Mono.just(unit)))
                                .map(u -> {
                                    u.setStatus(Unit.Status.AWAITING_OCCUPATION);
                                    return u;
                                })
                                .flatMap(unitRepository::save)
                                .doOnNext(u -> System.out.println("---- Saved: " +u))
                                .then(Mono.just(occupation)))
                        .map(o -> {
                            o.setStatus(Occupation.Status.MOVED);
                            return o;
                        })
                        .flatMap(occupationRepository::save)
                        .doOnNext(o -> System.out.println("---- Saved: " +o))
                        .then(Mono.just(notice)))
                .map(n -> {
                    n.setStatus(Notice.Status.EXITED);
                    return n;
                })
                .flatMap(noticeRepository::save)
                .doOnNext(n -> System.out.println("---- Saved: " +n));
    }
}
