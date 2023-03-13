package co.ke.proaktivio.qwanguapi.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import co.ke.proaktivio.qwanguapi.models.MpesaPayment;

public interface MpesaPaymentRepository extends ReactiveMongoRepository<MpesaPayment, String> {

}
