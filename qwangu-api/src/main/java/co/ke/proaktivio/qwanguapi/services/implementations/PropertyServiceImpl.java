package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.pojos.PropertyDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.PropertyRepository;
import co.ke.proaktivio.qwanguapi.services.PropertyService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Optional;

@Log4j2
@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {
    private final ReactiveMongoTemplate template;
    private final PropertyRepository propertyRepository;

    @Override
    public Mono<Property> create(PropertyDto dto) {
        String name = dto.getName();
        Query query = new Query()
                .addCriteria(Criteria.where("name").is(name));
        return template
                .exists(query, Property.class)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Property %s already exists!".formatted(name))))
                .doOnSuccess(a -> log.debug("Checks for property {} was successful", dto.getName()))
                .map($ -> new Property(name))
                .flatMap(template::save)
                .doOnSuccess(a -> log.info("Property created successfully: {}", a));
    }

    @Override
    public Mono<Property> update(String id, PropertyDto dto) {
        String apartmentName = dto.getName();
        return propertyRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Property with id %s does not exists!"
                        .formatted(id))))
                .flatMap(apartment -> exists(apartmentName)
                        .filter(exists -> !exists)
                        .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Property %s already exists!"
                                .formatted(apartmentName))))
                        .map($ -> {
                            apartment.setName(dto.getName());
                            apartment.setModifiedOn(LocalDateTime.now());
                            return apartment;
                        }))
                .doOnSuccess(a -> log.debug("Checks for Property {} was successful", dto.getName()))
                .flatMap(propertyRepository::save)
                .doOnSuccess(a -> log.info("Property updated successfully: {}", a));
    }

    @Override
    public Mono<Property> findById(String apartmentId) {
        Query query = new Query().addCriteria(Criteria.where("id").is(apartmentId));
        return template.findOne(query, Property.class);
    }

    public Mono<Boolean> exists(String name) {
        return template.exists(new Query()
                .addCriteria(Criteria.where("name").is(name)), Property.class);
    }

    @Override
    public Flux<Property> find(Optional<String> optionalApartmentName, OrderType order) {
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        optionalApartmentName.ifPresent(s -> {
            if(StringUtils.hasText(s))
                query.addCriteria(Criteria.where("name").regex(".*" +s.trim()+ ".*", "i"));
        });
        query.with(sort);
        return template
                .find(query, Property.class)
                .doOnComplete(() -> log.debug("Property retrieved from database successfully"));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Property.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Property with id %s does not exist!"
                        .formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged)
                .doOnSuccess($ -> log.info("Property with id {} deleted successfully.", id));
    }
}
