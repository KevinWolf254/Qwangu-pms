package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OccupationTransactionRepository extends ReactiveMongoRepository<OccupationTransaction, String> {
}
