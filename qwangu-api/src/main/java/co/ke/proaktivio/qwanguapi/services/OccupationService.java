package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface OccupationService {
    Mono<Occupation> create(CreateOccupationDto dto);

    Mono<Occupation> update(String id, UpdateOccupationDto dto);

    Flux<Occupation> findPaginated(Optional<String> id, Optional<Boolean> active, Optional<String> unitId, Optional<String> tenantId,
                             int page, int pageSize, OrderType order);

    Mono<Boolean> deleteById(String id);
}