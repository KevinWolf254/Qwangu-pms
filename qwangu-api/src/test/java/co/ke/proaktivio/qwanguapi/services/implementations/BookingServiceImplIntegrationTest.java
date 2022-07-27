package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.repositories.*;
import co.ke.proaktivio.qwanguapi.pojos.CreateBookingDto;
import org.junit.Before;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Testcontainers
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
class BookingServiceImplIntegrationTest {
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private ReactiveMongoTemplate template;
    @Autowired
    private BookingServiceImpl bookingService;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    private final LocalDateTime now = LocalDateTime.now();
    private final Payment payment;

    {
        payment = new Payment("1", Payment.Type.MPESA_PAY_BILL, Payment.Status.RENT_NEW, "RKTQDM7W6S",
                "Pay Bill", now, BigDecimal.valueOf(20000), "600638",
                "T903", null, "49197.00", "", "254708374149",
                "John", "", "Doe", now, null);
    }

    private final Unit unit = new Unit("1", Unit.Status.OCCUPIED, "T903", Unit.Type.APARTMENT_UNIT, Unit.Identifier.A,
            1, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, now, null, "1");
    private final Occupation occupation = new Occupation("1", Occupation.Status.CURRENT, now, null, "1", "1", now, null);
    private final Notice notice = new Notice("1", Notice.Status.AWAITING_EXIT, now, now.plusDays(15), now, null, "1");

    @Test
    void create() {
        // given
        var dto = new CreateBookingDto(now.plusDays(16), "1", "1");
        // when
        Mono<Booking> booking = paymentRepository.save(payment)
                .doOnSuccess(p -> System.out.println("---- Saved " + p))
                .then(unitRepository.save(unit))
                .doOnSuccess(u -> System.out.println("---- Saved " + u))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(o -> System.out.println("---- Saved " + o))
                .then(noticeRepository.save(notice))
                .doOnSuccess(n -> System.out.println("---- Saved " + n))
                .then(bookingService.create(dto));
        //then
        StepVerifier
                .create(booking)
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void update() {
    }

    @Test
    void findPaginated() {
    }

    @Test
    void deleteById() {
    }
}