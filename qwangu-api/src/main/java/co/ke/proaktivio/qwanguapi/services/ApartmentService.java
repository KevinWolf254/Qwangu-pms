package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import reactor.core.publisher.Mono;

public interface ApartmentService {
    Mono<Apartment> create(ApartmentDto dto);
    Mono<Apartment> update(Apartment apartment);
    Mono<Void> deleteById(String id);
}
