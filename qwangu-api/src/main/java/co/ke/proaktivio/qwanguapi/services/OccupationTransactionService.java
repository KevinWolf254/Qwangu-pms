package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.pojos.OccupationTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface OccupationTransactionService {
    Mono<OccupationTransaction> create(OccupationTransactionDto dto);
    Mono<OccupationTransaction> findLatestByOccupationId(String occupationId);
    Flux<OccupationTransaction> findPaginated(Optional<String> id, Optional<OccupationTransaction.Type> type,
                                              Optional<String> occupationId, Optional<String> receivableId,
                                              Optional<String> paymentId, int page, int pageSize, OrderType order);
}
