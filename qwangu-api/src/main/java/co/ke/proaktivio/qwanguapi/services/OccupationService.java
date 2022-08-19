package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface OccupationService {
    Mono<Occupation> create(OccupationDto dto);
    Mono<Occupation> update(String id, OccupationDto dto);
    Mono<Occupation> findByUnitId(String unitId);
    Mono<Occupation> findByUnitIdAndNotBooked(String unitId);
    Mono<Occupation> findByUnitIdAndStatus(String unitId, Occupation.Status status);
    Flux<Occupation> findByStatus(List<Occupation.Status> statuses);
    Flux<Occupation> findPaginated(Optional<String> id, Optional<Occupation.Status> status, Optional<String> unitId, Optional<String> tenantId,
                                                                                                  int page, int pageSize, OrderType order);
    Mono<Boolean> deleteById(String id);
}
