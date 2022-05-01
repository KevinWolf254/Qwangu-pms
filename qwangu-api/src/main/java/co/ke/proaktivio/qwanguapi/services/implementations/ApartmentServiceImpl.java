package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.ApartmentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Apartment> create(ApartmentDto dto) {
        Query query = new Query()
                .addCriteria(Criteria.where("name").is(dto.getName()));
        return template
                .exists(query, Apartment.class)
                .flatMap(exists -> {
                            if (exists)
                                return Mono.error(new CustomAlreadyExistsException("Apartment %s already exists!".formatted(dto.getName())));
                            LocalDateTime now = LocalDateTime.now();
                            return template.save(new Apartment(dto.getName(), now, now));
                        }
                );
    }

    @Override
    public Mono<Apartment> update(String id, ApartmentDto dto) {
        return template
                .findById(id, Apartment.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartment with id $s does not exist!!".formatted(id))))
                .flatMap(apartment -> {
                    apartment.setName(dto.getName());
                    return template.save(apartment);
                });
    }

    @Override
    public Flux<Apartment> find(Optional<String> id, Optional<String> name, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Sort sort = order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        if(id.isPresent())
            query.addCriteria(Criteria.where("id").is(id));
        if (name.isPresent())
            query.addCriteria(Criteria.where("name").is(name));
        query.with(pageable)
                .with(sort);
        return template.find(query, Apartment.class);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return template
                .findById(id, Apartment.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartment with id %s does not exist!!".formatted(id))))
                .flatMap(apartment -> template.remove(apartment))
                .flatMap(result -> Mono.empty());
    }
}
