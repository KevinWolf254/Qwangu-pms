package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ApartmentService {
    Mono<Apartment> create(ApartmentDto dto);

    Mono<Apartment> update(String id, ApartmentDto dto);

    Flux<Apartment> findPaginated(Optional<String> id, Optional<String> name, int page, int pageSize, OrderType order);

    Flux<Apartment> findAll(Optional<String> optionalId, Optional<String> optionalApartmentName, OrderType order);

    Mono<Boolean> deleteById(String id);
}
