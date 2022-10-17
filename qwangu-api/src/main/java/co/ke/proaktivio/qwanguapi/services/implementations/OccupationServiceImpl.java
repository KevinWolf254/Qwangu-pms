package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.services.*;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OccupationServiceImpl implements OccupationService {
    private final OccupationRepository occupationRepository;
    private final UnitService unitService;
    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final ReceiptService receiptService;
    private final TenantService tenantService;
    private final NoticeService noticeService;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<Occupation> create(OccupationForNewTenantDto dto) {
        String unitId = dto.getUnitId();
        return unitService.findById(unitId)
                .then(tenantService.create(dto.getTenant()))
                .flatMap(tenant -> create(tenant.getId(), new OccupationDto(dto.getStartDate(), dto.getUnitId(),
                        dto.getPaymentId())));
    }

    private Mono<Boolean> checkOccupationExistsByUnitIdAndOccupationStatus(String unitId, Occupation.Status status) {
        Query occupationUnAvailable = new Query()
                .addCriteria(Criteria
                        .where("unitId").is(unitId)
                        .and("status").is(status));
        return template.exists(occupationUnAvailable, Occupation.class);
    }

    private Mono<Occupation> findByUnitIdAndOccupationStatus(String unitId, Occupation.Status status) {
        Query occupationUnAvailable = new Query()
                .addCriteria(Criteria
                        .where("unitId").is(unitId)
                        .and("status").is(status));
        return template.findOne(occupationUnAvailable, Occupation.class);
    }

    private Mono<Boolean> checkIfUnitIsAlreadyOccupied(String unitId) {
        return findByUnitIdAndOccupationStatus(unitId, Occupation.Status.CURRENT)
                .flatMap(occupation -> {
                    System.out.println("Occupation: " +occupation);
                    if (occupation != null) {
                        return noticeService.findByOccupationIdAndIsActive(occupation.getId(), true)
                                .flatMap(notice -> {
                                    if (notice != null) {
                                        return Mono.just(false);
                                    }
                                    return Mono.just(true);
                                });
                    }
                    return Mono.just(false);
                })
                .switchIfEmpty(Mono.just(false))
                .filter(exists -> !exists)
                .switchIfEmpty(Mono.error(new CustomAlreadyExistsException("Unit already occupied!")));
    }


    public Mono<Occupation> create(String tenantId, OccupationDto dto) {
        String unitId = dto.getUnitId();
        String paymentId = dto.getPaymentId();
//        LocalDate today = LocalDate.now();
        return unitService.findById(unitId)
                .flatMap(unit -> tenantService.findById(tenantId)
                        .flatMap(tenant -> checkIfUnitIsAlreadyOccupied(dto.getUnitId())
                                .then(Mono.just(
                                                new Occupation.OccupationBuilder()
//                                                        .status(today.isAfter(dto.getStartDate()) ||
//                                                                today.isEqual(dto.getStartDate()) ?
//                                                                Occupation.Status.CURRENT :
//                                                                Occupation.Status.BOOKED)
                                                        .startDate(dto.getStartDate())
                                                        .tenantId(tenant.getId())
                                                        .unitId(dto.getUnitId())
                                                        .build()
                                        )
                                )
                                .flatMap(o -> occupationRepository.save(o)
                                        // create an invoice for rent advance
                                        .flatMap(occupation -> invoiceService.create(new InvoiceDto(Invoice.Type.RENT_ADVANCE,
                                                        null, unit.getRentPerMonth(), unit.getSecurityPerMonth(),
                                                        unit.getGarbagePerMonth(), unit.getOtherAmounts() != null ? unit.getOtherAmounts() : null, o.getId()))
                                                .then(paymentService.findById(paymentId))
                                                .filter(payment -> payment.getStatus().equals(Payment.Status.NEW))
                                                .switchIfEmpty(Mono.error(new CustomBadRequestException("Payment has already been processed!")))
                                                .flatMap(payment -> receiptService.create(new ReceiptDto(occupation.getId(), paymentId)))
                                                // create an invoice for rent
                                                .then(invoiceService.create(new InvoiceDto(Invoice.Type.RENT,
                                                        dto.getStartDate(), unit.getRentPerMonth(), unit.getSecurityPerMonth(),
                                                        unit.getGarbagePerMonth(), unit.getOtherAmounts() != null ? unit.getOtherAmounts() : null, o.getId())))
                                                .then(Mono.just(occupation))
                                        )
                                )
                        ));
    }

    @Override
    public Mono<Occupation> update(String occupationId, VacateOccupationDto dto) {
        var today = LocalDate.now();
        return findById(occupationId)
                .filter(occupation -> occupation.getStatus().equals(Occupation.Status.CURRENT))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("Occupant already vacated!")))
                .filter($ -> dto.getEndDate().isEqual(today) || dto.getEndDate().isBefore(today))
                .switchIfEmpty(Mono.error(new CustomBadRequestException("End date should be today or before!")))
                .map(occupation -> {
                    occupation.setStatus(Occupation.Status.VACATED);
                    occupation.setEndDate(dto.getEndDate());
                    return occupation;
                })
                .flatMap(occupationRepository::save);
    }

    @Override
    public Mono<Occupation> findById(String id) {
        return occupationRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Occupation with id %s does not exist!".formatted(id))));
    }

    @Override
    public Mono<Occupation> findByUnitId(String unitId) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("unitId").is(unitId)), Occupation.class);
    }

    @Override
    public Mono<Occupation> findByOccupationNo(String occupationNo) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("occupationNo").is(occupationNo)), Occupation.class);
    }

    @Override
    public Mono<Occupation> findByUnitIdAndStatus(String unitId, Occupation.Status status) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("unitId").is(unitId)
                        .and("status").is(status))
                .with(Sort.by(Sort.Direction.DESC, "createdOn")), Occupation.class);
    }

    @Override
    public Flux<Occupation> findByStatus(List<Occupation.Status> statuses) {
        return template
                .find(new Query().addCriteria(Criteria
                        .where("status").in(statuses)), Occupation.class);
    }

    @Override
    public Mono<Occupation> findByUnitIdAndNotBooked(String unitId) {
        return template.findOne(new Query()
                .addCriteria(Criteria
                        .where("unitId").is(unitId)
                        .and("status").in(Occupation.Status.CURRENT, Occupation.Status.VACATED))
                .with(Sort.by(Sort.Direction.DESC, "createdOn")), Occupation.class);
    }

    @Override
    public Flux<Occupation> findPaginated(Optional<Occupation.Status> status, Optional<String> unitId,
                                          Optional<String> tenantId, int page, int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        status.ifPresent(state -> query.addCriteria(Criteria.where("status").is(state)));
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
