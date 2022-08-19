package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.RentAdvance;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.RentAdvanceDto;
import co.ke.proaktivio.qwanguapi.pojos.UpdateRentAdvanceDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface RentAdvanceService {
    Mono<RentAdvance> create(RentAdvanceDto dto);
    Mono<RentAdvance> update(String id, UpdateRentAdvanceDto dto);
    Flux<RentAdvance> findPaginated(Optional<String> id, Optional<RentAdvance.Status> status,
                                    Optional<String> occupationId, Optional<String> paymentId, int page,
                                    int pageSize, OrderType order);
    Mono<Boolean> deleteById(String id);
}
