package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.BookingRefund;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.pojos.BookingRefundDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.BookingRefundRepository;
import co.ke.proaktivio.qwanguapi.repositories.InvoiceRepository;
import co.ke.proaktivio.qwanguapi.services.BookingRefundService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class BookingRefundServiceImplIntegrationTest {
    @Autowired
    private BookingRefundService bookingRefundService;
    @Autowired
    private InvoiceRepository invoiceRepository;
    @Autowired
    private BookingRefundRepository bookingRefundRepository;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;

    private final BookingRefund bookingRefund = new BookingRefund(null, BigDecimal.valueOf(10000), "Details!", "1",
            LocalDateTime.now(), null, null, null);

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    Mono<Void> deleteAll() {
        return invoiceRepository.deleteAll()
                .doOnSuccess(r -> System.out.println("---- Deleted all Receivables!"))
                .then(bookingRefundRepository.deleteAll())
                .doOnSuccess(r -> System.out.println("---- Deleted all Refunds!"));
    }

    @Test
    void create() {
        // given
        Map<String, BigDecimal> otherAmounts = new HashMap<>(1);
        otherAmounts.put("booking", BigDecimal.valueOf(20000));
        var receivable = new Invoice.InvoiceBuilder()
                .type(Invoice.Type.BOOKING)
                .period(LocalDate.now())
                .otherAmounts(otherAmounts)
                .build();
        receivable.setId("1");
        receivable.setCreatedOn(LocalDateTime.now());
        var dto = new BookingRefundDto(BigDecimal.valueOf(15000), "QVERDFDEERERT is transactionID",
                "1");

        // when
        Mono<BookingRefund> createBookingRefund = deleteAll()
                .then(invoiceRepository.save(receivable))
                .doOnSuccess(r -> System.out.println("---- Created: " + r))
                .then(bookingRefundService.create(dto))
                .doOnSuccess(r -> System.out.println("---- Created: " + r));
        // then
        StepVerifier
                .create(createBookingRefund)
                .expectNextMatches(r -> r.getId() != null && r.getAmount().compareTo(BigDecimal.valueOf(15000)) == 0 &&
                        r.getReceivableId().equals("1"))
                .verifyComplete();

        // given
        var receivable2 = new Invoice.InvoiceBuilder()
                .type(Invoice.Type.BOOKING)
                .period(LocalDate.now())
                .otherAmounts(otherAmounts)
                .build();
        receivable2.setId("2");
        receivable2.setCreatedOn(LocalDateTime.now());
        var bookingRefundAmountGTAmountPaid = new BookingRefundDto(BigDecimal.valueOf(25000),
                "QVERDFDEERERT is transactionID", "2");
        // when
        Mono<BookingRefund> refundAmountTooGreat = invoiceRepository.save(receivable2)
                .doOnSuccess(r -> System.out.println("---- Created: " + r))
                .then(bookingRefundService.create(bookingRefundAmountGTAmountPaid))
                .doOnSuccess(r -> System.out.println("---- Created: " + r));
        // then
        StepVerifier
                .create(refundAmountTooGreat)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Amount to be refunded cannot be greater than the amount paid!"))
                .verify();
    }

    @Test
    void findPaginated() {
        //given
        var bookingRefund2 = new BookingRefund(null, BigDecimal.valueOf(20000), "Details!", "1",
                LocalDateTime.now(), null, null, null);

        // when
        Flux<BookingRefund> findAll = deleteAll()
                .thenMany(bookingRefundRepository.saveAll(List.of(bookingRefund, bookingRefund2)))
                .doOnNext(r -> System.out.println("---- Created: " + r))
                .thenMany(bookingRefundService.findPaginated(Optional.empty(), Optional.empty(),
                        1, 10, OrderType.DESC));
        // then
        StepVerifier
                .create(findAll)
                .expectNextMatches(r -> r.getAmount().equals(BigDecimal.valueOf(20000)))
                .expectNextMatches(r -> r.getAmount().equals(BigDecimal.valueOf(10000)))
                .verifyComplete();
    }

    @Test
    void deleteById() {
        // when
        Mono<Boolean> delete = deleteAll()
                .then(bookingRefundRepository.save(bookingRefund))
                .doOnSuccess(r -> System.out.println("---- Created: " + r))
                .flatMap(refund -> bookingRefundService.deleteById(refund.getId()))
                .doOnSuccess(r -> System.out.println("---- Deleted successfully: " + r));
        // then
        StepVerifier
                .create(delete)
                .expectNextMatches(isTrue -> isTrue)
                .verifyComplete();
        // then
        StepVerifier
                .create(bookingRefundService.deleteById("23456"))
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Refund with id 23456 does not exist!"))
                .verify();

    }
}