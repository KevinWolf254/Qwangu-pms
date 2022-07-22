package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Booking;
import co.ke.proaktivio.qwanguapi.pojos.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface BookingService {
    Mono<Booking> create(CreateBookingDto dto);

    Mono<Booking> update(String id, UpdateBookingDto dto);

    Flux<Booking> findPaginated(Optional<String> id, Optional<Booking.Status> status, Optional<String> unitId, int page, int pageSize, OrderType order);

    Mono<Boolean> deleteById(String id);
}
