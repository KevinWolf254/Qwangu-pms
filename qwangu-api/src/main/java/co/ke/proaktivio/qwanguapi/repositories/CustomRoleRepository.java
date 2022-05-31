package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;

import java.util.Optional;

public interface CustomRoleRepository {
    Flux<Role> findPaginated(Optional<String> optionalId, Optional<String> optionalRoleName, int page, int pageSize, OrderType order);
}
