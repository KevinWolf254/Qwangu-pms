package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.CreateOccupationDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateOccupationDto;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class OccupationServiceImpl implements OccupationService {
    private final OccupationRepository occupationRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Occupation> create(CreateOccupationDto dto) {
        String unitId = dto.getUnitId();
        String tenantId = dto.getTenantId();
        Query query = new Query()
                .addCriteria(Criteria
                        .where("unitId").is(unitId)
                        .and("active").is(true));
        return template
                .findById(unitId, Unit.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Unit with id %s does not exist!".formatted(unitId))))
                .then(template.findById(tenantId, Tenant.class))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Tenant with id %s does not exist!".formatted(tenantId))))
                .then(template.exists(query, Occupation.class))
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Occupation already exists!")))
                .then(Mono.just(new Occupation(null, dto.getActive(), dto.getStarted(), dto.getEnded(), dto.getTenantId(),
                        dto.getUnitId(), LocalDateTime.now(), null)))
                .flatMap(occupationRepository::save);
    }

    @Override
    public Mono<Occupation> update(String id, UpdateOccupationDto dto) {
        if (dto.getActive()) {
            return occupationRepository
                    .findById(id)
                    .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(id))))
                    .flatMap(o -> Mono.just(o.getUnitId())
                            .map(unitId -> new Query()
                                    .addCriteria(Criteria
                                            .where("unitId").is(unitId)
                                            .and("active").is(true)
                                            .and("id").nin(id)))
                            .flatMap(q -> template
                                    .exists(q, Occupation.class)
                                    .filter(exists -> !exists)
                                    .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not activate while other occupation is active!")))
                                    .map(t -> o)))
                    .map(o -> {
                        if (dto.getActive() != null)
                            o.setActive(dto.getActive());
                        if ((dto.getStarted() != null))
                            o.setStarted(dto.getStarted());
                        if (dto.getEnded() != null)
                            o.setEnded(dto.getEnded());
                        o.setModified(LocalDateTime.now());
                        return o;
                    })
                    .flatMap(occupationRepository::save);
        }
        return occupationRepository
                .findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(id))))
                .map(o -> {
                    if (dto.getActive() != null)
                        o.setActive(dto.getActive());
                    if ((dto.getStarted() != null))
                        o.setStarted(dto.getStarted());
                    if (dto.getEnded() != null)
                        o.setEnded(dto.getEnded());
                    o.setModified(LocalDateTime.now());
                    return o;
                })
                .flatMap(occupationRepository::save);
    }

    @Override
    public Flux<Occupation> findPaginated(Optional<String> id, Optional<Boolean> active, Optional<String> unitId,
                                          Optional<String> tenantId, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        id.ifPresent(i -> query.addCriteria(Criteria.where("id").is(i)));
        active.ifPresent(a -> query.addCriteria(Criteria.where("active").is(a)));
        unitId.ifPresent(uId -> query.addCriteria(Criteria.where("unitId").is(uId)));
        tenantId.ifPresent(tId -> query.addCriteria(Criteria.where("tenantId").is(tId)));

        query.with(pageable)
                .with(sort);
        return template
                .find(query, Occupation.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Occupations were not found!")));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Occupation.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}