package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.pojos.OccupationTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OccupationTransactionService {
	Mono<OccupationTransaction> create(OccupationTransactionDto dto);

	Mono<OccupationTransaction> findLatestByOccupationId(String occupationId);

	Mono<OccupationTransaction> findById(String occupationTransactionId);

	Flux<OccupationTransaction> findAll(OccupationTransaction.OccupationTransactionType type, String occupationId, String invoiceId, String receiptId, OrderType order);

}
