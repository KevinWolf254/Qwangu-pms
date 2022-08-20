package co.ke.proaktivio.qwanguapi.repositories.custom.impl;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.custom.CustomApartmentRepository;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomApartmentRepositoryImpl implements CustomApartmentRepository {
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Apartment> create(ApartmentDto dto) {
        String name = dto.getName();
        Query query = new Query()
                .addCriteria(Criteria.where("name").is(name));
        return template
                .exists(query, Apartment.class)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Apartment %s already exists!".formatted(name))))
                .map($ -> new Apartment(null, name, LocalDateTime.now(), null))
                .flatMap(template::save);
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
                            if (StringUtils.hasText(apart.getId()) &&  apart.getId() != id &&
                                    StringUtils.hasText(apart.getName()) && apart.getName().equalsIgnoreCase(name))
                                throw new CustomAlreadyExistsException("Apartment %s already exists!".formatted(name));
                        }
                    }
                    apartment.setName(dto.getName());
                    apartment.setModified(LocalDateTime.now());
                    return apartment;
                }))
                .flatMap(template::save);
    }

    @Override
    public Flux<Apartment> findPaginated(Optional<String> optionalId, Optional<String> optionalApartmentName, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        optionalId.ifPresent(s -> query.addCriteria(Criteria.where("id").is(s)));
        optionalApartmentName.ifPresent(s -> query.addCriteria(Criteria.where("name").is(s)));
        query.with(pageable)
                .with(sort);
        return template.find(query, Apartment.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Apartments were not found!")));
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return template
                .findById(id, Apartment.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartment with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
