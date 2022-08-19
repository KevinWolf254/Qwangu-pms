package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Receivable;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.ReceivableDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;

public interface ReceivableService {
    Mono<Receivable> create(ReceivableDto dto);
    Mono<Receivable> update(String id, ReceivableDto dto);
    Flux<Receivable> findPaginated(Optional<String> id, Optional<Receivable.Type> type,
                                   Optional<LocalDate> period, int page, int pageSize, OrderType order);
    Mono<Boolean> deleteById(String id);
}
