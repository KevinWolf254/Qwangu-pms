package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateBookingDto;
import co.ke.proaktivio.qwanguapi.repositories.*;
import co.ke.proaktivio.qwanguapi.pojos.CreateBookingDto;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

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
    private final LocalDate today = LocalDate.now();
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
    private final Notice notice = new Notice("1", Notice.Status.AWAITING_EXIT, now, today.plusDays(15), now, null, "1");
    private final Booking booking = new Booking("1", Booking.Status.BOOKED, today, now, null, "1", "1");

    Supplier<Mono<Notice>> setUp = () -> bookingRepository.deleteAll()
            .doOnSuccess($ -> System.out.println("---- Deleted bookings!"))
            .then(paymentRepository.deleteAll())
            .doOnSuccess($ -> System.out.println("---- Deleted payments!"))
            .then(unitRepository.deleteAll())
            .doOnSuccess($ -> System.out.println("---- Deleted units!"))
            .then(occupationRepository.deleteAll())
            .doOnSuccess($ -> System.out.println("---- Deleted occupations!"))
            .then(noticeRepository.deleteAll())
            .doOnSuccess($ -> System.out.println("---- Deleted notices!"))
            .then(paymentRepository.save(payment))
            .doOnSuccess(p -> System.out.println("---- Saved " + p))
            .then(unitRepository.save(unit))
            .doOnSuccess(u -> System.out.println("---- Saved " + u))
            .then(occupationRepository.save(occupation))
            .doOnSuccess(o -> System.out.println("---- Saved " + o))
            .then(noticeRepository.save(notice))
            .doOnSuccess(n -> System.out.println("---- Saved " + n));

    @Test
    void create() {
        // given
        var dto = new CreateBookingDto(today.plusDays(16), "1", "1");
        // when
        Mono<Booking> booking = setUp.get()
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
        // given
        var dto = new UpdateBookingDto(Booking.Status.BOOKED, today.plusDays(16));
        // when

        Mono<Booking> update = setUp.get()
                .then(bookingRepository.save(booking))
                .doOnSuccess(p -> System.out.println("---- Saved " + p))
                .then(bookingService.update("1", dto));
        // then
        StepVerifier
                .create(update)
                .expectNextMatches(b -> b.getOccupation().isEqual(today.plusDays(16)))
                .verifyComplete();
    }

    @Test
    void findPaginated() {
        // when
        Flux<Booking> find = setUp.get()
                .then(bookingRepository.save(booking))
                .doOnSuccess(p -> System.out.println("---- Saved " + p))
                .thenMany(bookingService.findPaginated(Optional.of("1"), Optional.empty(), Optional.empty(),
                        1, 10, OrderType.ASC));
        // then
        StepVerifier
                .create(find)
                .expectNextCount(1)
                .verifyComplete();
        // then
        StepVerifier
                .create(bookingService.findPaginated(Optional.of("14030"), Optional.empty(), Optional.empty(),
                        1, 10, OrderType.ASC))
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Bookings were not found!"))
                .verify();
    }

    @Test
    void deleteById() {
        // given
        // when
        Mono<Boolean> createUnitThenDelete = bookingRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Bookings!"))
                .then(bookingRepository.save(booking))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(bookingService.deleteById("1"));
        // then
        StepVerifier
                .create(createUnitThenDelete)
                .expectNext(true)
                .verifyComplete();

        // when
        Mono<Boolean> deleteUnitThatDoesNotExist = bookingService.deleteById("3090");
        // then
        StepVerifier
                .create(deleteUnitThatDoesNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Booking with id 3090 does not exist!"))
                .verify();
    }
}