package co.ke.proaktivio.qwanguapi.repositories.custom;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;

import java.util.Optional;

public interface CustomAuthorityRepository {
    Flux<Authority> findPaginated(Optional<String> optionalId, Optional<String> optionalApartmentName, int page, int pageSize, OrderType order);
}
