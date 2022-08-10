package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import co.ke.proaktivio.qwanguapi.pojos.UnitsWithNoticeDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface UnitService {

    Mono<Unit> create(UnitDto dto);

    Mono<Unit> update(String id, UnitDto dto);

    Flux<Unit> findPaginated(Optional<String> id, Optional<Unit.Status> status, Optional<String> accountNo, Optional<Unit.Type> type,
                             Optional<Unit.Identifier> identifier, Optional<Integer> floorNo,
                             Optional<Integer> bedrooms, Optional<Integer> bathrooms, Optional<String> apartmentId, int page, int pageSize,
                             OrderType order);

    Flux<Unit> findUnitsByOccupationIds(List<String> occupationIds);

    Mono<Boolean> deleteById(String id);

}
