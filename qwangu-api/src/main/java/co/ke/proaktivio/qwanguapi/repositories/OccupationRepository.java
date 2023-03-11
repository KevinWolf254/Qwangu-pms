package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OccupationRepository extends ReactiveMongoRepository<Occupation, String> {
}
