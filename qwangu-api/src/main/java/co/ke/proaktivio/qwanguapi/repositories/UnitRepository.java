package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Unit;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UnitRepository extends ReactiveMongoRepository<Unit, String> {
}
