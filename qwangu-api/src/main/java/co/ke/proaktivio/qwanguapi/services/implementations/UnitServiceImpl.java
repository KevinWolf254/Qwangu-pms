package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import co.ke.proaktivio.qwanguapi.repositories.PropertyRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.UnitService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {
    private final PropertyRepository propertyRepository;
    private final UnitRepository unitRepository;
    private final ReactiveMongoTemplate template;
    private final OccupationRepository occupationRepository;

    @Override
    public Mono<Unit> create(UnitDto dto) {
        String propertyId = dto.getPropertyId();
        Integer floorNo = dto.getFloorNo();
        Unit.Identifier identifier = dto.getIdentifier();
        var accountNo = RandomStringUtils.randomAlphanumeric(4);
        Query query = new Query()
                .addCriteria(Criteria
                        .where("propertyId").is(propertyId)
                        .and("floorNo").is(floorNo)
                        .and("identifier").is(identifier));

        Unit.UnitType unitType = dto.getType();
        return template
                .exists(query, Unit.class)
                .filter(exits -> !exits)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Unit already exists!")))
                .flatMap(r -> propertyRepository.findById(propertyId))
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Property with id %s does not exist!"
                        .formatted(propertyId))))
                .filter(property -> {
                    Property.PropertyType propertyType = property.getType();
                    return (propertyType.equals(Property.PropertyType.APARTMENT) && unitType.equals(Unit.UnitType.APARTMENT_UNIT)) ||
                            (propertyType.equals(Property.PropertyType.HOUSE) && (unitType.equals(Unit.UnitType.VILLA) ||
                                    unitType.equals(Unit.UnitType.MAISONETTES) || unitType.equals(Unit.UnitType.TOWN_HOUSE)));
                })
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Unit must be of the right type!")))
                .map(apartment -> new Unit.UnitBuilder()
                        .status(Unit.Status.VACANT)
                        .number(accountNo.toUpperCase())
                        .type(unitType)
                        .identifier(dto.getIdentifier())
                        .floorNo(dto.getFloorNo())
                        .noOfBedrooms(dto.getNoOfBedrooms())
                        .noOfBathrooms(dto.getNoOfBathrooms())
                        .advanceInMonths(dto.getAdvanceInMonths())
                        .currency(dto.getCurrency())
                        .rentPerMonth(dto.getRentPerMonth())
                        .securityPerMonth(dto.getSecurityPerMonth())
                        .garbagePerMonth(dto.getGarbagePerMonth())
                        .propertyId(apartment.getId())
                        .otherAmounts(dto.getOtherAmounts()).build())
                .flatMap(unitRepository::save);
    }

    @Override
    public Mono<Unit> update(String id, UnitDto dto) {
        return unitRepository.findById(id)
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Unit with id %s does not exist!".formatted(id))))
                .filter(u -> dto.getType() != null &&
                        u.getType() != null &&
                        u.getType() == dto.getType())
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not change the unit's type!")))
                .filter(u -> {
                    if (u.getType() != null && u.getType().equals(Unit.UnitType.APARTMENT_UNIT)) {
                        return (u.getIdentifier() != null &&
                                u.getFloorNo() != null &&
                                dto.getIdentifier() != null &&
                                dto.getFloorNo() != null &&
                                u.getIdentifier().equals(dto.getIdentifier()) &&
                                Objects.equals(u.getFloorNo(), dto.getFloorNo()));
                    }
                    return true;
                })
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not change the unit's floorNo and identifier!")))
                .filter(u -> {
                    if (u.getType() != null && u.getType().equals(Unit.UnitType.APARTMENT_UNIT))
                        return Objects.equals(u.getPropertyId(), dto.getPropertyId());
                    return true;
                })
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not change the unit's Property!")))
                .map(u -> {
                    if (dto.getStatus() != null)
                        u.setStatus(dto.getStatus());
                    if (dto.getIdentifier() != null)
                        u.setIdentifier(dto.getIdentifier());
                    if (dto.getNoOfBedrooms() != null)
                        u.setNoOfBedrooms(dto.getNoOfBedrooms());
                    if (dto.getNoOfBathrooms() != null)
                        u.setNoOfBedrooms(dto.getNoOfBedrooms());
                    if (dto.getAdvanceInMonths() != null)
                        u.setAdvanceInMonths(dto.getAdvanceInMonths());
                    if (dto.getCurrency() != null)
                        u.setCurrency(dto.getCurrency());
                    if (dto.getRentPerMonth() != null)
                        u.setRentPerMonth(dto.getRentPerMonth());
                    if (dto.getSecurityPerMonth() != null)
                        u.setSecurityPerMonth(dto.getSecurityPerMonth());
                    if (dto.getGarbagePerMonth() != null)
                        u.setGarbagePerMonth(dto.getGarbagePerMonth());
                    u.setModifiedOn(LocalDateTime.now());
                    return u;
                })
                .flatMap(unitRepository::save);
    }

    @Override
    public Mono<Unit> findById(String unitId) {
        return unitRepository.findById(unitId);
    }

    @Override
    public Flux<Unit> findAll(String propertyId, Unit.Status status, String accountNo,
                           Unit.UnitType type, Unit.Identifier identifier, Integer floorNo,
                           Integer bedrooms, Integer bathrooms, OrderType order) {
        Sort sort = order != null ? order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id")):
                    Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        if (StringUtils.hasText(propertyId))
            query.addCriteria(Criteria.where("propertyId").is(propertyId));
        if(status != null)
        	query.addCriteria(Criteria.where("status").is(status));
        if (StringUtils.hasText(accountNo))
            query.addCriteria(Criteria.where("number").regex(".*" + accountNo.trim() + ".*", "i"));
        if (type != null)
        	query.addCriteria(Criteria.where("type").is(type));
        if(identifier != null)
        	query.addCriteria(Criteria.where("identifier").is(identifier));
        if(floorNo != null)
        	query.addCriteria(Criteria.where("floorNo").is(floorNo));
        if (bedrooms != null)
        	query.addCriteria(Criteria.where("noOfBedrooms").is(bedrooms));
        if (bathrooms != null)
        	query.addCriteria(Criteria.where("noOfBathrooms").is(bathrooms));
        query.with(sort);
        return template
                .find(query, Unit.class);
    }

    @Override
    public Flux<Unit> findByOccupationIds(List<String> occupationIds) {
        return occupationRepository.findAllById(occupationIds)
                .filter(occupation -> occupation.getUnitId() != null && !occupation.getUnitId().isEmpty() &&
                        !occupation.getUnitId().isBlank())
                .flatMap(occupation -> unitRepository.findById(occupation.getUnitId()));
    }

    @Override
    public Mono<Unit> findByAccountNoAndIsBooked(String accountNo, Boolean isBooked) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("accountNo").is(accountNo)
                        .and("isBooked").is(isBooked)), Unit.class);
    }

    @Override
    public Mono<Unit> findByAccountNo(String accountNo) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("accountNo").is(accountNo)), Unit.class);
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, Unit.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Unit with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
