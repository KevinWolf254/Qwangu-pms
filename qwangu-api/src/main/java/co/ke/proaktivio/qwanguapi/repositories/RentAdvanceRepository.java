package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.RentAdvance;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RentAdvanceRepository extends ReactiveMongoRepository<RentAdvance, String> {
}
