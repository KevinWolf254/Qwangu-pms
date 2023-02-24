package co.ke.proaktivio.qwanguapi.services.implementations;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Invoice.Type;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Occupation.Status;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.models.Unit.Identifier;
import co.ke.proaktivio.qwanguapi.models.Unit.UnitType;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
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
	private UnitRepository unitRepository;
	@SuppressWarnings("unused")
	@Autowired
    private OccupationTransactionService occupationTransactionService;

    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;
    
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

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
		var occupation = new Occupation.OccupationBuilder()
				.status(Status.CURRENT)
				.startDate(LocalDate.now())
				.tenantId("1")
				.unitId("1")
				.build();
		return occupation;
	}
	
    @NotNull
    private Mono<Void> reset() {
        return invoiceRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Invoices!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"));
    }
    
    @Test
    void create_returnsCustomBadRequestException_whenOccupationIdDoesNotExist() {
    	// given
    	var dto = getInvoiceWithNoOccupationId();
		dto.setOccupationId("123456");
		// when
    	Mono<Invoice> createWithNonExistantOccupationId = reset()
    			.then(invoiceService.create(dto));
    	// then
		StepVerifier
				.create(createWithNonExistantOccupationId)
				.expectErrorMatches(e -> e instanceof CustomBadRequestException && e.getMessage()
						.equals("Occupation with id %s does not exist!".formatted(dto.getOccupationId())))
				.verify();
    }
    
    @Test
    void create_returnsSuccess_forFirstMonthInvoice() {
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
    	Mono<Invoice> createInvoice = reset()
    			.then(unitRepository.save(unit))
    			.doOnSuccess(t -> System.out.println("---- Created: " +t))
    			.then(occupationRepository.save(occupation))
    			.doOnSuccess(t -> System.out.println("---- Created: " +t))
    			.then(invoiceService.create(dto));
    	
    	// then
    	StepVerifier
		    	.create(createInvoice)
		    	.expectNextMatches(i -> !i.getId().isEmpty() && !i.getNumber().isEmpty() &&
		    			i.getCurrency().equals(Currency.KES) &&
		    			i.getRentAmount().intValue() == 25000 && 
		    			i.getGarbageAmount().intValue() == 500 && 
		    			i.getSecurityAmount().intValue() == 500 && 
		    			i.getOtherAmounts() == null &&
		    			i.getOccupationId().endsWith(occupation.getId()) && 
		    			i.getCreatedOn() != null && 
		    			i.getCreatedBy().equals("SYSTEM") && 
		    			i.getModifiedOn() != null && 
		    			i.getModifiedBy().equals("SYSTEM"))
		    	.verifyComplete();
    }

    @Test
    void create_returnsSuccess_forOtherMonthsInvoice() {
    	
    }
    
    @Test
    void findById() {
    }

    @Test
    void find() {
    }

    @Test
    void deleteById() {
    }
}