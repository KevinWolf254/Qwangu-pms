package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Receivable;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.repositories.ReceivableRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.services", "co.ke.proaktivio.qwanguapi.repositories"})
class ReceivableJobManagerIntegrationTest {
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private OccupationTransactionRepository occupationTransactionRepository;
    @Autowired
    private ReceivableRepository receivableRepository;
    @Autowired
    private ReceivableJobManager manager;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Test
    void create() {
        // given
        LocalDateTime now = LocalDateTime.now();
        var currentOccupation = new Occupation("1", Occupation.Status.CURRENT, now.minusDays(30), null,
                "1", "1", now, null);
        var currentUnit = new Unit("1", Unit.Status.OCCUPIED, false, "TE99", Unit.Type.APARTMENT_UNIT,
                Unit.Identifier.A, 1, 2, 1, 2, Unit.Currency.KES,
                BigDecimal.valueOf(27000), BigDecimal.valueOf(500), BigDecimal.valueOf(300), now,
                null, "1");

        var bookedOccupation = new Occupation("2", Occupation.Status.BOOKED, now, null,
                "2", "2", now, null);
        var bookedUnit = new Unit("2", Unit.Status.VACANT, false, "TE100", Unit.Type.APARTMENT_UNIT,
                Unit.Identifier.B, 1, 2, 1, 2, Unit.Currency.KES,
                BigDecimal.valueOf(27000), BigDecimal.valueOf(500), BigDecimal.valueOf(300), now, null,
                "1");

        var occupationTransaction = new OccupationTransaction(null, OccupationTransaction.Type.DEBIT,
                BigDecimal.valueOf(5000), BigDecimal.ZERO, BigDecimal.valueOf(5000), "1", "1",
                "1", now.minusDays(15));

        // when
        Flux<OccupationTransaction> createRentInvoices = occupationRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(occupationTransactionRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all OccupationTransactions!"))
                .thenMany(unitRepository.saveAll(List.of(currentUnit, bookedUnit)))
                .doOnNext(t -> System.out.println("---- Created: " + t))
                .thenMany(occupationRepository.saveAll(List.of(currentOccupation, bookedOccupation)))
                .doOnNext(t -> System.out.println("---- Created: " + t))
                .then(occupationTransactionRepository.save(occupationTransaction))
                .doOnSuccess(t -> System.out.println("---- Created: " + t))
                .thenMany(manager.createRentInvoices());

        // then
        StepVerifier
                .create(createRentInvoices)
                .expectNextCount(2)
                .verifyComplete();

        // then
        Flux<Receivable> allReceivables = receivableRepository.findAll()
                .doOnNext(t -> System.out.println("---- Found: " + t));
        StepVerifier
                .create(allReceivables)
                .expectNextMatches(r -> r.getType().equals(Receivable.Type.RENT) &&
                        r.getRentAmount().equals(BigDecimal.valueOf(27000)) &&
                        r.getSecurityAmount().equals(BigDecimal.valueOf(500)) &&
                        r.getGarbageAmount().equals(BigDecimal.valueOf(300)))
                .expectNextMatches(r -> r.getType().equals(Receivable.Type.RENT) &&
                        r.getRentAmount().equals(BigDecimal.valueOf(27000)) &&
                        r.getSecurityAmount().equals(BigDecimal.valueOf(500)) &&
                        r.getGarbageAmount().equals(BigDecimal.valueOf(300)))
                .verifyComplete();

        // then
        Flux<OccupationTransaction> allOccupationTransactions = occupationTransactionRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdOn"))
                .doOnNext(t -> System.out.println("---- Found: " + t));
        StepVerifier
                .create(allOccupationTransactions)
                .expectNextMatches(ot -> ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                        ot.getTotalAmountOwed().equals(BigDecimal.valueOf(27800)) &&
                        ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                        (ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(27800)) ||
                                ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(32800))))
                .expectNextMatches(ot -> ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                        ot.getTotalAmountOwed().equals(BigDecimal.valueOf(27800)) &&
                        ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                        (ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(27800)) ||
                                ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(32800))))
                .expectNextMatches(ot -> ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                        ot.getTotalAmountOwed().equals(BigDecimal.valueOf(5000)) &&
                        ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(5000)))
                .verifyComplete();
    }
}