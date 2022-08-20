package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;

import java.util.Optional;

public interface AuthorityService {
    Flux<Authority> findPaginated(Optional<String> id, Optional<String> name, int page, int pageSize, OrderType order);
    Flux<Authority> findByRoleId(String roleId);
}
