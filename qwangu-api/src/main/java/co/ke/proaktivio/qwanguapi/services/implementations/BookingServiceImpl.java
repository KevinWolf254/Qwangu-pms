package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Booking;
import co.ke.proaktivio.qwanguapi.pojos.CreateBookingDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateBookingDto;
import co.ke.proaktivio.qwanguapi.repositories.BookingRepository;
import co.ke.proaktivio.qwanguapi.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;

    @Override
    public Mono<Booking> create(CreateBookingDto dto) {
        String unitId = dto.getUnitId();
        String paymentId = dto.getPaymentId();
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
