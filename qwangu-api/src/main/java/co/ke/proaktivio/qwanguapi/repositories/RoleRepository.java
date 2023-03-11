package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.UserRole;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RoleRepository extends ReactiveMongoRepository<UserRole, String> {
}
