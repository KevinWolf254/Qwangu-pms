package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.BookingRefund;
import co.ke.proaktivio.qwanguapi.models.Receivable;
import co.ke.proaktivio.qwanguapi.pojos.BookingRefundDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.BookingRefundRepository;
import co.ke.proaktivio.qwanguapi.repositories.ReceivableRepository;
import co.ke.proaktivio.qwanguapi.services.BookingRefundService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
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
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
class BookingRefundServiceImplIntegrationTest {
    @Autowired
    private BookingRefundService bookingRefundService;
    @Autowired
    private ReceivableRepository receivableRepository;
    @Autowired
    private BookingRefundRepository bookingRefundRepository;

    private final BookingRefund bookingRefund = new BookingRefund(null, BigDecimal.valueOf(10000), "Details!", "1",
            LocalDateTime.now(), null);

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    Mono<Void> deleteAll() {
        return receivableRepository.deleteAll()
                .doOnSuccess(r -> System.out.println("---- Deleted all Receivables!"))
                .then(bookingRefundRepository.deleteAll())
                .doOnSuccess(r -> System.out.println("---- Deleted all Refunds!"));
    }

    @Test
    void create() {
        // given
        Map<String, BigDecimal> otherAmounts = new HashMap<>(1);
        otherAmounts.put("booking", BigDecimal.valueOf(20000));
        var receivable = new Receivable("1", Receivable.Type.BOOKING, LocalDate.now(), null,
                null, null, otherAmounts, LocalDateTime.now(), null);
        var dto = new BookingRefundDto(BigDecimal.valueOf(15000), "QVERDFDEERERT is transactionID",
                "1");

        // when
        Mono<BookingRefund> createBookingRefund = deleteAll()
                .then(receivableRepository.save(receivable))
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
        var receivable2 = new Receivable("2", Receivable.Type.BOOKING, LocalDate.now(), null,
                null, null, otherAmounts, LocalDateTime.now(), null);
        var bookingRefundAmountGTAmountPaid = new BookingRefundDto(BigDecimal.valueOf(25000),
                "QVERDFDEERERT is transactionID", "2");
        // when
        Mono<BookingRefund> refundAmountTooGreat = receivableRepository.save(receivable2)
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
                LocalDateTime.now(), null);

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