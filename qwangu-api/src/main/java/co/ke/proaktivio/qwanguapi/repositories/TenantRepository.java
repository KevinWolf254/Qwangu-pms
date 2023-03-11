package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Tenant;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface TenantRepository extends ReactiveMongoRepository<Tenant, String> {
}
