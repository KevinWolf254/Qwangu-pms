package co.ke.proaktivio.qwanguapi.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import co.ke.proaktivio.qwanguapi.models.MpesaPayment;
import reactor.core.publisher.Mono;

public interface MpesaPaymentRepository extends ReactiveMongoRepository<MpesaPayment, String> {
	Mono<MpesaPayment> findByTransactionId(String transactionId);
}
