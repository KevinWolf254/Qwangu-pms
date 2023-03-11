package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.UserToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserTokenRepository extends ReactiveMongoRepository<UserToken, String> {
}
