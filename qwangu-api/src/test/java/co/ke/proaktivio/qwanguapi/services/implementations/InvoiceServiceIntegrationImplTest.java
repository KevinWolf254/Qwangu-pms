package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Invoice.Type;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Occupation.Status;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.models.Unit.Identifier;
import co.ke.proaktivio.qwanguapi.models.Unit.UnitType;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class InvoiceServiceIntegrationImplTest {
	@Autowired
	private InvoiceService invoiceService;
	@Autowired
	private InvoiceRepository invoiceRepository;
	@Autowired
	private OccupationRepository occupationRepository;
	@Autowired
	private OccupationTransactionRepository occupationTransactionRepository;
	@Autowired
	private UnitRepository unitRepository;
	@SuppressWarnings("unused")
	@Autowired
	private OccupationTransactionService occupationTransactionService;
	@Autowired
	private ReactiveMongoTemplate template;

	@MockBean
	private BootstrapConfig bootstrapConfig;
	@MockBean
	private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

	@Container
	private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(
			DockerImageName.parse("mongo:latest"));

	@DynamicPropertySource
	public static void overrideProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
	}

	InvoiceDto getInvoiceWithNoOccupationId() {
		var dto = new InvoiceDto();
		dto.setType(Type.RENT);
		dto.setCurrency(Currency.KES);
		LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
		dto.setStartDate(firstDay);
		dto.setEndDate(firstDay.with(lastDayOfMonth()));
		return dto;
	}

	Occupation getOccupationWithNoId() {
		var occupation = new Occupation.OccupationBuilder().status(Status.CURRENT).startDate(LocalDate.now())
				.tenantId("1").unitId("1").build();
		return occupation;
	}

	@NotNull
	private Mono<Void> reset() {
		return invoiceRepository.deleteAll()
				.doOnSuccess(t -> System.out.println("---- Deleted all Invoices!"))
				.then(occupationRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
				.then(occupationTransactionRepository.deleteAll())
				.doOnSuccess(t -> System.out.println("---- Deleted all OccupationTransactions!"));
	}

	@Test
	void create_returnsCustomBadRequestException_whenOccupationIdDoesNotExist() {
		// given
		var dto = getInvoiceWithNoOccupationId();
		dto.setOccupationId("123456");
		// when
		Mono<Invoice> createWithNonExistantOccupationId = reset().then(invoiceService.create(dto));
		// then
		StepVerifier.create(createWithNonExistantOccupationId)
				.expectErrorMatches(e -> e instanceof CustomBadRequestException && e.getMessage()
						.equals("Occupation with id %s does not exist!".formatted(dto.getOccupationId())))
				.verify();
	}

	@Test
	void create_returnsInvoiceSuccessfully_forRentAdvance() {
		// given
		var occupation = getOccupationWithNoId();
		occupation.setId("1");
		var dto = getInvoiceWithNoOccupationId();
		dto.setType(Type.RENT_ADVANCE);
		dto.setOccupationId(occupation.getId());
		var unit = new Unit();
		unit.setId("1");
		unit.setStatus(Unit.Status.OCCUPIED);
		unit.setNumber("12345");
		unit.setType(UnitType.APARTMENT_UNIT);
		unit.setIdentifier(Identifier.A);
		unit.setNoOfBedrooms(2);
		unit.setNoOfBathrooms(1);
		unit.setCurrency(Currency.KES);
		unit.setRentPerMonth(BigDecimal.valueOf(25000l));
		unit.setSecurityPerMonth(BigDecimal.valueOf(500l));
		unit.setGarbagePerMonth(BigDecimal.valueOf(500l));
		unit.setAdvanceInMonths(2);
		unit.setGarbageAdvance(BigDecimal.valueOf(1000l));
		unit.setSecurityAdvance(BigDecimal.valueOf(1000l));
		Map<String, BigDecimal> otherAmountsAdvance = new HashMap<>();
		otherAmountsAdvance.put("GYM", BigDecimal.valueOf(2500l));
		unit.setOtherAmountsAdvance(otherAmountsAdvance);
		unit.setPropertyId("1");

		// when
		Mono<Invoice> createInvoice = reset().then(unitRepository.save(unit))
				.doOnSuccess(t -> System.out.println("---- Created: " + t)).then(occupationRepository.save(occupation))
				.doOnSuccess(t -> System.out.println("---- Created: " + t)).then(invoiceService.create(dto));

		// then
		StepVerifier.create(createInvoice)
				.expectNextMatches(i -> !i.getId().isEmpty() && !i.getNumber().isEmpty()
						&& i.getType().equals(Type.RENT_ADVANCE) && i.getCurrency().equals(Currency.KES)
						&& i.getRentAmount().intValue() == 50000 && i.getGarbageAmount().intValue() == 1000
						&& i.getSecurityAmount().intValue() == 1000
						&& i.getOtherAmounts().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
								.equals(BigDecimal.valueOf(2500l))
						&& i.getOccupationId().endsWith(occupation.getId()) && i.getCreatedOn() != null
						&& i.getCreatedBy().equals("SYSTEM") && i.getModifiedOn() != null
						&& i.getModifiedBy().equals("SYSTEM"))
				.verifyComplete();
	}

	@Test
	void create_savesOccupationTransactionSuccessfully_forRentAdvance() {
		// given
		create_returnsInvoiceSuccessfully_forRentAdvance();
		// when
		Flux<OccupationTransaction> findOccupationTransactions = occupationTransactionRepository.findAll();
		// then
		var type = co.ke.proaktivio.qwanguapi.models.OccupationTransaction.Type.DEBIT;
		StepVerifier.create(findOccupationTransactions)
				.expectNextMatches(ot -> ot.getType().equals(type) && ot.getOccupationId() != null
						&& ot.getInvoiceId() != null && ot.getTotalAmountOwed().equals(BigDecimal.valueOf(54500l))
						&& ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(54500l))
						&& ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.verifyComplete();
	}

	@Test
	void create_returnsInvoiceSuccessfully_forRent() {
		// given
		var occupation = getOccupationWithNoId();
		occupation.setId("1");
		var dto = getInvoiceWithNoOccupationId();
		dto.setOccupationId(occupation.getId());
		var unit = new Unit();
		unit.setId("1");
		unit.setStatus(Unit.Status.OCCUPIED);
		unit.setNumber("12345");
		unit.setType(UnitType.APARTMENT_UNIT);
		unit.setIdentifier(Identifier.A);
		unit.setNoOfBedrooms(2);
		unit.setNoOfBathrooms(1);
		unit.setCurrency(Currency.KES);
		unit.setRentPerMonth(BigDecimal.valueOf(25000l));
		unit.setSecurityPerMonth(BigDecimal.valueOf(500l));
		unit.setGarbagePerMonth(BigDecimal.valueOf(500l));
		unit.setPropertyId("1");

		// when
		create_returnsInvoiceSuccessfully_forRentAdvance();
		Mono<Invoice> createInvoice = invoiceService.create(dto);

		// then
		StepVerifier.create(createInvoice)
			.expectNextMatches(i -> !i.getId().isEmpty() && !i.getNumber().isEmpty()
				&& i.getType().equals(Type.RENT) && i.getCurrency().equals(Currency.KES)
				&& i.getRentAmount().intValue() == 25000 && i.getGarbageAmount().intValue() == 500
				&& i.getSecurityAmount().intValue() == 500 && i.getOtherAmounts() == null
				&& i.getOccupationId().equals(occupation.getId()) && i.getCreatedOn() != null
				&& i.getCreatedBy().equals("SYSTEM") && i.getModifiedOn() != null && i.getModifiedBy().equals("SYSTEM"))
				.verifyComplete();
	}

	@Test
	void create_savesOccupationTransactionSuccessfully_forRent() {
		// given
		create_returnsInvoiceSuccessfully_forRent();
		// when
		Flux<OccupationTransaction> findOccupationTransactions = template
			.find(new Query()
					.addCriteria(Criteria.where("occupationId").is("1"))
					.with(Sort.by(Sort.Direction.DESC, "id")), OccupationTransaction.class)
					.doOnNext(a -> System.out.println("---- Found " + a));
		// then
		var type = co.ke.proaktivio.qwanguapi.models.OccupationTransaction.Type.DEBIT;
		StepVerifier.create(findOccupationTransactions)
				.expectNextMatches(ot -> 
								ot.getType().equals(type) && 
								ot.getOccupationId() != null && 
								ot.getInvoiceId() != null && 
								ot.getTotalAmountOwed().equals(BigDecimal.valueOf(26000l)) && 
								ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(80500l)) && 
								ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.expectNextMatches(ot -> 
								ot.getType().equals(type) && 
								ot.getOccupationId() != null && 
								ot.getInvoiceId() != null && 
								ot.getTotalAmountOwed().equals(BigDecimal.valueOf(54500l)) && 
								ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(54500l)) && 
								ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.verifyComplete();
	}

	@Test
	void create_returnsInvoiceSuccessfully_forPenalty() {
		// given
		var dto = getInvoiceWithNoOccupationId();
		dto.setType(Type.PENALTY);
		dto.setOccupationId("1");
		// when
		create_returnsInvoiceSuccessfully_forRent();
		Flux<OccupationTransaction> createPenalty = invoiceService.create(dto)
				.thenMany(template
				.find(new Query()
						.addCriteria(Criteria.where("occupationId").is("1"))
						.with(Sort.by(Sort.Direction.DESC, "id")), OccupationTransaction.class))
				.doOnNext(a -> System.out.println("---- Found " + a));
		// then
		var type = co.ke.proaktivio.qwanguapi.models.OccupationTransaction.Type.DEBIT;
		StepVerifier.create(createPenalty)
				.expectNextMatches(ot -> 
								ot.getType().equals(type) && 
								ot.getOccupationId() != null && 
								ot.getInvoiceId() != null && 
								ot.getTotalAmountOwed().equals(BigDecimal.valueOf(5000l)) && 
								ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(85500l)) && 
								ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.expectNextMatches(ot -> 
								ot.getType().equals(type) && 
								ot.getOccupationId() != null && 
								ot.getInvoiceId() != null && 
								ot.getTotalAmountOwed().equals(BigDecimal.valueOf(26000l)) && 
								ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(80500l)) && 
								ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.expectNextMatches(ot -> 
								ot.getType().equals(type) && 
								ot.getOccupationId() != null && 
								ot.getInvoiceId() != null && 
								ot.getTotalAmountOwed().equals(BigDecimal.valueOf(54500l)) && 
								ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(54500l)) && 
								ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.verifyComplete();
	}


	@SuppressWarnings("serial")
	@Test
	void create_returnsInvoiceSuccessfully_forUtilities() {
		// given
		var dto = getInvoiceWithNoOccupationId();
		dto.setType(Type.UTILITIES);
		dto.setOtherAmounts(new HashMap<>() {{
			put("WATER", BigDecimal.valueOf(1500l));
			put("GYM", BigDecimal.valueOf(2000l));
		}});
		dto.setOccupationId("1");
		// when
		create_returnsInvoiceSuccessfully_forPenalty();
		Flux<OccupationTransaction> createPenalty = invoiceService.create(dto)
				.thenMany(template
				.find(new Query()
						.addCriteria(Criteria.where("occupationId").is("1"))
						.with(Sort.by(Sort.Direction.DESC, "id")), OccupationTransaction.class))
				.doOnNext(a -> System.out.println("---- Found " + a));
		// then
		var type = co.ke.proaktivio.qwanguapi.models.OccupationTransaction.Type.DEBIT;
		StepVerifier.create(createPenalty)
				.expectNextMatches(ot -> 
								ot.getType().equals(type) && 
								ot.getOccupationId().equals("1") && 
								ot.getInvoiceId() != null && 
								ot.getTotalAmountOwed().equals(BigDecimal.valueOf(3500l)) && 
								ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(89000l)) && 
								ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.expectNextMatches(ot -> 
								ot.getType().equals(type) && 
								ot.getOccupationId().equals("1") && 
								ot.getInvoiceId() != null && 
								ot.getTotalAmountOwed().equals(BigDecimal.valueOf(5000l)) && 
								ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(85500l)) && 
								ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.expectNextMatches(ot -> 
								ot.getType().equals(type) && 
								ot.getOccupationId().equals("1") && 
								ot.getInvoiceId() != null && 
								ot.getTotalAmountOwed().equals(BigDecimal.valueOf(26000l)) && 
								ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(80500l)) && 
								ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.expectNextMatches(ot -> 
								ot.getType().equals(type) && 
								ot.getOccupationId().equals("1") && 
								ot.getInvoiceId() != null && 
								ot.getTotalAmountOwed().equals(BigDecimal.valueOf(54500l)) && 
								ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(54500l)) && 
								ot.getTotalAmountPaid().equals(BigDecimal.ZERO))
				.verifyComplete();
	}

	@Test
	void findById_returnsEmpty_whenIdDoesNotExist() {
		// given
		var invoiceId = "2000";
		// when
		Mono<Invoice> findByIdDoesNotExist = reset().then(invoiceService.findById(invoiceId));
		// then
		StepVerifier.create(findByIdDoesNotExist).expectComplete().verify();

	}

	@Test
	void find_returnsEmpty_whenNonExist() {
		// when
		Flux<Invoice> findNonExist = reset().thenMany(invoiceService.find(null, null, null, null));
		// then
		StepVerifier.create(findNonExist).expectComplete().verify();
		
	}

	@SuppressWarnings("serial")
	@Test
	void find_returnsSuccessfuly_whenInvoiceExists() {
		// given
		var invoiceId = "1";
		Type type = Invoice.Type.RENT;
		var invoiceNo = "12345";
		var occupationId = "1";
		var order = OrderType.DESC;
		var invoice = new Invoice.InvoiceBuilder()
				.type(type)
				.startDate(LocalDate.now())
				.currency(Currency.KES)
				.rentAmount(BigDecimal.valueOf(25000l))
				.securityAmount(BigDecimal.valueOf(1000l))
				.garbageAmount(BigDecimal.valueOf(1000l))
				.otherAmounts(new HashMap<>() {{put("GYM", BigDecimal.valueOf(1500l));}})
				.occupationId(occupationId)
				.build();
		invoice.setId(invoiceId);
		invoice.setNumber(invoiceNo);
		// when
		Flux<Invoice> findInvoice = 
				reset()
				.then(invoiceRepository.save(invoice))
				.thenMany(invoiceService.find(type, invoiceNo, occupationId, order))
				.doOnNext(a -> System.out.println("---- Found " + a));
		// then
		StepVerifier
			.create(findInvoice)
			.expectNextMatches(i -> 
					i.getId().equals(invoiceId) && 
					i.getNumber().equals(invoiceNo) && 
					i.getType().equals(Type.RENT) && 
					i.getCurrency().equals(Currency.KES) && 
					i.getRentAmount().intValue() == 25000 && 
					i.getGarbageAmount().intValue() == 1000 && 
					i.getSecurityAmount().intValue() == 1000 && 
					i.getOtherAmounts() != null && 
					i.getOccupationId().equals(occupationId))
			.verifyComplete();			
	}
	

	@Test
	void deleteById_returnsCustomNotFoundException_whenUnitDoesNotExist() {
		// given
		var invoiceId = "3090";
		// when
		Mono<Boolean> deleteInvoiceThatDoesNotExist = reset().then(invoiceService.deleteById(invoiceId));
		// then
		StepVerifier.create(deleteInvoiceThatDoesNotExist).expectErrorMatches(e -> e instanceof CustomNotFoundException
				&& e.getMessage().equals("Invoice with id %s does not exist!".formatted(invoiceId))).verify();
	}
    
	@Test
	void deleteById() {
		// given
		var invoiceId = "1";
		// when
		find_returnsSuccessfuly_whenInvoiceExists();
		Mono<Boolean> deleteById = invoiceService.deleteById(invoiceId);
		// then
		StepVerifier
			.create(deleteById)
            .expectNext(true)
            .verifyComplete();
	}
}