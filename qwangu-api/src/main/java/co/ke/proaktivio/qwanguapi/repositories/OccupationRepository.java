package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OccupationRepository extends ReactiveMongoRepository<Occupation, String> {
}
