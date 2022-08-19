package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.BookingRefund;
import co.ke.proaktivio.qwanguapi.pojos.BookingRefundDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface BookingRefundService {
    Mono<BookingRefund> create(BookingRefundDto dto);
    Flux<BookingRefund> findPaginated(Optional<String> id, Optional<String> receivableId, int page, int pageSize,
                                      OrderType order);
    Mono<Boolean> deleteById(String id);
}
