package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Payment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends ReactiveMongoRepository<Payment, String> {
}
