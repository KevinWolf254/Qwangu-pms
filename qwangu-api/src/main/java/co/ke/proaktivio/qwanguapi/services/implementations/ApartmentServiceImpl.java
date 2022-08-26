package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.ApartmentRepository;
import co.ke.proaktivio.qwanguapi.services.ApartmentService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@Service
@RequiredArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {
    private final ReactiveMongoTemplate template;
    private final ApartmentRepository apartmentRepository;

    @Override
    public Mono<Apartment> create(ApartmentDto dto) {
        String name = dto.getName();
        Query query = new Query()
                .addCriteria(Criteria.where("name").is(name));
        return template
                .exists(query, Apartment.class)
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Apartment %s already exists!".formatted(name))))
                .doOnSuccess(a -> log.debug(" Checks for Apartment {} was successful", dto.getName()))
                .map($ -> new Apartment(name))
                .flatMap(template::save)
                .doOnSuccess(a -> log.debug(" Apartment with name {} created on database successfully", dto.getName()));
    }

    @Override
    public Mono<Apartment> update(String id, ApartmentDto dto) {
        String apartmentName = dto.getName();
        return apartmentRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartment with id %s does not exists!"
                        .formatted(id))))
                .flatMap(apartment -> exists(apartmentName)
                        .filter(exists -> !exists)
                        .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Apartment %s already exists!"
                                .formatted(apartmentName))))
                        .map($ -> {
                            apartment.setName(dto.getName());
                            apartment.setModifiedOn(LocalDateTime.now());
                            return apartment;
                        }))
                .doOnSuccess(a -> log.debug(" Checks for Apartment {} was successful", dto.getName()))
                .flatMap(apartmentRepository::save)
                .doOnSuccess(a -> log.debug(" Apartment with name {} update on database successfully", dto.getName()));
    }

    public Mono<Boolean> exists(String name) {
        return template.exists(new Query()
                .addCriteria(Criteria.where("name").is(name)), Apartment.class);
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
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Apartments were not found!")))
                .doOnComplete(() -> log.debug(" Apartments retrieved from database successfully"));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Apartment.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartment with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged)
                .doOnSuccess($ -> log.debug(" Apartment with id {} deleted from database successfully", id));
    }
}
