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
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
                                .where("isActive").is(true)
                                .and("vacatingOn").is(LocalDate.now().minusDays(1))))
                .flatMapMany(query -> template.find(query, Notice.class))
                .doOnNext(n -> System.out.println("---- Found: " +n))
                .flatMap(notice -> occupationRepository.findById(notice.getOccupationId())
                        .filter(Objects::nonNull)
                        .doOnSuccess(occupation -> System.out.println("---- Found:" + occupation))
                        .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
                        .flatMap(occupation -> unitRepository.findById(occupation.getUnitId())
                                .filter(Objects::nonNull)
                                .doOnSuccess(unit -> System.out.println("---- Found:" + unit))
                                .filter(unit -> unit.getStatus().equals(Unit.Status.OCCUPIED))
                                .map(unit -> {
                                    unit.setStatus(Unit.Status.VACANT);
                                    return unit;
                                })
                                .flatMap(unitRepository::save)
                                .doOnNext(u -> System.out.println("---- Saved: " +u))
                                .then(Mono.just(occupation)))
                        .map(occupation -> {
                            occupation.setStatus(Occupation.Status.PREVIOUS);
                            return occupation;
                        })
                        .flatMap(occupationRepository::save)
                        .doOnNext(occupation -> System.out.println("---- Saved: " +occupation))
                        .then(Mono.just(notice)))
                .map(notice -> {
                    notice.setIsActive(false);
                    return notice;
                })
                .flatMap(noticeRepository::save)
                .doOnNext(n -> System.out.println("---- Saved: " +n));
    }
}
