package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Booking;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.CreateBookingDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateBookingDto;
import co.ke.proaktivio.qwanguapi.repositories.BookingRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.services.BookingService;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final UnitRepository unitRepository;
    private final PaymentRepository paymentRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Booking> create(CreateBookingDto dto) {
        String unitId = dto.getUnitId();
        String paymentId = dto.getPaymentId();
//        return paymentRepository.findById(paymentId)
//                .switchIfEmpty(Mono.error(new CustomNotFoundException("Payment with id %s does not exist!".formatted(paymentId))))
//                .then(Mono.just(new Query()
//                        .addCriteria(Criteria
//                                .where("unitId").is(unitId)
//                                .and("active").is(true))))
//                .flatMap(query -> template.exists(query, Booking.class))
//                .filter(exists -> !exists)
//                .switchIfEmpty(Mono.error(new CustomBadRequestException("Unit has already been booked!")))
//                .then(unitRepository.findById(unitId))
//                .switchIfEmpty(Mono.error(new CustomNotFoundException("Unit with id %s does not exist!".formatted(unitId))))
//                .map(unit -> {
//                    if(unit.getVacant())
//                        return new Booking(null, true, dto.getIn(), LocalDateTime.now(), null, unitId, paymentId);
//                    new Query()
//                            .addCriteria(Criteria
//                                    .where("unitId").is(unitId)
//                                    .and("active").is(true))
//                })
////                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not create a booking ")))
//                .then(Mono.just(new Booking(null, true, dto.getIn(), LocalDateTime.now(), null, unitId, paymentId)))
//                .flatMap(bookingRepository::save);
        return null;
    }

    @Override
    public Mono<Booking> update(String id, UpdateBookingDto dto) {
        return null;
    }

    @Override
    public Flux<Booking> findPaginated(Optional<String> id, Optional<String> unitId, int page, int pageSize, OrderType order) {
        return null;
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return null;
    }
}
