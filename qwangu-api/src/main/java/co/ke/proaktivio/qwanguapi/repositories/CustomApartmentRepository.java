package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface CustomApartmentRepository {

    Mono<Apartment> create(ApartmentDto dto);

    Mono<Apartment> update(String id, ApartmentDto dto);

    Flux<Apartment> findPaginated(Optional<String> optionalId, Optional<String> optionalApartmentName, int page, int pageSize, OrderType order);

    Mono<String> delete(String id);
}
