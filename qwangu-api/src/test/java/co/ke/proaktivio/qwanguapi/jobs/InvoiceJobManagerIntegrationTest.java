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
import java.time.LocalDate;
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
        LocalDate now = LocalDate.now();
        var currentOccupation = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(now.minusDays(30))
                .unitId("1")
                .build();
        currentOccupation.setId("1");
        currentOccupation.setStatus(Occupation.Status.CURRENT);
        var currentUnit = new Unit.UnitBuilder()
                .status(Unit.Status.OCCUPIED)
//                .booked(false)
                .number("TE99")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.A)
                .floorNo(1)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .propertyId("1").build();
        currentUnit.setId("1");
        var bookedOccupation = new Occupation.OccupationBuilder()
                .tenantId("2")
                .startDate(now)
                .unitId("2")
                .build();
        bookedOccupation.setStatus(Occupation.Status.PENDING_OCCUPATION);
        bookedOccupation.setId("2");
        var bookedUnit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
//                .booked(false)
                .number("TE100")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.B)
                .floorNo(1)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .propertyId("1").build();
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
        occupationTransaction.setCreatedOn(LocalDateTime.now().minusDays(15));

        // when
        var createRentInvoices = occupationRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(unitRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(occupationTransactionRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all OccupationTransactions!"))
                .then(invoiceRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Invoices!"))
                .thenMany(unitRepository.saveAll(List.of(currentUnit, bookedUnit)))
                .doOnNext(t -> System.out.println("---- Created: " + t))
                .thenMany(occupationRepository.saveAll(List.of(currentOccupation, bookedOccupation)))
                .doOnNext(t -> System.out.println("---- Created: " + t))
                .then(occupationTransactionRepository.save(occupationTransaction))
                .doOnSuccess(t -> System.out.println("---- Created: " + t))
                .thenMany(manager.createRentInvoices())
                .doOnNext(invoice -> System.out.println("---- Found :" +invoice));

        // then
        StepVerifier
                .create(createRentInvoices)
                .expectNextMatches(invoice -> !invoice.getId().isEmpty() && !invoice.getOccupationId().isEmpty() &&
                        !invoice.getNumber().isEmpty() && invoice.getType().equals(Invoice.Type.RENT) &&
                        invoice.getStartDate() != null && invoice.getRentAmount().equals(BigDecimal.valueOf(27000)) &&
                        invoice.getSecurityAmount().equals(BigDecimal.valueOf(500)) &&
                        invoice.getGarbageAmount().equals(BigDecimal.valueOf(300)) && invoice.getCreatedOn() != null &&
                        invoice.getModifiedOn() != null)
                .verifyComplete();

        // then
        Flux<OccupationTransaction> allOccupationTransactions = occupationTransactionRepository
                .findAll(Sort.by(Sort.Direction.DESC, "createdOn"))
                .doOnNext(t -> System.out.println("---- Found: " + t));
        StepVerifier
                .create(allOccupationTransactions)
                .expectNextMatches(ot -> !ot.getId().isEmpty() && ot.getOccupationId().equals("1") &&
                        !ot.getInvoiceId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                        ot.getTotalAmountOwed().equals(BigDecimal.valueOf(27800)) &&
                        ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(32800)))
                .expectNextMatches(ot -> ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                        ot.getTotalAmountOwed().equals(BigDecimal.valueOf(5000)) &&
                        ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(5000)))
                .verifyComplete();
    }
}