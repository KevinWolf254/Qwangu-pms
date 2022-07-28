package co.ke.proaktivio.qwanguapi.jobs;

import co.ke.proaktivio.qwanguapi.models.*;
import co.ke.proaktivio.qwanguapi.repositories.*;
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
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
class NoticeJobManagerIntegrationTest {
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private UnitRepository unitRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private NoticeJobManager noticeJobManager;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }
    private final LocalDateTime now = LocalDateTime.now();
    private final LocalDate today = LocalDate.now();
    private final Unit unit = new Unit("1", Unit.Status.OCCUPIED, "TE34", Unit.Type.APARTMENT_UNIT, Unit.Identifier.A,
            2, 2, 1, 2, Unit.Currency.KES, 27000, 510, 300, now, null, "1");
    private final Occupation occupation = new Occupation("1", Occupation.Status.CURRENT, LocalDateTime.now(), null, "1", "1", now, null);
    private final Tenant tenant = new Tenant("1", "John", "middle", "Doe", "0700000000", "person@gmail.com", now, null);
    private final Notice notice = new Notice("1", Notice.Status.AWAITING_EXIT, now.minusDays(30), today.minusDays(1), now, null, "1");
    private final Booking booking = new Booking("1", Booking.Status.BOOKED, today, now.minusDays(5), null, "1", "1");

    @Test
    void vacate() {
        // when
        Flux<Notice> vacate = unitRepository.save(unit)
                .doOnSuccess(u -> System.out.println("---- Saved: " +u))
                .then(tenantRepository.save(tenant))
                .doOnSuccess(t -> System.out.println("---- Saved: " +t))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(o -> System.out.println("---- Saved: " +o))
                .then(noticeRepository.save(notice))
                .doOnSuccess(n -> System.out.println("---- Saved: " +n))
                .then(bookingRepository.save(booking))
                .doOnSuccess(b -> System.out.println("---- Saved: " +b))
                .thenMany(noticeJobManager.vacate());
        //then
        StepVerifier
                .create(vacate)
                .expectNextMatches(n -> n.getStatus().equals(Notice.Status.EXITED))
                .verifyComplete();
    }
}