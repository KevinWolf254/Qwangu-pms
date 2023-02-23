package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.properties.RentPropertiesConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Invoice.Type;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.DebitTransactionDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import com.mongodb.client.result.DeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
	private final InvoiceRepository invoiceRepository;
	private final OccupationRepository occupationRepository;
	private final OccupationTransactionService occupationTransactionService;
	private final ReactiveMongoTemplate template;
	private final RentPropertiesConfig rentPropertiesConfig;
	
	@Override
//    @Transactional
	public Mono<Invoice> create(InvoiceDto dto) {
		Type type = dto.getType();
		var occupationId = dto.getOccupationId();
			return occupationRepository.findById(occupationId)
					.switchIfEmpty(
							Mono.error(
									new CustomBadRequestException(
											"Occupation with id %s does not exist!".formatted(occupationId))))
					.flatMap(occupation -> template
							.findOne(new Query().addCriteria(Criteria.where("occupationId").is(occupation.getId()))
									.with(Sort.by(Sort.Direction.DESC, "id")), Invoice.class)
							.switchIfEmpty(Mono.just(new Invoice()))
							.flatMap(previousInvoice -> {
								var unitId = occupation.getUnitId();
								return template
										.findOne(new Query().addCriteria(Criteria.where("id").is(unitId)), Unit.class)
										.map(unit -> {
											Currency currency = unit.getCurrency();
											var invoice = new Invoice();
											invoice.setType(type);
											invoice.setCurrency(currency);
											invoice.setNumber(invoice.generateInvoiceNumber.apply(previousInvoice, occupation));
											invoice.setOccupationId(occupationId);
											if (type.equals(Type.RENT_ADVANCE)) {
												BigDecimal totalRentAdvance = unit.getRentPerMonth().multiply(BigDecimal.valueOf(unit.getAdvanceInMonths()));
												BigDecimal securityAdvance = unit.getSecurityAdvance();
												BigDecimal garbageAdvance = unit.getGarbageAdvance();
												Map<String, BigDecimal> otherAmountsAdvance = unit.getOtherAmountsAdvance();
												invoice.setRentAmount(totalRentAdvance);
												invoice.setSecurityAmount(securityAdvance);
												invoice.setGarbageAmount(garbageAdvance);
												invoice.setOtherAmounts(otherAmountsAdvance);
											} else if (type.equals(Type.RENT)) {
												LocalDate startDate = dto.getStartDate();
												LocalDate endDate = dto.getEndDate();
												
												long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
												int totalDaysOfMonth = startDate.lengthOfMonth();
												BigDecimal rentToPay = unit.getRentPerMonth().multiply(BigDecimal.valueOf((int)totalDays / totalDaysOfMonth));
												
												invoice.setRentAmount(rentToPay);
												invoice.setStartDate(startDate);
												invoice.setEndDate(endDate);
												invoice.setSecurityAmount(unit.getSecurityPerMonth());
												invoice.setGarbageAmount(unit.getGarbagePerMonth());
												invoice.setOtherAmounts(unit.getOtherAmountsPerMonth());												
											} else if (type.equals(Type.PENALTY)) {
												Map<String, BigDecimal> penalty = new HashMap<>();
												penalty.put("PENALTY", unit.getRentPerMonth()
														.multiply(BigDecimal.valueOf(rentPropertiesConfig.getPenaltyPercentageOfRent() / 100)));
												invoice.setOtherAmounts(penalty);
											} else {
												invoice.setOtherAmounts(dto.getOtherAmounts());
											}
											return invoice;
										})
										.flatMap(invoiceRepository::save);
							}))
					.flatMap(invoice -> occupationTransactionService
							.createDebitTransaction(new DebitTransactionDto(occupationId, invoice.getId()))
							.then(Mono.just(invoice)))
                    .doOnSuccess(result -> log.info("Successfully created: {}", result));
	}

//    public Mono<Invoice> create(InvoiceDto dto) {
//        String occupationId = dto.getOccupationId();
//        // TODO INCLUDE BEGINNING_OF_MONTH OR MIDDLE_OF_MONTH IN INVOICE
//        return occupationRepository.findById(occupationId)
//                .switchIfEmpty(Mono.error(new CustomBadRequestException("Occupation with id %s does not exist!"
//                        .formatted(occupationId))))
//                .flatMap(occupation -> template
//                        .findOne(new Query()
//                                .addCriteria(Criteria.where("occupationId").is(occupation.getId()))
//                                .with(Sort.by(Sort.Direction.DESC, "id")), Invoice.class)
//                        .switchIfEmpty(Mono.just(new Invoice()))
//                        .map(previousInvoice -> new Invoice.InvoiceBuilder()
//                                .number(previousInvoice, occupation)
//                                .type(dto.getType())
//                                .startDate(occupation.getStartDate())
//                                .endDate(!dto.getType().equals(Invoice.Type.RENT_ADVANCE) && dto.getEndDate() == null ?
//                                        occupation.getStartDate().withDayOfMonth(occupation.getStartDate()
//                                                .getMonth().length(occupation.getStartDate().isLeapYear())) :
//                                        dto.getEndDate())
//                                .currency(dto.getCurrency())
//                                .rentAmount(dto.getRentAmount())
//                                .securityAmount(dto.getSecurityAmount())
//                                .garbageAmount(dto.getGarbageAmount())
//                                .otherAmounts(dto.getOtherAmounts() != null ?
//                                        dto.getOtherAmounts() :
//                                        null)
//                                .occupationId(occupationId)
//                                .build()))
//                .flatMap(invoiceRepository::save)
//                .flatMap(invoice -> occupationTransactionService
//                        .createDebitTransaction(new DebitTransactionDto(occupationId, invoice.getId()))
//                        .then(Mono.just(invoice))
//                );
//    }

	@Override
	public Mono<Invoice> findById(String id) {
		return invoiceRepository.findById(id).switchIfEmpty(
				Mono.error(new CustomNotFoundException("Invoice with id %s does not exist!".formatted(id))));
	}

//    @Override
//    public Mono<Invoice> update(String id, InvoiceDto dto) {
//        return findById(id)
//                .map(receivable -> {
//                    receivable.setType(dto.getType());
//                    receivable.setStartDate(dto.getStartDate());
//                    receivable.setRentAmount(dto.getRentAmount());
//                    receivable.setSecurityAmount(dto.getSecurityAmount());
//                    receivable.setGarbageAmount(dto.getGarbageAmount());
//                    receivable.setOtherAmounts(dto.getOtherAmounts());
//                    return receivable;
//                })
//                .flatMap(invoiceRepository::save);
//    }

	@Override
	public Flux<Invoice> findPaginated(Optional<Invoice.Type> type, Optional<String> invoiceNo,
			Optional<String> occupationId, OrderType order) {
		Sort sort = order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) : Sort.by(Sort.Order.desc("id"));
		Query query = new Query();
		type.ifPresent(s -> query.addCriteria(Criteria.where("type").is(s)));
		invoiceNo.ifPresent(number -> {
			if (StringUtils.hasText(number))
				query.addCriteria(Criteria.where("number").regex(".*" + number.trim() + ".*", "i"));
		});
		occupationId.ifPresent(i -> {
			if (StringUtils.hasText(i))
				query.addCriteria(Criteria.where("occupationId").is(i));
		});
		query.with(sort);
		return template.find(query, Invoice.class)
				.switchIfEmpty(Flux.error(new CustomNotFoundException("Invoices were not found!")));
	}

	@Override
	public Mono<Boolean> deleteById(String id) {
		return template.findById(id, Invoice.class)
				.switchIfEmpty(
						Mono.error(new CustomNotFoundException("Receivable with id %s does not exist!".formatted(id))))
				.flatMap(template::remove)
				.map(DeleteResult::wasAcknowledged);
	}
}
