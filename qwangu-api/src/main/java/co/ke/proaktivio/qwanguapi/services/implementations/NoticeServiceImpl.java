package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Booking;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.CreateNoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateNoticeDto;
import co.ke.proaktivio.qwanguapi.repositories.BookingRepository;
import co.ke.proaktivio.qwanguapi.repositories.NoticeRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.NoticeService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final NoticeRepository noticeRepository;
    private final OccupationRepository occupationRepository;
    private final UnitRepository unitRepository;
    private final ReactiveMongoTemplate template;
    private final BookingRepository bookingRepository;

    @Override
    public Mono<Notice> create(CreateNoticeDto dto) {
        return Mono.just(dto)
                .flatMap(d -> occupationRepository.findById(d.getOccupationId()))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(dto.getOccupationId()))))
                .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not create notice of occupation that is not active!")))
                .then(Mono.just(new Notice(null, Notice.Status.AWAITING_EXIT, dto.getNotificationDate(), dto.getVacatingDate(), LocalDateTime.now(), null, dto.getOccupationId())))
                .flatMap(noticeRepository::save);
    }

    @Override
    public Mono<Notice> update(String id, UpdateNoticeDto dto) {
        return noticeRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Notice with id %s does not exist!".formatted(id))))
                .filter(notice -> notice.getStatus().equals(Notice.Status.AWAITING_EXIT))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not update notice that is inactive!")))
                .flatMap(notice -> occupationRepository
                        .findById(notice.getOccupationId())
                        .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(notice.getOccupationId()))))
                        .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
                        .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not update notice of occupation that is not active!")))
                        .then(Mono.just(notice)))
                .map(notice -> {
                    if (dto.getStatus() != null)
                        notice.setStatus(dto.getStatus());
                    if (dto.getNotificationDate() != null)
                        notice.setNotificationDate(dto.getNotificationDate());
                    if (dto.getVacatingDate() != null)
                        notice.setVacatingDate(dto.getVacatingDate());
                    notice.setModified(LocalDateTime.now());
                    return notice;
                })
                .flatMap(noticeRepository::save);
    }

    @Override
    public Flux<Notice> findPaginated(Optional<String> id, Optional<Notice.Status> status, Optional<String> occupationId,
                                      int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        id.ifPresent(i -> query.addCriteria(Criteria.where("id").is(i)));
        status.ifPresent(s -> query.addCriteria(Criteria.where("status").is(s)));
        occupationId.ifPresent(i -> query.addCriteria(Criteria.where("occupationId").is(i)));
        query.with(pageable)
                .with(sort);
        return template
                .find(query, Notice.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Notices were not found!")));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Notice.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Notice with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }

    @Scheduled(cron = "0 0/1 * * * ?")
    protected void processNotices() {

        Mono.just(new Query()
                        .addCriteria(Criteria
                                .where("status").is(Notice.Status.AWAITING_EXIT)
                                .and("vacatingDate").is(LocalDate.now().minusDays(1))))
                .flatMapMany(query -> template.find(query, Notice.class))
                .flatMap(notice -> occupationRepository.findById(notice.getOccupationId())
                        .filter(Objects::nonNull)
                        .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
                        .flatMap(occupation -> unitRepository.findById(occupation.getUnitId())
                                .filter(Objects::nonNull)
                                .filter(unit -> unit.getStatus().equals(Unit.Status.OCCUPIED))
                                .flatMap(unit -> Mono.just(new Query()
                                                .addCriteria(Criteria
                                                        .where("status").is(Booking.Status.PENDING_OCCUPATION)
                                                        .and("unitId").is(unit.getId())))
                                        .flatMap(query -> template.findOne(query, Booking.class))
                                        .filter(Objects::nonNull)
                                        .map(booking -> {
                                            booking.setStatus(Booking.Status.FULFILLED);
                                            booking.setModified(LocalDateTime.now());
                                            return booking;
                                        })
                                        .flatMap(bookingRepository::save)
                                        .then(Mono.just(unit)))
                                .map(u -> {
                                    u.setStatus(Unit.Status.AWAITING_OCCUPATION);
                                    return u;
                                })
                                .flatMap(unitRepository::save)
                                .then(Mono.just(occupation)))
                        .map(o -> {
                            o.setStatus(Occupation.Status.MOVED);
                            return o;
                        })
                        .flatMap(occupationRepository::save)
                        .then(Mono.just(notice)))
                .map(n -> {
                    n.setStatus(Notice.Status.EXITED);
                    return n;
                })
                .flatMap(noticeRepository::save)
                .subscribe();

    }
}


//        Mono.just(new Query()
//                        .addCriteria(Criteria
//                                .where("status").is(Notice.Status.AWAITING_EXIT)
//                                .and("vacatingDate").is(LocalDate.now().minusDays(1))))
//                .flatMap(query -> template.find(query, Notice.class).collectList())
//                .flatMap(notices -> {
//                    notices.stream().flatMap(notice -> {
//                        return occupationRepository.findById(notice.getOccupationId())
//                                .filter(Objects::nonNull)
//                                .filter(Occupation::getActive)
//                                .flatMap(occupation -> unitRepository.findById(occupation.getUnitId())
//                                        .filter(Objects::nonNull)
//                                        .filter(unit -> unit.getStatus().equals(Unit.Status.OCCUPIED))
//                                        .flatMap(unit -> Mono.just(new Query()
//                                                        .addCriteria(Criteria
//                                                                .where("status").is(Booking.Status.PENDING_OCCUPATION)
//                                                                .and("unitId").is(unit.getId())))
//                                                .flatMap(query -> template.findOne(query, Booking.class))
//                                                .filter(Objects::nonNull)
//                                                .map(booking -> {
//                                                    booking.setStatus(Booking.Status.OCCUPIED);
//                                                    booking.setModified(LocalDateTime.now());
//                                                    return booking;
//                                                }).flatMap(bookingRepository::save)
//                                                .map($ -> {
//                                                    unit.setStatus(Unit.Status.AWAITING_OCCUPATION);
//                                                    return unit;
//                                                })
//                                                .flatMap(unitRepository::save))
//                                        .map($ -> {
//                                            occupation.setActive(false);
//                                            return occupation;
//                                        })
//                                        .flatMap(occupationRepository::save))
//                                .map($ -> {
//                                    notice.setStatus(Notice.Status.EXITED);
//                                    return notice;
//                                }));
//                    }));
//
//                });


//                    template.find(query, Notice.class)
//                            .flatMap(notice -> occupationRepository.findById(notice.getOccupationId())
//                                    .flatMap(occupation -> unitRepository.findById(occupation.getUnitId())
//                                            .flatMap(unit -> {
//                                                Query queryBookingPendingOccupation = new Query()
//                                                        .addCriteria(Criteria
//                                                                .where("status").is(Booking.Status.PENDING_OCCUPATION)
//                                                                .and("unitId").is(unit.getId()));
//                                                return template.findOne(queryBookingPendingOccupation, Booking.class)
//                                                        .map(booking -> {
//                                                            if (booking != null) {
//                                                                booking.setStatus(Booking.Status.OCCUPIED);
//                                                                return bookingRepository.save(booking);
//                                                            }
//                                                        })
//                                            }))
//                }