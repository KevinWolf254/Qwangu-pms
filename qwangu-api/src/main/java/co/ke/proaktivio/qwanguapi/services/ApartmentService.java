package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ApartmentService {
    Mono<Apartment> create(ApartmentDto dto);

    Mono<Apartment> update(String id, ApartmentDto dto);

    Mono<Apartment> findById(String apartmentId);

    Flux<Apartment> find(Optional<String> optionalApartmentName, OrderType order);

    Mono<Boolean> deleteById(String id);
}
