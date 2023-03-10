package co.ke.proaktivio.qwanguapi.services;

import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.pojos.PropertyDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PropertyService {
    Mono<Property> create(PropertyDto dto);

    Mono<Property> update(String id, PropertyDto dto);

    Mono<Property> findById(String apartmentId);

    Flux<Property> find(@Nullable String name, @Nullable Property.PropertyType type, OrderType order);

    Mono<Boolean> deleteById(String id);
}
