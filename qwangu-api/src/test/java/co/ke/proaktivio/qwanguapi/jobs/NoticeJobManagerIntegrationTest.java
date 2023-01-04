package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.repositories.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Testcontainers
@SpringBootTest
class NoticeJobManagerIntegrationTest {
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private NoticeJobManager noticeJobManager;
    @Autowired
    private RefundRepository refundRepository;
    private final LocalDate today = LocalDate.now();

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
                .then(invoiceRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Invoices!"))
                .then(noticeRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Notices!"))
                .then(refundRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Refunds!"));
    }

    private Unit getUnit() {
        Map<String, BigDecimal> otherAmounts = getOtherAmounts();
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.OCCUPIED)
                .number("TE34")
                .type(Unit.Type.APARTMENT_UNIT)
                .identifier(Unit.Identifier.A)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(510))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .otherAmounts(otherAmounts)
                .apartmentId("1")
                .build();
        unit.setId("1");
        return unit;
    }

    @NotNull
    private static HashMap<String, BigDecimal> getOtherAmounts() {
        var otherAmounts = new HashMap<String, BigDecimal>();
        otherAmounts.put("GYM", BigDecimal.valueOf(500));
        return otherAmounts;
    }

    private Tenant getTenant() {
        var tenant = new Tenant.TenantBuilder()
                .firstName("John")
                .middleName("Joe")
                .surname("Doe")
                .mobileNumber("0700000000")
                .emailAddress("person@gmail.com")
                .build();
        tenant.setId("1");
        return tenant;
    }
    private Occupation getOccupation() {
        var occupation = new Occupation.OccupationBuilder()
                .status(Occupation.Status.CURRENT)
                .startDate(today.minusDays(61))
                .tenantId("1")
                .unitId("1")
                .build();
        occupation.setId("1");
        return occupation;
    }
    private Notice getNotice() {
        var notice = new Notice.NoticeBuilder()
                .status(Notice.Status.ACTIVE)
                .notificationDate(today.minusDays(31))
                .vacatingDate(today.minusDays(1))
                .occupationId("1")
                .build();
        notice.setId("1");
        return notice;
    }
    private Invoice getInvoice() {
        Map<String, BigDecimal> otherAmounts = getOtherAmounts();
        var invoice = new Invoice.InvoiceBuilder()
                .type(Invoice.Type.RENT_ADVANCE)
                .currency(Unit.Currency.KES)
                .rentAmount(BigDecimal.valueOf(27000).multiply(BigDecimal.valueOf(2)))
                .securityAmount(BigDecimal.valueOf(510))
                .garbageAmount(BigDecimal.valueOf(300))
                .otherAmounts(otherAmounts)
                .occupationId("1")
                .build();
        invoice.setId("1");
        return invoice;
    }

    @NotNull
    private Flux<Notice> updateNotice(Notice notice, Tenant tenant, Occupation occupation, Unit unit, Invoice invoice) {
        Flux<Notice> vacate = reset()
                .then(unitRepository.save(unit))
                .doOnSuccess(u -> System.out.println("---- Saved: " +u))
                .then(tenantRepository.save(tenant))
                .doOnSuccess(t -> System.out.println("---- Saved: " +t))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(o -> System.out.println("---- Saved: " +o))
                .then(invoiceRepository.save(invoice))
                .doOnSuccess(o -> System.out.println("---- Saved: " +o))
                .then(noticeRepository.save(notice))
                .doOnSuccess(n -> System.out.println("---- Saved: " +n))
                .thenMany(noticeJobManager.vacate());
        return vacate;
    }

    @Test
    void vacate_returnsANoticeWithStatusFulfilled_whenSuccessful() {
        // given
        var notice = getNotice();
        var tenant = getTenant();
        var occupation = getOccupation();
        var unit = getUnit();
        var invoice = getInvoice();

        // when
        Flux<Notice> vacate = updateNotice(notice, tenant, occupation, unit, invoice);
        //then
        StepVerifier
                .create(vacate)
                .expectNextMatches(n -> n.getStatus().equals(Notice.Status.FULFILLED))
                .verifyComplete();
    }

    @Test
    void vacate_createsRefund_whenSuccessful() {
        // given
        var notice = getNotice();
        var tenant = getTenant();
        var occupation = getOccupation();
        var unit = getUnit();
        var invoice = getInvoice();

        // when
        Flux<Refund> getRefund = updateNotice(notice, tenant, occupation, unit, invoice)
                .thenMany(refundRepository.findAll());
        //then
        StepVerifier
                .create(getRefund)
                .expectNextMatches(r -> !r.getId().isEmpty() && r.getStatus().equals(Refund.Status.PENDING_REVISION)
                && r.getRefundDate() == null && r.getCurrency().equals(Unit.Currency.KES) &&
                        r.getRent().equals(BigDecimal.valueOf(54000.0)) &&
                        r.getSecurity().equals(BigDecimal.valueOf(510.0))
                        && r.getGarbage().equals(BigDecimal.valueOf(300.0)) && r.getOthers() != null &&
                        r.getTotal().equals(BigDecimal.valueOf(55310.0)) && r.getInvoiceId().equals("1") &&
                        r.getOccupationId().equals("1") && r.getCreatedOn() != null && r.getCreatedBy().equals("SYSTEM")
                        && r.getModifiedOn() != null && r.getModifiedBy().equals("SYSTEM"))
                .verifyComplete();
    }

    @Test
    void vacate_createsRefundWithLevelTwo_whenSuccessful() {
        // given
        var notice = getNotice();
        notice.setNotificationDate(today.minusDays(22));
        var tenant = getTenant();
        var occupation = getOccupation();
        var unit = getUnit();
        var invoice = getInvoice();

        // when
        Flux<Refund> getRefund = updateNotice(notice, tenant, occupation, unit, invoice)
                .thenMany(refundRepository.findAll());
        //then
        StepVerifier
                .create(getRefund)
                .expectNextMatches(r -> !r.getId().isEmpty() && r.getStatus().equals(Refund.Status.PENDING_REVISION)
                        && r.getRefundDate() == null && r.getCurrency().equals(Unit.Currency.KES) &&
                        r.getRent().floatValue() == 40500.00 && r.getSecurity().floatValue() == 382.50
                        && r.getGarbage().floatValue() == 225.00 && r.getOthers() != null &&
                        r.getTotal().floatValue() == 41482.50 &&
                        r.getInvoiceId().equals("1") &&
                        r.getOccupationId().equals("1") && r.getCreatedOn() != null && r.getCreatedBy().equals("SYSTEM")
                        && r.getModifiedOn() != null && r.getModifiedBy().equals("SYSTEM"))
                .verifyComplete();
    }

    @Test
    void vacate_createsRefundWithLevelThree_whenSuccessful() {
        // given
        var notice = getNotice();
        notice.setNotificationDate(today.minusDays(15));
        var tenant = getTenant();
        var occupation = getOccupation();
        var unit = getUnit();
        var invoice = getInvoice();

        // when
        Flux<Refund> getRefund = updateNotice(notice, tenant, occupation, unit, invoice)
                .thenMany(refundRepository.findAll());
        //then
        StepVerifier
                .create(getRefund)
                .expectNextMatches(r -> !r.getId().isEmpty() && r.getStatus().equals(Refund.Status.PENDING_REVISION)
                        && r.getRefundDate() == null && r.getCurrency().equals(Unit.Currency.KES) &&
                        r.getRent().floatValue() == 27000.0 && r.getSecurity().floatValue() == 255.0
                        && r.getGarbage().floatValue() == 150.0 && r.getOthers() != null &&
                        r.getTotal().floatValue() == 27655.0 &&
                        r.getInvoiceId().equals("1") &&
                        r.getOccupationId().equals("1") && r.getCreatedOn() != null && r.getCreatedBy().equals("SYSTEM")
                        && r.getModifiedOn() != null && r.getModifiedBy().equals("SYSTEM"))
                .verifyComplete();
    }

    @Test
    void vacate_createsRefundWithLevelFour_whenSuccessful() {
        // given
        var notice = getNotice();
        notice.setNotificationDate(today.minusDays(8));
        var tenant = getTenant();
        var occupation = getOccupation();
        var unit = getUnit();
        var invoice = getInvoice();

        // when
        Flux<Refund> getRefund = updateNotice(notice, tenant, occupation, unit, invoice)
                .thenMany(refundRepository.findAll());
        //then
        StepVerifier
                .create(getRefund)
                .expectNextMatches(r -> !r.getId().isEmpty() && r.getStatus().equals(Refund.Status.PENDING_REVISION)
                        && r.getRefundDate() == null && r.getCurrency().equals(Unit.Currency.KES) &&
                        r.getRent().floatValue() == 13500.00 && r.getSecurity().floatValue() == 127.50
                        && r.getGarbage().floatValue() == 75.00 && r.getOthers() != null &&
                        r.getTotal().floatValue() == 13827.50 &&
                        r.getInvoiceId().equals("1") &&
                        r.getOccupationId().equals("1") && r.getCreatedOn() != null && r.getCreatedBy().equals("SYSTEM")
                        && r.getModifiedOn() != null && r.getModifiedBy().equals("SYSTEM"))
                .verifyComplete();
    }

    @Test
    void vacate_createsRefundWithLevelFive_whenSuccessful() {
        // given
        var notice = getNotice();
        notice.setNotificationDate(today.minusDays(2));
        var tenant = getTenant();
        var occupation = getOccupation();
        var unit = getUnit();
        var invoice = getInvoice();

        // when
        Flux<Refund> getRefund = updateNotice(notice, tenant, occupation, unit, invoice)
                .thenMany(refundRepository.findAll());
        //then
        StepVerifier
                .create(getRefund)
                .expectNextMatches(r -> !r.getId().isEmpty() && r.getStatus().equals(Refund.Status.PENDING_REVISION)
                        && r.getRefundDate() == null && r.getCurrency().equals(Unit.Currency.KES) &&
                        r.getRent().floatValue() == 0.0 && r.getSecurity().floatValue() == 127.50
                        && r.getGarbage().floatValue() == 0.0 && r.getOthers() != null &&
                        r.getTotal().floatValue() == 0.0 &&
                        r.getInvoiceId().equals("1") &&
                        r.getOccupationId().equals("1") && r.getCreatedOn() != null && r.getCreatedBy().equals("SYSTEM")
                        && r.getModifiedOn() != null && r.getModifiedBy().equals("SYSTEM"))
                .verifyComplete();
    }

    @Test
    void vacate_updateUnitToVacantUpdatesOccupationToVacatedUpdatesNoticeToFulfilledRefund_whenSuccessful() {
        // given
        var notice = getNotice();
        var tenant = getTenant();
        var occupation = getOccupation();
        var unit = getUnit();
        var invoice = getInvoice();

        // when
        Flux<Unit> updatedUnit = updateNotice(notice, tenant, occupation, unit, invoice)
                .thenMany(unitRepository.findById("1"));
        //then
        StepVerifier
                .create(updatedUnit)
                .expectNextMatches(u -> u.getStatus().equals(Unit.Status.VACANT))
                .verifyComplete();

        // when
        Mono<Occupation> updatedOccupation = occupationRepository.findById("1");

        //then
        StepVerifier
                .create(updatedOccupation)
                .expectNextMatches(o -> o.getStatus().equals(Occupation.Status.VACATED) &&
                        o.getEndDate().equals(notice.getVacatingDate()))
                .verifyComplete();

        // when
        Mono<Notice> updatedNotice = noticeRepository.findById("1");

        // then
        StepVerifier
                .create(updatedNotice)
                .expectNextMatches(n -> n.getStatus().equals(Notice.Status.FULFILLED))
                .verifyComplete();

    }
}

