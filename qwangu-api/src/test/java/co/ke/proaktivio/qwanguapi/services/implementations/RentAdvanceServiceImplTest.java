package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.RentAdvance;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.RentAdvanceDto;
import co.ke.proaktivio.qwanguapi.pojos.UpdateRentAdvanceDto;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
import co.ke.proaktivio.qwanguapi.repositories.PaymentRepository;
import co.ke.proaktivio.qwanguapi.repositories.RentAdvanceRepository;
import co.ke.proaktivio.qwanguapi.services.RentAdvanceService;
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
import java.util.List;
import java.util.Optional;

@Testcontainers
@SpringBootTest
class RentAdvanceServiceImplTest {
    @Autowired
    private RentAdvanceService rentAdvanceService;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private RentAdvanceRepository rentAdvanceRepository;

    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName
            .parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    private final Payment payment = new Payment(null, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W67",
            "Pay Bill", LocalDateTime.now(), BigDecimal.valueOf(20000), "600638",
            "TE34", "", "49197.00", "", "254708374147",
            "John", "", "Doe");
    private final Occupation occupation = new Occupation.OccupationBuilder()
            .tenantId("1")
            .unitId("1")
            .startDate(LocalDate.now())
            .unitId("1")
            .build();
    private final RentAdvance advance = new RentAdvance(null, RentAdvance.Status.HOLDING, null, null,
            null, null, null, null, null, null);

    Mono<Void> deleteAll() {
        return paymentRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Payments!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Occupations!"))
                .then(rentAdvanceRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all Rent advances!"));
    }

    @Test
    void create() {
        // given
        var dto = new RentAdvanceDto(RentAdvance.Status.HOLDING, null, null);

        var payment2 = new Payment(null, Payment.Status.NEW, Payment.Type.MPESA_PAY_BILL, "RKTQDM7W77",
                "Pay Bill", LocalDateTime.now(), BigDecimal.valueOf(20000), "600638",
                "TE35", "", "49197.00", "", "254708374147",
                "John", "", "Doe");
        var dto2 = new RentAdvanceDto(RentAdvance.Status.HOLDING, null, null);

        // when
        Mono<RentAdvance> createAdvance = deleteAll()
                .then(occupationRepository.save(occupation))
                .doOnSuccess(u -> System.out.println("---- Created: " + u))
                .flatMap(occ -> paymentRepository.save(payment)
                        .doOnSuccess(u -> System.out.println("---- Created: " + u))
                        .flatMap(pay -> {
                            dto.setOccupationId(occ.getId());
                            dto.setPaymentId(pay.getId());
                            return rentAdvanceService.create(dto);
                        }))
                .doOnSuccess(u -> System.out.println("---- Created: " + u));
        // then
        StepVerifier
                .create(createAdvance)
                .expectNextMatches(advance -> advance.getStatus().equals(RentAdvance.Status.HOLDING) &&
                        !advance.getOccupationId().isEmpty() && !advance.getPaymentId().isEmpty())
                .verifyComplete();

        // when
        Mono<RentAdvance> createWithoutOccupation = paymentRepository.save(payment2)
                .doOnSuccess(u -> System.out.println("---- Created: " + u))
                .flatMap(pay -> {
                    dto2.setPaymentId(pay.getId());
                    return rentAdvanceService.create(dto2);
                })
                .doOnSuccess(u -> System.out.println("---- Created: " + u));
        // then
        StepVerifier
                .create(createWithoutOccupation)
                .expectNextMatches(advance -> advance.getStatus().equals(RentAdvance.Status.HOLDING) &&
                        !advance.getPaymentId().isEmpty())
                .verifyComplete();

        // when
        Flux<Payment> findAllPayments = paymentRepository.findAll()
                .doOnNext(u -> System.out.println("---- Found: " + u));
        // then
        StepVerifier
                .create(findAllPayments)
                .expectNextMatches(p -> p.getStatus().equals(Payment.Status.PROCESSED))
                .expectNextMatches(p -> p.getStatus().equals(Payment.Status.PROCESSED))
                .verifyComplete();
    }

    @Test
    void update() {
        // given
        var dto = new UpdateRentAdvanceDto(RentAdvance.Status.RELEASED, "Details!", LocalDate.now());

        // when
        Mono<RentAdvance> updateAdvance = deleteAll()
                .then(occupationRepository.save(occupation))
                .doOnSuccess(u -> System.out.println("---- Created: " + u))
                .flatMap(occ -> paymentRepository.save(payment)
                        .doOnSuccess(u -> System.out.println("---- Created: " + u))
                        .flatMap(pay -> {
                            advance.setOccupationId(occ.getId());
                            advance.setPaymentId(pay.getId());
                            return rentAdvanceRepository.save(advance)
                                    .doOnSuccess(u -> System.out.println("---- Created: " + u))
                                    .flatMap(rentAdvance -> rentAdvanceService.update(rentAdvance.getId(), dto));
                        }))
                .doOnSuccess(u -> System.out.println("---- Updated: " + u));
        // then
        StepVerifier
                .create(updateAdvance)
                .expectNextMatches(a -> a.getStatus().equals(RentAdvance.Status.RELEASED) &&
                        a.getReturnDetails().equals("Details!") && a.getReturnedOn() != null)
                .verifyComplete();
    }

    @Test
    void findPaginated() {
        // given
        var advance2 = new RentAdvance(null, RentAdvance.Status.HOLDING, null, null,
                null, null, null, null, null, null);

        // when
        Flux<RentAdvance> findAll = deleteAll()
                .thenMany(rentAdvanceRepository.saveAll(List.of(advance, advance2)))
                .doOnNext(u -> System.out.println("---- Created: " + u))
                .thenMany(rentAdvanceService.findPaginated(Optional.empty(), Optional.empty(), Optional.empty(),
                        Optional.empty(), 1, 10, OrderType.DESC))
                .doOnNext(u -> System.out.println("---- Found: " + u));
        // then
        StepVerifier
                .create(findAll)
                .expectNextMatches(r -> !r.getId().isEmpty() && r.getStatus().equals(RentAdvance.Status.HOLDING))
                .expectNextMatches(r -> !r.getId().isEmpty() && r.getStatus().equals(RentAdvance.Status.HOLDING))
                .verifyComplete();
    }

    @Test
    void deleteById() {
        Mono<Boolean> delete = deleteAll()
                .then(rentAdvanceRepository.save(advance))
                .doOnSuccess(r -> System.out.println("---- Created: " + r))
                .flatMap(rentAdvance -> rentAdvanceService.deleteById(rentAdvance.getId()))
                .doOnSuccess(r -> System.out.println("---- Deleted successfully: " + r));
        // then
        StepVerifier
                .create(delete)
                .expectNextMatches(isTrue -> isTrue)
                .verifyComplete();
        // then
        StepVerifier
                .create(rentAdvanceService.deleteById("23456"))
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("RentAdvance with id 23456 does not exist!"))
                .verify();
    }
}