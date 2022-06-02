package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.User;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String>, CustomUserRepository {
}
