package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface OneTimeTokenRepository extends ReactiveMongoRepository<OneTimeToken, String> {
}
