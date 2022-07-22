package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Booking;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.CreateBookingDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateBookingDto;
import co.ke.proaktivio.qwanguapi.repositories.*;
import co.ke.proaktivio.qwanguapi.services.BookingService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
//@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UnitRepository unitRepository;
    private final PaymentRepository paymentRepository;
    private final ReactiveMongoTemplate template;

    public BookingServiceImpl(BookingRepository bookingRepository, UnitRepository unitRepository,
                              PaymentRepository paymentRepository, ReactiveMongoTemplate template) {
        this.bookingRepository = bookingRepository;
        this.unitRepository = unitRepository;
        this.paymentRepository = paymentRepository;
        this.template = template;
    }

    @Override
    public Mono<Booking> create(CreateBookingDto dto) {
        String unitId = dto.getUnitId();
        String paymentId = dto.getPaymentId();
        return paymentRepository.findById(paymentId)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Payment with id %s does not exist!".formatted(paymentId))))
                .then(Mono.just(new Query()
                        .addCriteria(Criteria
                                .where("unitId").is(unitId)
                                .and("active").is(true))))
                .flatMap(query -> template.exists(query, Booking.class))
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Unit has already been booked!")))
                .then(unitRepository.findById(unitId))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Unit with id %s does not exist!".formatted(unitId))))
                .flatMap(unit -> {
                    Booking booking = new Booking(null, Booking.Status.PENDING_OCCUPATION, dto.getOccupation(), LocalDateTime.now(), null, unitId, paymentId);
                    if (unit.getStatus().equals(Unit.Status.VACANT)) {
                        return Mono.just(booking);
                    }
                    return validate(unit.getId(), dto.getOccupation())
                            .then(Mono.just(booking));
                })
                .flatMap(bookingRepository::save);
    }

    @Override
    public Mono<Booking> update(String id, UpdateBookingDto dto) {
        return bookingRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Booking with id %s does not exist!".formatted(id))))
                .filter(booking -> booking.getStatus().equals(Booking.Status.PENDING_OCCUPATION))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not update an already expired booking")))
                .flatMap(booking -> unitRepository.findById(booking.getUnitId())
                        .switchIfEmpty(Mono.error(new CustomNotFoundException("Unit does not exist!")))
                        .flatMap(unit -> {
                            booking.setOccupation(dto.getOccupation());
                            if (unit.getStatus().equals(Unit.Status.VACANT))
                                return Mono.just(booking);
                            return validate(unit.getId(), dto.getOccupation())
                                    .then(Mono.just(booking));
                        }))
                .flatMap(bookingRepository::save);
    }

    private Mono<Notice> validate (String unitId, LocalDateTime dto) {
        return Mono.just(new Query()
                        .addCriteria(Criteria
                                .where("unitId").is(unitId)
                                .and("active").is(true)))
                .flatMap(query -> template.findOne(query, Occupation.class))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation does not exist!")))
                .map(occupation -> new Query()
                        .addCriteria(Criteria
                                .where("occupationId").is(occupation.getId())))
                .flatMap(query -> template.findOne(query, Notice.class))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not book a unitId that is already occupied and not notice given!")))
                .filter(Notice::getActive)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not book a unitId that is already occupied and notice is disabled!")))
                .filter(notice -> dto.isAfter(notice.getVacatingDate()))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not occupy before current tenant's vacating date!")))
                .filter(notice -> dto.minusDays(14).isEqual(notice.getVacatingDate()) || dto.minusDays(14).isBefore(notice.getVacatingDate()))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Occupation date must be within 14 days after current tenant vacates!")));
    }

    @Override
    public Flux<Booking> findPaginated(Optional<String> id, Optional<Booking.Status> status, Optional<String> unitId, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        id.ifPresent(i -> query.addCriteria(Criteria.where("id").is(i)));
        status.ifPresent(s -> query.addCriteria(Criteria.where("status").is(s)));
        unitId.ifPresent(uId -> query.addCriteria(Criteria.where("unitId").is(uId)));

        query.with(pageable)
                .with(sort);
        return template
                .find(query, Booking.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Bookings were not found!")));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Booking.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Booking with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
