package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Property;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface PropertyRepository extends ReactiveMongoRepository<Property, String> {
}
