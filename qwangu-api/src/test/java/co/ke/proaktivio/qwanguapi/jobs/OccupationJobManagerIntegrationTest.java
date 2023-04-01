package co.ke.proaktivio.qwanguapi.jobs;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;

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
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.EmailNotification;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.NotificationStatus;
import co.ke.proaktivio.qwanguapi.repositories.EmailNotificationRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.TenantRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import co.ke.proaktivio.qwanguapi.services.OccupationEmailNotificationService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OccupationJobManagerIntegrationTest {
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UnitRepository unitRepository;
    @SuppressWarnings("unused")
	@Autowired
	private OccupationEmailNotificationService occupationEmailNotificationService;
    @Autowired
    private EmailNotificationRepository emailNotificationRepository;
    
    @Autowired
    private OccupationJobManager underTest;
    
    @MockBean
	private BootstrapConfig bootstrapConfig;
	@MockBean
	private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @NotNull
    private Mono<Void> reset() {
        return unitRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(tenantRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(emailNotificationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all EmailNotifications!"));
    }

	@SuppressWarnings("serial")
	@Test
	void processPendingOccupations_returnsOccupation_whenSuccessful() {
		// given
		var tenantId = "12345";
		var unitId = "123456";
		var occupation = new Occupation.OccupationBuilder()
				.startDate(LocalDate.now()).status(Occupation.Status.PENDING_OCCUPATION)
				.tenantId(tenantId).unitId(unitId).build();
		var unit = new Unit.UnitBuilder().status(Unit.Status.VACANT).number("12345")
				.type(Unit.UnitType.APARTMENT_UNIT).identifier(Unit.Identifier.A)
				.floorNo(2).currency(Currency.KES).rentPerMonth(BigDecimal.valueOf(25000l))
				.securityPerMonth(BigDecimal.valueOf(500l)).garbagePerMonth(BigDecimal.valueOf(550l))
				.otherAmounts(new HashMap<String, BigDecimal>(){{
					put("GYM", BigDecimal.valueOf(1000l));
				}}).advanceInMonths(1).securityAdvance(BigDecimal.valueOf(2000)).garbageAdvance(BigDecimal.valueOf(1500))
				.otherAmountsAdvance(new HashMap<String, BigDecimal>() {{
					put("GYM", BigDecimal.valueOf(1000l));
				}}).build();
		unit.setId(unitId);
		var tenant = new Tenant.TenantBuilder().firstName("John").middleName("Doe")
				.surname("Doe").mobileNumber("0720000000")
				.emailAddress("john.doe@someplace.com").build();
		tenant.setId(tenantId);
		
		// when
		Flux<Occupation> create = reset().then(tenantRepository.save(tenant))
				.doOnSuccess(o -> System.out.println("Created: " + o))
				.then(unitRepository.save(unit))
				.doOnSuccess(o -> System.out.println("Created: " + o))
				.then(occupationRepository.save(occupation))
				.doOnSuccess(o -> System.out.println("Created: " + o))
				.thenMany(underTest.processPendingOccupations());
		// then
		StepVerifier
			.create(create)
			.expectNextMatches(o -> o.getStatus().equals(Occupation.Status.CURRENT))
			.verifyComplete();
	}
	
	@Test
	void processPendingOccupations_updatesUnitStatusToOccupied_whenSuccessful() {
		// given
		processPendingOccupations_returnsOccupation_whenSuccessful();
		// when
		Flux<Unit> units = unitRepository.findAll();
		// then
		StepVerifier
			.create(units)
			.expectNextMatches(u -> u.getStatus().equals(Unit.Status.OCCUPIED))
			.verifyComplete();		
	}
	
	@Test
	void processPendingOccupations_createsEmailNotification_whenSuccessful() {
		// given
		processPendingOccupations_returnsOccupation_whenSuccessful();
		// when
		Flux<EmailNotification> emailNotifications = emailNotificationRepository.findAll();
		// then
		StepVerifier
			.create(emailNotifications)
			.expectNextMatches(e -> e.getStatus().equals(NotificationStatus.PENDING))
			.verifyComplete();
	}
}
