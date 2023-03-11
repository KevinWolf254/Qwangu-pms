package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Refund;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RefundRepository extends ReactiveMongoRepository<Refund, String> {
}
