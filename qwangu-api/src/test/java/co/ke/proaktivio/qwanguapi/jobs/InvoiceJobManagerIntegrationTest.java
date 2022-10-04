package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationTransactionRepository;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.repositories.UnitRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

@Testcontainers
@SpringBootTest
class InvoiceJobManagerIntegrationTest {
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private OccupationTransactionRepository occupationTransactionRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private InvoiceJobManager manager;

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
                "1", "1", now, null, null, null);
//        new Unit("1", Unit.Status.OCCUPIED, false, "TE99", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.A, 1, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(27000), BigDecimal.valueOf(500), BigDecimal.valueOf(300), now,
//                null, "1");
        var currentUnit = new Unit.UnitBuilder()
                .status(Unit.Status.OCCUPIED)
                .booked(false)
                .accountNo("TE99")
                .type(Unit.Type.APARTMENT_UNIT)
                .identifier(Unit.Identifier.A)
                .floorNo(1)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        currentUnit.setId("1");

        var bookedOccupation = new Occupation("2", Occupation.Status.BOOKED, now, null,
                "2", "2", now, null, null, null);
//        var bookedUnit = new Unit("2", Unit.Status.VACANT, false, "TE100", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.B, 1, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(27000), BigDecimal.valueOf(500), BigDecimal.valueOf(300), now, null,
//                "1");
        var bookedUnit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
                .booked(false)
                .accountNo("TE100")
                .type(Unit.Type.APARTMENT_UNIT)
                .identifier(Unit.Identifier.B)
                .floorNo(1)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        bookedUnit.setId("2");

        var occupationTransaction = new OccupationTransaction.OccupationTransactionBuilder()
                .type(OccupationTransaction.Type.DEBIT)
                .occupationId("1")
                .invoiceId("1")
                .receiptId("1")
                .totalAmountOwed(BigDecimal.valueOf(5000))
                .totalAmountPaid(BigDecimal.ZERO)
                .totalAmountCarriedForward(BigDecimal.valueOf(5000))
                .build();
        occupationTransaction.setCreatedOn(now.minusDays(15));

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
        Flux<Invoice> allReceivables = invoiceRepository.findAll()
                .doOnNext(t -> System.out.println("---- Found: " + t));
        StepVerifier
                .create(allReceivables)
                .expectNextMatches(r -> r.getType().equals(Invoice.Type.RENT) &&
                        r.getRentAmount().equals(BigDecimal.valueOf(27000)) &&
                        r.getSecurityAmount().equals(BigDecimal.valueOf(500)) &&
                        r.getGarbageAmount().equals(BigDecimal.valueOf(300)))
                .expectNextMatches(r -> r.getType().equals(Invoice.Type.RENT) &&
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