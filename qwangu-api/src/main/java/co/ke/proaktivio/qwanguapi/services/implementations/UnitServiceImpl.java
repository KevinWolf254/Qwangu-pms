package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import co.ke.proaktivio.qwanguapi.repositories.ApartmentRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.UnitService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
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
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {
    private final ApartmentRepository apartmentRepository;
    private final UnitRepository unitRepository;
    private final ReactiveMongoTemplate template;
    private final OccupationRepository occupationRepository;

    @Override
    public Mono<Unit> create(UnitDto dto) {
        if (dto.getType().equals(Unit.Type.APARTMENT_UNIT))
            return createApartmentUnit(dto);
        return createNonApartmentUnit(dto);
    }

    private Mono<Unit> createNonApartmentUnit(UnitDto dto) {
        var accountNo = RandomStringUtils.randomAlphanumeric(4);
        return Mono.just(dto)
                .map(d -> new Unit.UnitBuilder()
                        .status(Unit.Status.VACANT)
                        .booked(false)
                        .accountNo(accountNo)
                        .type(d.getType())
                        .noOfBedrooms(d.getNoOfBedrooms())
                        .noOfBathrooms(d.getNoOfBathrooms())
                        .advanceInMonths(d.getAdvanceInMonths())
                        .currency(d.getCurrency())
                        .rentPerMonth(d.getRentPerMonth())
                        .securityPerMonth(d.getSecurityPerMonth())
                        .garbagePerMonth(d.getGarbagePerMonth()).build())
                .flatMap(unitRepository::save);
    }

    private Mono<Unit> createApartmentUnit(UnitDto dto) {
        String apartmentId = dto.getApartmentId();
        Integer floorNo = dto.getFloorNo();
        Unit.Identifier identifier = dto.getIdentifier();
        var accountNo = RandomStringUtils.randomAlphanumeric(4);
        Query query = new Query()
                .addCriteria(Criteria
                        .where("apartmentId").is(apartmentId)
                        .and("floorNo").is(floorNo)
                        .and("identifier").is(identifier));

        return template
                .exists(query, Unit.class)
                .filter(exits -> !exits)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Unit already exists!")))
                .flatMap(r -> apartmentRepository.findById(apartmentId))
                .filter(Objects::nonNull)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartment with id %s does not exist!".formatted(apartmentId))))
                .map(apartment -> new Unit.UnitBuilder()
                        .status(Unit.Status.VACANT)
                        .booked(false)
                        .accountNo(accountNo)
                        .type(dto.getType())
                        .identifier(dto.getIdentifier())
                        .floorNo(dto.getFloorNo())
                        .noOfBedrooms(dto.getNoOfBedrooms())
                        .noOfBathrooms(dto.getNoOfBathrooms())
                        .advanceInMonths(dto.getAdvanceInMonths())
                        .currency(dto.getCurrency())
                        .rentPerMonth(dto.getRentPerMonth())
                        .securityPerMonth(dto.getSecurityPerMonth())
                        .garbagePerMonth(dto.getGarbagePerMonth())
                        .apartmentId(apartment.getId()).build())
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
                    if (u.getType() != null && u.getType().equals(Unit.Type.APARTMENT_UNIT)) {
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
                    if (u.getType() != null && u.getType().equals(Unit.Type.APARTMENT_UNIT))
                        return Objects.equals(u.getApartmentId(), dto.getApartmentId());
                    return true;
                })
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Can not change the unit's Apartment!")))
                .map(u -> {
                    if (dto.getStatus() != null)
                        u.setStatus(dto.getStatus());
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
        return unitRepository.findById(unitId)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Unit with id %s does not exist!"
                        .formatted(unitId))));
    }

    @Override
    public Flux<Unit> findPaginated(Optional<Unit.Status> status, Optional<String> accountNo, Optional<Unit.Type> type,
                                    Optional<Unit.Identifier> identifier, Optional<Integer> floorNo,
                                    Optional<Integer> bedrooms, Optional<Integer> bathrooms, Optional<String> apartmentId, int page, int pageSize,
                                    OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        status.ifPresent(i -> query.addCriteria(Criteria.where("status").is(i)));
        accountNo.ifPresent(acct -> query.addCriteria(Criteria.where("accountNo").is(acct)));
        type.ifPresent(t -> query.addCriteria(Criteria.where("type").is(t)));
        identifier.ifPresent(t -> query.addCriteria(Criteria.where("identifier").is(t)));
        floorNo.ifPresent(t -> query.addCriteria(Criteria.where("floorNo").is(t)));
        bedrooms.ifPresent(t -> query.addCriteria(Criteria.where("noOfBedrooms").is(t)));
        bathrooms.ifPresent(t -> query.addCriteria(Criteria.where("noOfBathrooms").is(t)));
        apartmentId.ifPresent(t -> query.addCriteria(Criteria.where("apartmentId").is(t)));
        query.with(pageable)
                .with(sort);
        return template
                .find(query, Unit.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("Units were not found!")));
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
