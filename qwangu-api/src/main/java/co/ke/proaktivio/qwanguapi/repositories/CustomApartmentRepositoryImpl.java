package co.ke.proaktivio.qwanguapi.repositories;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomApartmentRepositoryImpl implements CustomApartmentRepository {
    private final ReactiveMongoTemplate template;

    public Flux<Apartment> find(Query query) {
        return template.find(query, Apartment.class);
    }

    @Override
    public Mono<Apartment> create(ApartmentDto dto) {
        String name = dto.getName();
        Query query = new Query()
                .addCriteria(Criteria.where("name").is(name));
        return template
                .exists(query, Apartment.class)
                .flatMap(exists -> {
                    if (exists)
                        throw new CustomAlreadyExistsException("Apartment %s already exists!".formatted(name));
                    LocalDateTime now = LocalDateTime.now();
                    Mono<Apartment> saved = template.save(new Apartment(name, now, now));
                    return saved;
                });
    }

    @Override
    public Mono<Apartment> update(String id, ApartmentDto dto) {
        String name = dto.getName();
        Query query = new Query()
                .addCriteria(Criteria.where("name").is(name));
        Mono<Apartment> findByIdResult = template.findById(id, Apartment.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartment with id %s does not exists!".formatted(id))));
        Mono<List<Apartment>> findByNameResult = template.find(query, Apartment.class).collectList();

        return findByIdResult.zipWith(findByNameResult, ((apartment, apartments) -> {
                    if (apartments != null && apartments.size() > 0) {
                        for (Apartment apart : apartments) {
                            if (apart.getId() != id && apart.getName().equals(name))
                                throw new CustomAlreadyExistsException("Apartment %s already exists!".formatted(name));
                        }
                    }
                    apartment.setName(dto.getName());
                    return apartment;
                }))
                .flatMap(template::save);
    }

    @Override
    public Flux<Apartment> findPaginated(Optional<String> optionalId, Optional<String> optionalApartmentName, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        optionalId.ifPresent(s -> query.addCriteria(Criteria.where("id").is(s)));
        optionalApartmentName.ifPresent(s -> query.addCriteria(Criteria.where("name").is(s)));
        query.with(pageable)
                .with(sort);
        return template.find(query, Apartment.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartments do not exist!")));
    }

    @Override
    public Mono<String> delete(String id) {
        return template
                .findById(id, Apartment.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartment with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .flatMap(result -> Mono.just("Deleted Successfully"));
    }
}
