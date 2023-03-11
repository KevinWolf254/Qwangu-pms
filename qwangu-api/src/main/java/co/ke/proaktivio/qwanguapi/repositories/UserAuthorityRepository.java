package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface UserAuthorityRepository extends ReactiveMongoRepository<UserAuthority, String> {
}
