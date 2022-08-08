package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.RentInvoice;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.RentTransactionRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
class RentJobManagerIntegrationTest {
    @Autowired
    private RentTransactionRepository rentTransactionRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private ReactiveMongoTemplate template;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private RentJobManager manager;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    private final String UNIT_ID = "1";
    private final Unit UNIT = new Unit(UNIT_ID, Unit.Status.OCCUPIED, "TE99", Unit.Type.APARTMENT_UNIT, Unit.Identifier.A,
            2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");
    private final String OCCUPATION_ID = "1";
    private final Occupation OCCUPATION = new Occupation(OCCUPATION_ID, Occupation.Status.CURRENT, LocalDateTime.now(), null, "1", UNIT_ID, LocalDateTime.now(), null);

    private final String UNIT_ID_v2 = "2";
    private final Unit UNIT_v2 = new Unit(UNIT_ID_v2, Unit.Status.OCCUPIED, "TE100", Unit.Type.APARTMENT_UNIT, Unit.Identifier.B,
            2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, LocalDateTime.now(), null, "1");
    private final String OCCUPATION_ID_v2 = "2";
    private final Occupation OCCUPATION_v2 = new Occupation(OCCUPATION_ID_v2, Occupation.Status.CURRENT, LocalDateTime.now(), null, "2", UNIT_ID_v2, LocalDateTime.now(), null);
    private final RentInvoice debitTransaction = new RentInvoice(null, RentInvoice.Type.DEBIT, BigDecimal.valueOf(UNIT_v2.getRentPerMonth()),
            BigDecimal.valueOf(UNIT_v2.getSecurityPerMonth()), BigDecimal.valueOf(UNIT_v2.getGarbagePerMonth()), BigDecimal.valueOf(5000), BigDecimal.valueOf(300),
            BigDecimal.valueOf(250), BigDecimal.valueOf(150), BigDecimal.valueOf(1500), LocalDateTime.now(), null, OCCUPATION_ID_v2, null);

    private final Occupation occupationWithStatusMoved = new Occupation("3", Occupation.Status.MOVED, LocalDateTime.now().minusDays(90), LocalDateTime.now(), "4", UNIT_ID, LocalDateTime.now(), null);

    private Mono<Occupation> createNew() {
        return unitRepository.deleteAll()
                .doOnSuccess($ -> System.out.println("---- Deleted all units!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess($ -> System.out.println("---- Deleted all occupations!"))
                .then(unitRepository.save(UNIT))
                .doOnSuccess(u -> System.out.println("---- Created " + u))
                .then(occupationRepository.save(OCCUPATION))
                .doOnSuccess(o -> System.out.println("---- Created " + o));
    }

    @Test
    void create() {
        // when
        Flux<RentInvoice> createForNew = createNew()
                .thenMany(manager.create())
                .doOnNext(r -> System.out.println("---- Created " + r));

        // then
        StepVerifier
                .create(createForNew)
                .expectNextMatches(r -> !r.getId().isEmpty() && r.getRentAmount().equals(BigDecimal.valueOf(27000)))
                .verifyComplete();

        // when
        Flux<RentInvoice> createForNewAndExisting = createForNewAndExisting();
        // then
        StepVerifier
                .create(createForNewAndExisting)
                .expectNextCount(1)
                .expectNextMatches(r -> r.getRentAmountCarriedForward().equals(BigDecimal.valueOf(5000)))
                .verifyComplete();

        // when
        Flux<RentInvoice> createOccupationMoved = createForNewAndExisting()
                .then(occupationRepository.save(occupationWithStatusMoved))
                .doOnSuccess(o -> System.out.println("---- Created " + o))
                .thenMany(manager.create());
        // then
        StepVerifier
                .create(createOccupationMoved)
                .expectNextCount(2)
                .verifyComplete();

    }

    private Flux<RentInvoice> createForNewAndExisting() {
        return createNew()
                .then(unitRepository.save(UNIT_v2))
                .doOnSuccess(u -> System.out.println("---- Created " + u))
                .then(occupationRepository.save(OCCUPATION_v2))
                .doOnSuccess(o -> System.out.println("---- Created " + o))
                .then(rentTransactionRepository.save(debitTransaction))
                .thenMany(manager.create())
                .doOnNext(r -> System.out.println("---- Created " + r));
    }
}