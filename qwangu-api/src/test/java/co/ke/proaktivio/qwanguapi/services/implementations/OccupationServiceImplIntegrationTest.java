package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.models.Occupation.Status;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.repositories.*;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import static java.time.temporal.TemporalAdjusters.lastDayOfMonth;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class OccupationServiceImplIntegrationTest {
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OccupationService occupationService;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private OccupationTransactionRepository occupationTransactionRepository;
    @Autowired
    private ReceiptRepository receiptRepository;

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @NotNull
    private Mono<Void> reset() {
        return unitRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Units!"))
                .then(tenantRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"))
                .then(paymentRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Payments!"));
    }

    private Unit getUnit() {
        Unit unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
                .number("TE99")
                .type(Unit.UnitType.APARTMENT_UNIT)
                .identifier(Unit.Identifier.B)
                .floorNo(2)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(27000))
                .securityPerMonth(BigDecimal.valueOf(510))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .propertyId("1").build();
        unit.setId("12");
        return unit;
    }

    private Payment getPayment() {
        var payment = new Payment.PaymentBuilder()
                .status(Payment.Status.NEW)
                .type(Payment.Type.MPESA_PAY_BILL)
                .transactionId("RKTQDM7W67")
                .transactionType("Pay Bill")
                .transactionTime(LocalDateTime.now())
                .currency(Unit.Currency.KES)
                .amount(BigDecimal.valueOf(20000))
                .shortCode("600638")
                .referenceNo("TE3490")
                .balance("49197.00")
                .mobileNumber("254708374147")
                .firstName("John")
                .build();
        payment.setId("15");
        return payment;
    }

    @NotNull
    private static Tenant getTenant() {
        var tenant = new Tenant.TenantBuilder()
                .firstName("John")
                .middleName("Doe")
                .surname("Jane")
                .mobileNumber("0720000001")
                .emailAddress("jJane@mail.co.ke")
                .build();
        tenant.setId("1");
        return tenant;
    }

    @NotNull
    private static Occupation getOccupation(Unit unit, Tenant tenant) {
        var occupation = new Occupation.OccupationBuilder()
                .status(Occupation.Status.PENDING_VACATING)
                .startDate(LocalDate.now())
                .tenantId(tenant.getId())
                .unitId(unit.getId())
                .build();
        occupation.setId("1");
        return occupation;
    }

    @Test
    void create_returnsCustomBadException_whenPaymentStatusIsProcessed() {
        // given
        var unit = getUnit();
        unit.setStatus(Unit.Status.OCCUPIED);
        Tenant tenant = getTenant();
        Occupation occupation = getOccupation(unit, tenant);
        LocalDate now = LocalDate.now();
        var notice = new Notice.NoticeBuilder()
                .status(Notice.Status.ACTIVE)
                .notificationDate(now)
                .vacatingDate(now.plusDays(5))
                .occupationId("1")
                .build();

        var payment = getPayment();
        payment.setStatus(Payment.Status.PROCESSED);
        var tenantDto = new TenantDto("John", "middle", "Doe","0700000000",
                "person@gmail.com");
        var dto = new OccupationForNewTenantDto(null, tenantDto, new OccupationDto(now.plusDays(6),
                unit.getId(), payment.getId()));

        // when
        Mono<Occupation> createOccupation = reset()
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(noticeRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Notices!"))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((tenantRepository.save(tenant)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((occupationRepository.save(occupation)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((noticeRepository.save(notice)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(paymentRepository.save(payment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto));

        // then
        StepVerifier
                .create(createOccupation)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Payment with id %s has already been processed!".formatted(payment.getId())))
                .verify();
    }

    private
    @Test
    void create_returnsCustomBadException_whenPaymentDoesNotExist() {
        // given
        var unit = getUnit();
        unit.setStatus(Unit.Status.OCCUPIED);
        Tenant tenant = getTenant();
        Occupation occupation = getOccupation(unit, tenant);
        var notice = new Notice.NoticeBuilder()
                .status(Notice.Status.ACTIVE)
                .notificationDate(LocalDate.now())
                .vacatingDate(LocalDate.now().minusDays(5))
                .occupationId("1")
                .build();

        var tenantDto = new TenantDto("John", "middle", "Doe","0700000000",
                "person@gmail.com");
        var dto = new OccupationForNewTenantDto(null, tenantDto, new OccupationDto(LocalDate.now(), unit.getId(), "1"));

        // when
        Mono<Occupation> createOccupation = reset()
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((tenantRepository.save(tenant)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((occupationRepository.save(occupation)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((noticeRepository.save(notice)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto));

        // then
        StepVerifier
                .create(createOccupation)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Payment with id %s does not exist!".formatted("1")))
                .verify();
    }

    @Test
    void create_returnsCustomBadException_whenOccupationDateIsBeforeVacatingDate() {
        // given
        var unit = getUnit();
        unit.setStatus(Unit.Status.OCCUPIED);
        Tenant tenant = getTenant();
        Occupation occupation = getOccupation(unit, tenant);
        var notice = new Notice.NoticeBuilder()
                .status(Notice.Status.ACTIVE)
                .notificationDate(LocalDate.now())
                .vacatingDate(LocalDate.now())
                .occupationId("1")
                .build();

        var payment = getPayment();
        var tenantDto = new TenantDto("John", "middle", "Doe","0700000000",
                "person@gmail.com");
        var dto = new OccupationForNewTenantDto(null, tenantDto, new OccupationDto(LocalDate.now().minusDays(5), unit.getId(), payment.getId()));

        // when
        Mono<Occupation> createOccupation = reset()
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((tenantRepository.save(tenant)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((occupationRepository.save(occupation)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((noticeRepository.save(notice)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(paymentRepository.save(payment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto));

        // then
        StepVerifier
                .create(createOccupation)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Invalid occupation date. It should be after the current occupant's vacating date!"))
                .verify();
    }

    @Test
    void create_returnsCustomBadException_whenTenantIsNotVacating() {
        // given
        var unit = getUnit();
        unit.setStatus(Unit.Status.OCCUPIED);
        Tenant tenant = getTenant();
        var occupation = new Occupation.OccupationBuilder()
                .status(Occupation.Status.CURRENT)
                .startDate(LocalDate.now())
                .tenantId(tenant.getId())
                .unitId(unit.getId())
                .build();
        var payment = getPayment();
        payment.setAmount(BigDecimal.valueOf(81810));
        var tenantDto = new TenantDto("John", "middle", "Doe","0700000000",
                "person@gmail.com");
        var dto = new OccupationForNewTenantDto(null, tenantDto, new OccupationDto(LocalDate.now(), unit.getId(), payment.getId()));

        // when
        Mono<Occupation> createOccupation = reset()
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((tenantRepository.save(tenant)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then((occupationRepository.save(occupation)))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(paymentRepository.save(payment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto));

        // then
        StepVerifier
                .create(createOccupation)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Unit is unavailable. Occupant has not given a vacating notice!"))
                .verify();
    }

    @Test
    void create_returnsCustomBadException_whenTenantIdDoesNotExist() {
        // given
        var unit = getUnit();
        var payment = getPayment();
        var dto = new OccupationForNewTenantDto("1", null, new OccupationDto(LocalDate.now(), unit.getId(), payment.getId()));

        // when
        Mono<Occupation> createOccupation = reset()
                .then(paymentRepository.save(payment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto));

        // then
        StepVerifier
                .create(createOccupation)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Tenant with id %s does not exist!".formatted("1")))
                .verify();
    }

    @Test
    void create_returnsCustomBadException_whenUnitIdDoesNotExist() {
        // given
        var unitId = "1";
        var payment = getPayment();
        var tenantDto = new TenantDto("John", "middle", "Doe","0700000000",
                "person@gmail.com");
        var dto = new OccupationForNewTenantDto(null, tenantDto, new OccupationDto(LocalDate.now(), unitId, payment.getId()));

        // when
        Mono<Occupation> createOccupation = reset()
                .then(paymentRepository.save(payment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto));

        // then
        StepVerifier
                .create(createOccupation)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Unit with id %s does not exist!".formatted(unitId)))
                .verify();
    }

    @Test
    void create_returnsCustomBadException_whenAmountPaidIsLessThanExpected() {
        // given
        var unit = getUnit();
        var payment = getPayment();
        var tenantDto = new TenantDto("John", "middle", "Doe","0700000000",
                "person@gmail.com");
        var dto = new OccupationForNewTenantDto(null, tenantDto, new OccupationDto(LocalDate.now(), unit.getId(), payment.getId()));

        // when
        Mono<Occupation> createOccupation = reset()
                .then(paymentRepository.save(payment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto));

        // then
        StepVerifier
                .create(createOccupation)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Amount to be paid should be KES 81810 but was KES 20000"))
                .verify();
    }

    @Test
    void create_returnsCustomBadRequestException_whenVacantUnitIsAlreadyBooked() {
        // given
        var unit = getUnit();
        var payment = getPayment();
        payment.setAmount(BigDecimal.valueOf(81810));
        var payment01 = getPayment();
        payment01.setId("1200");
        var tenantDto = new TenantDto("John", "Percy", "Doe", "0700000000",
                "person@gmail.com");
        var tenantDto01 = new TenantDto("John", "Percy", "Doe", "0700000001",
                "person01@gmail.com");
        var dto = new OccupationForNewTenantDto(null, tenantDto, new OccupationDto(LocalDate.now(), unit.getId(),
                payment.getId()));
        var dto01 = new OccupationForNewTenantDto(null, tenantDto01, new OccupationDto(LocalDate.now(), unit.getId(),
                payment01.getId()));
        // when
        Mono<Occupation> createOccupation = reset()
                .then(invoiceRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Invoices!"))
                .then(paymentRepository.save(payment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(paymentRepository.save(payment01))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto01))
                .doOnSuccess(a -> System.out.println("---- Saved " + a));

        // then
        StepVerifier
                .create(createOccupation)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Unit has already been booked!"))
                .verify();
    }

    // TODO ENABLE TRANSACTIONAL
    @Test
    void create_returnsRollBackSuccessful_whenRunTimeExceptionOccurs() {
        // given
        create_returnsCustomBadRequestException_whenVacantUnitIsAlreadyBooked();
        // when
        Flux<Tenant> tenants =
        tenantRepository.findAll()
                .doOnNext(a -> System.out.println("---- Found " + a));
        // then
        StepVerifier
                .create(tenants)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void create_returnsOccupationAndTenantAndInvoiceAndReceiptAndSetsPaymentAsProcessed_whenSuccessful() {
        // given
        var unit = getUnit();
        var payment = getPayment();
        payment.setAmount(BigDecimal.valueOf(81810));
        var tenantDto = new TenantDto("John", "Percy", "Doe","0700000000",
                "person@gmail.com");
        var dto = new OccupationForNewTenantDto(null, tenantDto, new OccupationDto(LocalDate.now(), unit.getId(), payment.getId()));

        // when
        Mono<Occupation> createOccupation = reset()
                .then(receiptRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Receipts!"))
                .then(invoiceRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Invoices!"))
                .then(paymentRepository.save(payment))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(unitRepository.save(unit))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.create(dto));

        // then
        StepVerifier
                .create(createOccupation)
                .expectNextMatches(occupation -> !occupation.getId().isEmpty() &&
                        occupation.getStatus().equals(Occupation.Status.PENDING_OCCUPATION) &&
                        !occupation.getNumber().isEmpty() && occupation.getStartDate() != null &&
                        !occupation.getTenantId().isEmpty() && !occupation.getUnitId().isEmpty() &&
                        occupation.getCreatedOn() != null && occupation.getEndDate() == null &&
                        !occupation.getCreatedBy().isEmpty() && occupation.getModifiedOn() != null &&
                        !occupation.getModifiedBy().isEmpty())
                .verifyComplete();

        // when
        Flux<Tenant> tenants = tenantRepository.findAll()
                .doOnNext(a -> System.out.println("---- Found " + a));

        // then
        StepVerifier
                .create(tenants)
                .expectNextMatches(tenant -> !tenant.getId().isEmpty() && tenant.getFirstName().equals("John") &&
                        tenant.getMiddleName().equals("Percy") && tenant.getSurname().equals("Doe") &&
                        tenant.getMobileNumber().equals("0700000000") && tenant.getEmailAddress().equals("person@gmail.com"))
                .verifyComplete();

        // when
        Flux<Invoice> invoices = invoiceRepository.findAll()
                .doOnNext(a -> System.out.println("---- Found " + a));

        // then
        StepVerifier
                .create(invoices)
                .expectNextMatches(invoice -> (!invoice.getId().isEmpty() && !invoice.getNumber().isEmpty() &&
                        invoice.getType().equals(Invoice.Type.RENT_ADVANCE) &&
                                invoice.getStartDate().equals(dto.getOccupation().getStartDate()) &&
                        invoice.getEndDate() == null && invoice.getRentAmount().equals(BigDecimal.valueOf(54000)) &&
                        invoice.getSecurityAmount() == null && invoice.getGarbageAmount() == null &&
                        invoice.getOtherAmounts() == null && !invoice.getOccupationId().isEmpty()) &&
                        invoice.getCreatedOn() != null && invoice.getCreatedBy().equals("SYSTEM") &&
                        invoice.getModifiedOn() != null && invoice.getModifiedBy().equals("SYSTEM") ||
                        (!invoice.getId().isEmpty() && !invoice.getNumber().isEmpty() &&
                                invoice.getType().equals(Invoice.Type.RENT) &&
                                invoice.getStartDate().equals(dto.getOccupation().getStartDate()) &&
                                invoice.getEndDate().equals(LocalDate.now().with(lastDayOfMonth())) &&
                                invoice.getRentAmount().equals(BigDecimal.valueOf(27000)) &&
                                invoice.getSecurityAmount().equals(BigDecimal.valueOf(510)) &&
                                invoice.getGarbageAmount().equals(BigDecimal.valueOf(300)) &&
                                invoice.getOtherAmounts() == null && !invoice.getOccupationId().isEmpty()) &&
                                invoice.getCreatedOn() != null && invoice.getCreatedBy().equals("SYSTEM") &&
                                invoice.getModifiedOn() != null && invoice.getModifiedBy().equals("SYSTEM"))
                .expectNextMatches(invoice -> (!invoice.getId().isEmpty() && !invoice.getNumber().isEmpty() &&
                        invoice.getType().equals(Invoice.Type.RENT_ADVANCE) &&
                        invoice.getStartDate().equals(dto.getOccupation().getStartDate()) &&
                        invoice.getEndDate() == null && invoice.getRentAmount().equals(BigDecimal.valueOf(54000)) &&
                        invoice.getSecurityAmount() == null && invoice.getGarbageAmount() == null &&
                        invoice.getOtherAmounts() == null && !invoice.getOccupationId().isEmpty()) &&
                        invoice.getCreatedOn() != null && invoice.getCreatedBy().equals("SYSTEM") &&
                        invoice.getModifiedOn() != null && invoice.getModifiedBy().equals("SYSTEM") ||
                        (!invoice.getId().isEmpty() && !invoice.getNumber().isEmpty() &&
                                invoice.getType().equals(Invoice.Type.RENT) &&
                                invoice.getStartDate().equals(dto.getOccupation().getStartDate()) &&
                                invoice.getEndDate().equals(LocalDate.now().with(lastDayOfMonth())) &&
                                invoice.getRentAmount().equals(BigDecimal.valueOf(27000)) &&
                                invoice.getSecurityAmount().equals(BigDecimal.valueOf(510)) &&
                                invoice.getGarbageAmount().equals(BigDecimal.valueOf(300)) &&
                                invoice.getOtherAmounts() == null && !invoice.getOccupationId().isEmpty()) &&
                                invoice.getCreatedOn() != null && invoice.getCreatedBy().equals("SYSTEM") &&
                                invoice.getModifiedOn() != null && invoice.getModifiedBy().equals("SYSTEM"))
                .verifyComplete();

        // when
        Flux<Receipt> receipts = receiptRepository.findAll()
                .doOnNext(a -> System.out.println("---- Found " + a));

        // then
        StepVerifier
                .create(receipts)
                .expectNextMatches(receipt -> !receipt.getId().isEmpty() && !receipt.getNumber().isEmpty() &&
                        !receipt.getOccupationId().isEmpty() && receipt.getPaymentId().equals("15") &&
                        receipt.getCreatedOn() != null && receipt.getCreatedBy().equals("SYSTEM") &&
                        receipt.getModifiedOn() != null && receipt.getModifiedBy().equals("SYSTEM"))
                .verifyComplete();

        // when
        Flux<OccupationTransaction> occupationTransactions = occupationTransactionRepository.findAll()
                .doOnNext(a -> System.out.println("---- Found " + a));

        // then
        StepVerifier
                .create(occupationTransactions)
                .expectNextMatches(ot -> (!ot.getId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                        !ot.getOccupationId().isEmpty() && !ot.getInvoiceId().isEmpty() && ot.getReceiptId() == null &&
                        ot.getTotalAmountOwed().equals(BigDecimal.valueOf(54000)) &&
                        ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(54000)) &&
                        ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
                        ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM")) ||
                        (!ot.getId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                                !ot.getOccupationId().isEmpty() && !ot.getInvoiceId().isEmpty() && ot.getReceiptId() == null &&
                                ot.getTotalAmountOwed().equals(BigDecimal.valueOf(27810)) &&
                                ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                                ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(81810)) &&
                                ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
                                ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM")) ||
                        (!ot.getId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.CREDIT) &&
                                !ot.getOccupationId().isEmpty() && ot.getInvoiceId() == null &&
                                !ot.getReceiptId().isEmpty() &&
                                ot.getTotalAmountOwed().equals(BigDecimal.ZERO) &&
                                ot.getTotalAmountPaid().equals(BigDecimal.valueOf(81810)) &&
                                ot.getTotalAmountCarriedForward().equals(BigDecimal.ZERO) &&
                                ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
                                ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM")))
                .expectNextMatches(ot -> (!ot.getId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                        !ot.getOccupationId().isEmpty() && !ot.getInvoiceId().isEmpty() && ot.getReceiptId() == null &&
                        ot.getTotalAmountOwed().equals(BigDecimal.valueOf(54000)) &&
                        ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(54000)) &&
                        ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
                        ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM")) ||
                        (!ot.getId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                                !ot.getOccupationId().isEmpty() && !ot.getInvoiceId().isEmpty() && ot.getReceiptId() == null &&
                                ot.getTotalAmountOwed().equals(BigDecimal.valueOf(27810)) &&
                                ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                                ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(81810)) &&
                                ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
                                ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM")) ||
                        (!ot.getId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.CREDIT) &&
                                !ot.getOccupationId().isEmpty() && ot.getInvoiceId() == null &&
                                !ot.getReceiptId().isEmpty() &&
                                ot.getTotalAmountOwed().equals(BigDecimal.ZERO) &&
                                ot.getTotalAmountPaid().equals(BigDecimal.valueOf(81810)) &&
                                ot.getTotalAmountCarriedForward().equals(BigDecimal.ZERO) &&
                                ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
                                ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM")))
                .expectNextMatches(ot -> (!ot.getId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                        !ot.getOccupationId().isEmpty() && !ot.getInvoiceId().isEmpty() && ot.getReceiptId() == null &&
                        ot.getTotalAmountOwed().equals(BigDecimal.valueOf(54000)) &&
                        ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                        ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(54000)) &&
                        ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
                        ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM")) ||
                        (!ot.getId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.DEBIT) &&
                                !ot.getOccupationId().isEmpty() && !ot.getInvoiceId().isEmpty() && ot.getReceiptId() == null &&
                                ot.getTotalAmountOwed().equals(BigDecimal.valueOf(27810)) &&
                                ot.getTotalAmountPaid().equals(BigDecimal.ZERO) &&
                                ot.getTotalAmountCarriedForward().equals(BigDecimal.valueOf(81810)) &&
                                ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
                                ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM")) ||
                        (!ot.getId().isEmpty() && ot.getType().equals(OccupationTransaction.Type.CREDIT) &&
                                !ot.getOccupationId().isEmpty() && ot.getInvoiceId() == null &&
                                !ot.getReceiptId().isEmpty() &&
                                ot.getTotalAmountOwed().equals(BigDecimal.ZERO) &&
                                ot.getTotalAmountPaid().equals(BigDecimal.valueOf(81810)) &&
                                ot.getTotalAmountCarriedForward().equals(BigDecimal.ZERO) &&
                                ot.getCreatedOn() != null && ot.getCreatedBy().equals("SYSTEM") &&
                                ot.getModifiedOn() != null && ot.getModifiedBy().equals("SYSTEM")))
                .verifyComplete();

    }

    @NotNull
    private static Occupation getOccupation() {
        String occupationId = "1";
        String tenantId= "1";
        String unitId = "1";
        var occupation = new Occupation.OccupationBuilder()
                .tenantId(tenantId)
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        occupation.setStatus(Occupation.Status.CURRENT);
        occupation.setId(occupationId);
        return occupation;
    }

    @Test
    void findAll_returnsOccupations_whenSuccessful() {
        Occupation occupation = getOccupation();
        // when
        Flux<Occupation> findOccupation = occupationRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(occupationService.findAll(Occupation.Status.CURRENT, null,
                        "1", "1", OrderType.ASC));
        // then
        StepVerifier
                .create(findOccupation)
                .expectNextMatches(o -> o.getStatus().equals(Occupation.Status.CURRENT) && o.getId().equals("1"))
                .verifyComplete();
    }

    @Test
    void findAll_returnsEmpty_whenNoOccupationFound() {
        // when
        Flux<Occupation> findOccupationNotExist = occupationService.findAll(Occupation.Status.CURRENT,
                null, "13", "14",  OrderType.ASC);
        // then
        StepVerifier
                .create(findOccupationNotExist)
                .expectComplete()
                .verify();
    }

    @Test
    void findAll_returnsOccupationsInDesc_whenSuccessful() {
        // given
        Occupation occupation = getOccupation();
        String unitId = "2";
        var occupation2 = new Occupation.OccupationBuilder()
                .tenantId("2")
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        occupation2.setId("2");
        // when
        Flux<Occupation> findAll = reset()
                .then(occupationRepository.deleteAll())
                .doOnSuccess($ -> System.out.println("---- Deleted all Occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationRepository.save(occupation2))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(occupationService.findAll(null, null,
                        null, null, OrderType.DESC));
        // then
        StepVerifier
                .create(findAll)
                .expectNextMatches(o -> o.getId().equals("2"))
                .expectNextMatches(o -> o.getId().equals("1"))
                .verifyComplete();
    }

    @Test
    void deleteById_returnsTrue_whenOccupationStatusIsVacated() {
        // given
        String occupationId = "1";
        String unitId = "1";
        String tenantId= "1";
        var occupation = new Occupation.OccupationBuilder()
                .tenantId(tenantId)
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        occupation.setId(occupationId);
        occupation.setStatus(Occupation.Status.VACATED);
        // when
        Mono<Boolean> createThenDelete = occupationRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.deleteById(occupationId));
        // then
        StepVerifier
                .create(createThenDelete)
                .expectNext(true)
                .verifyComplete();
    }
    
    @Test
    void deleteById_returnsCustomBadRequestException_whenOccupationIsNotVacated() {
    	// given
    	var occupationId = "1";
        Status status = Occupation.Status.CURRENT;
        Status statusRequired = Occupation.Status.VACATED;
        var occupation = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(LocalDate.now())
                .unitId("1")
                .build();
        occupation.setId(occupationId);
		occupation.setStatus(status);
        // when
        Mono<Boolean> deleteCurrent = occupationRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationService.deleteById(occupationId));
        // then
        StepVerifier
                .create(deleteCurrent)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Occupation can not be deleted. Status is not %s.".formatted(statusRequired.getState())))
                .verify();
    }
    
    @Test
    void deleteById_returnsCustomNotFoundException_whenOccupationIdDoesNotExist() {
        // when
        Mono<Boolean> deleteThatDoesNotExist = occupationService.deleteById("3090");
        // then
        StepVerifier
                .create(deleteThatDoesNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Occupation with id 3090 does not exist!"))
                .verify();
    }
}