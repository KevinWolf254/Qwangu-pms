package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.pojos.OccupationTransactionDto;
import reactor.core.publisher.Mono;

public interface OccupationTransactionService {
    Mono<OccupationTransaction> create(OccupationTransactionDto dto);
}
