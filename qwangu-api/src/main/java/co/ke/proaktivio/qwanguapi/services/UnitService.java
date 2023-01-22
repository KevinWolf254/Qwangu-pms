package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface UnitService {
    Mono<Unit> create(UnitDto dto);
    Mono<Unit> update(String id, UnitDto dto);
    Mono<Unit> findById(String unitId);
    Flux<Unit> find(Optional<String> apartmentId, Optional<Unit.Status> status, Optional<String> accountNo, Optional<Unit.UnitType> type,
                    Optional<Unit.Identifier> identifier, Optional<Integer> floorNo,
                    Optional<Integer> bedrooms, Optional<Integer> bathrooms, OrderType order);
    Flux<Unit> findByOccupationIds(List<String> occupationIds);
    Mono<Unit> findByAccountNoAndIsBooked(String accountNo, Boolean isBooked);
    Mono<Unit> findByAccountNo(String accountNo);
    Mono<Boolean> deleteById(String id);
}
