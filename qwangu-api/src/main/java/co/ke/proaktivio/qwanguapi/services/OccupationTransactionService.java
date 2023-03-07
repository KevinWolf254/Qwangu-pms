package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.pojos.CreditTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.DebitTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OccupationTransactionService {
	Mono<OccupationTransaction> createDebitTransaction(DebitTransactionDto dto);

	Mono<OccupationTransaction> createCreditTransaction(CreditTransactionDto dto);

	Mono<OccupationTransaction> findLatestByOccupationId(String occupationId);

	Mono<OccupationTransaction> findById(String occupationTransactionId);

	Flux<OccupationTransaction> findAll(OccupationTransaction.Type type, String occupationId, String invoiceId, String receiptId, OrderType order);
}
