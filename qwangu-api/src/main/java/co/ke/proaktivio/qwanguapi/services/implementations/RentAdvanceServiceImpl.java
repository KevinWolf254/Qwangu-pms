package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.RentAdvance;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.RentAdvanceDto;
import co.ke.proaktivio.qwanguapi.pojos.UpdateRentAdvanceDto;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.RentAdvanceRepository;
import co.ke.proaktivio.qwanguapi.services.RentAdvanceService;
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
public class RentAdvanceServiceImpl implements RentAdvanceService {
    private final RentAdvanceRepository rentAdvanceRepository;
    private final PaymentRepository paymentRepository;
    private final OccupationRepository occupationRepository;
    private final ReactiveMongoTemplate template;

    @Override
    public Mono<RentAdvance> create(RentAdvanceDto dto) {
        String occupationId = dto.getOccupationId();
        String paymentId = dto.getPaymentId();
        return Mono
                .just(dto)
                .filter(d -> d.getStatus().equals(RentAdvance.Status.HOLDING))
                .switchIfEmpty(Mono
                        .error(new CustomBadRequestException("Status should be HOLDING on creation!")))
                .flatMap(d -> paymentRepository.findById(d.getPaymentId()))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Payment with id %s does not exist!"
                        .formatted(paymentId))))
                .filter(payment -> payment.getStatus().equals(Payment.Status.NEW))
                .switchIfEmpty(Mono
                        .error(new CustomBadRequestException("Payment with id %s has already been processed!"
                                .formatted(paymentId))))
                .flatMap(payment -> {
                    payment.setStatus(Payment.Status.PROCESSED);
                    if (occupationId != null && !occupationId.trim().isEmpty() && !occupationId.trim().isBlank()) {
                        return occupationRepository.findById(occupationId)
                                .switchIfEmpty(Mono.error(
                                        new CustomNotFoundException("Occupation with id %s does not exist!"
                                        .formatted(occupationId))))
                                .then(paymentRepository.save(payment));
                    }
                    return paymentRepository.save(payment);
                })
                .map(payment ->
                        new RentAdvance.RentAdvanceBuilder()
                                .status(dto.getStatus())
                                .occupationId(occupationId)
                                .paymentId(payment.getId())
                                .build()
                )
                .flatMap(rentAdvanceRepository::save);
    }

    @Override
    public Mono<RentAdvance> update(String id, UpdateRentAdvanceDto dto) {
        return rentAdvanceRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("Rent advance with id %s does not exist!"
                        .formatted(id))))
                .filter(rentAdvance -> rentAdvance.getStatus().equals(RentAdvance.Status.HOLDING))
                .switchIfEmpty(Mono
                        .error(new CustomBadRequestException("Status should be HOLDING on creation!")))
                .map(rentAdvance -> {
                    rentAdvance.setStatus(dto.getStatus());
                    if ((dto.getStatus().equals(RentAdvance.Status.RELEASED))) {
                        if (dto.getReturnDetails() == null || dto.getReturnDetails().trim().isEmpty() ||
                                dto.getReturnDetails().trim().isBlank())
                            throw new CustomBadRequestException("Return details are required!");
                        if (dto.getReturnedOn() == null)
                            throw new CustomBadRequestException("Return on date is required!");
                        rentAdvance.setReturnDetails(dto.getReturnDetails());
                        rentAdvance.setReturnedOn(dto.getReturnedOn());
                    }
                    return rentAdvance;
                })
                .flatMap(rentAdvanceRepository::save);
    }

    @Override
    public Flux<RentAdvance> findPaginated(Optional<String> id, Optional<RentAdvance.Status> status,
                                           Optional<String> occupationId, Optional<String> paymentId, int page,
                                           int pageSize, OrderType order) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Sort sort = order.equals(OrderType.ASC) ?
                Sort.by(Sort.Order.asc("id")) :
                Sort.by(Sort.Order.desc("id"));
        Query query = new Query();
        id.ifPresent(i -> query.addCriteria(Criteria.where("id").is(i)));
        status.ifPresent(state -> query.addCriteria(Criteria.where("status").is(state)));
        occupationId.ifPresent(uId -> query.addCriteria(Criteria.where("occupationId").is(uId)));
        paymentId.ifPresent(tId -> query.addCriteria(Criteria.where("paymentId").is(tId)));

        query.with(pageable)
                .with(sort);
        return template
                .find(query, RentAdvance.class)
                .switchIfEmpty(Flux.error(new CustomNotFoundException("RentAdvance were not found!")));
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return template
                .findById(id, RentAdvance.class)
                .switchIfEmpty(Mono.error(new CustomNotFoundException("RentAdvance with id %s does not exist!".formatted(id))))
                .flatMap(template::remove)
                .map(DeleteResult::wasAcknowledged);
    }
}
