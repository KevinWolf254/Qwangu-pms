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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

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
		                    .doOnSuccess(result -> log.info("Successfully found: {}", result))
							.flatMap(previousInvoice -> {
								var unitId = occupation.getUnitId();
								return template
										.findOne(new Query().addCriteria(Criteria.where("id").is(unitId)), Unit.class)
					                    .switchIfEmpty(Mono.error(new CustomBadRequestException("Unit with id %s does not exist!".formatted(unitId))))
					                    .doOnSuccess(result -> log.info("Successfully found: {}", result))
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
												
												long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
												int totalDaysOfMonth = startDate.lengthOfMonth();
												double proRate = (double)totalDays / (double)totalDaysOfMonth;
												BigDecimal rentToPay = unit.getRentPerMonth().multiply(BigDecimal.valueOf(proRate)).setScale(0, RoundingMode.CEILING);
												
												invoice.setRentAmount(rentToPay);
												invoice.setStartDate(startDate);
												invoice.setEndDate(endDate);
												invoice.setSecurityAmount(unit.getSecurityPerMonth());
												invoice.setGarbageAmount(unit.getGarbagePerMonth());
												invoice.setOtherAmounts(unit.getOtherAmountsPerMonth());												
											} else if (type.equals(Type.PENALTY)) {
												Map<String, BigDecimal> penalty = new HashMap<>();
												penalty.put("PENALTY", unit.getRentPerMonth()
														.multiply(BigDecimal.valueOf((double)rentPropertiesConfig.getPenaltyPercentageOfRent() / (double)100))
														.setScale(0, RoundingMode.CEILING));
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

	@Override
	public Mono<Invoice> findById(String id) {
		return invoiceRepository.findById(id);
	}


	@Override
	public Flux<Invoice> findAll(Invoice.Type type, String invoiceNo, String occupationId,
			OrderType order) {
		Sort sort = order != null
				? order.equals(OrderType.ASC) ? Sort.by(Sort.Order.asc("id")) : Sort.by(Sort.Order.desc("id"))
				: Sort.by(Sort.Order.desc("id"));
		Query query = new Query();
		if(type != null)
			query.addCriteria(Criteria.where("type").is(type));
		if (StringUtils.hasText(invoiceNo))
			query.addCriteria(Criteria.where("number").regex(".*" + invoiceNo.trim() + ".*", "i"));
		if (StringUtils.hasText(occupationId))
			query.addCriteria(Criteria.where("occupationId").is(occupationId.trim()));
	
		query.with(sort);
		return template.find(query, Invoice.class);
	}

	@Override
	public Mono<Boolean> deleteById(String id) {
		return template.findById(id, Invoice.class)
				.switchIfEmpty(
						Mono.error(new CustomNotFoundException("Invoice with id %s does not exist!".formatted(id))))
				.flatMap(template::remove)
				.map(DeleteResult::wasAcknowledged);
	}
}
