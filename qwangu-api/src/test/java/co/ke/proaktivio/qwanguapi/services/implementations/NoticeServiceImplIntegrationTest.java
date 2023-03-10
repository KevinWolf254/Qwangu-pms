package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.CreateNoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateNoticeDto;
import co.ke.proaktivio.qwanguapi.repositories.NoticeRepository;
import co.ke.proaktivio.qwanguapi.repositories.OccupationRepository;
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

import java.time.LocalDate;
import java.util.Optional;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class NoticeServiceImplIntegrationTest {
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private NoticeServiceImpl noticeService;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    private final LocalDate now = LocalDate.now();
    private final LocalDate today = LocalDate.now();
    private final Notice notice = new Notice("1", Notice.Status.ACTIVE, now, today.plusDays(40), "1", null,
            null, null, null);
    private final Notice noticeWithOccupationMoved = new Notice("2", Notice.Status.ACTIVE, now, today.plusDays(40), "2",
            null, null, null, null);

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Test
    void create() {
        // given
        var occupationId = "1";
        LocalDate now = LocalDate.now();
        var dto = new CreateNoticeDto(now, today.plusDays(30), "1");
        var dtoOccupationDoesNotExist = new CreateNoticeDto(now, today.plusDays(30), "3000");
        var dtoOccupationNotActive = new CreateNoticeDto(now, today.plusDays(30), "2");
        var occupation = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(LocalDate.now())
                .unitId("1")
                .build();
        occupation.setId(occupationId);
        occupation.setStatus(Occupation.Status.CURRENT);
        var occupationVacated = new Occupation.OccupationBuilder()
                .tenantId("2")
                .startDate(LocalDate.now())
                .unitId("2")
                .build();
        occupationVacated.setId("2");
        occupationVacated.setStatus(Occupation.Status.VACATED);
        // when
        Mono<Notice> notice = noticeRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all notices!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationRepository.save(occupationVacated))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeService.create(dto));
        // then
        StepVerifier
                .create(notice)
                .expectNextCount(1)
                .verifyComplete();

        // when
        Mono<Notice> noticeThrowsCustomNotFoundException = noticeService.create(dtoOccupationDoesNotExist);
        // then
        StepVerifier
                .create(noticeThrowsCustomNotFoundException)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Occupation with id 3000 does not exist!"))
                .verify();

        // when
        Mono<Notice> noticeThrowsCustomBadRequestException = noticeService.create(dtoOccupationNotActive);
        // then
        StepVerifier
                .create(noticeThrowsCustomBadRequestException)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Can not create notice of occupation that is not active!"))
                .verify();
    }

    @Test
    void update() {
        // given
        LocalDate now = LocalDate.now();
        var dto = new UpdateNoticeDto(now, today.plusDays(35), Notice.Status.ACTIVE);
        var noticeWithOccupationDoesNotExist = new Notice("3000", Notice.Status.ACTIVE, now, today.plusDays(40),
                "3000", null, null, null, null);
        var occupation = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(LocalDate.now())
                .unitId("1")
                .build();
        occupation.setId("1");
        occupation.setStatus(Occupation.Status.CURRENT);
        var occupationVacated = new Occupation.OccupationBuilder()
                .tenantId("2")
                .startDate(LocalDate.now())
                .unitId("2")
                .build();
        occupationVacated.setId("2");
        occupationVacated.setStatus(Occupation.Status.VACATED);
        // when
        Mono<Notice> updateNotice = noticeRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all notices!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationRepository.save(occupationVacated))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeRepository.save(notice))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeService.update("1", dto));
        // then
        StepVerifier
                .create(updateNotice)
                .expectNextMatches(n -> n.getModifiedOn() != null)
                .verifyComplete();

        // when
        Mono<Notice> updateThrowsCustomNotFoundException = noticeService.update("3000", dto);
        // then
        StepVerifier
                .create(updateThrowsCustomNotFoundException)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Notice with id 3000 does not exist!"))
                .verify();
        // when
        Mono<Notice> updateNoticeWithOccupationDoesNotExist = noticeRepository.save(noticeWithOccupationDoesNotExist)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeService.update("3000", dto));
        // then
        StepVerifier
                .create(updateNoticeWithOccupationDoesNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Occupation with id 3000 does not exist!"))
                .verify();

        // when
        Mono<Notice> updateNoticeWithOccupationNotActive = noticeRepository.save(noticeWithOccupationMoved)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeService.update("2", dto));
        // then
        StepVerifier
                .create(updateNoticeWithOccupationNotActive)
                .expectErrorMatches(e -> e instanceof CustomBadRequestException &&
                        e.getMessage().equals("Can not update notice of occupation that is not active!"))
                .verify();
    }

    @Test
    void findPaginated() {
        // given
        var occupation = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(LocalDate.now())
                .unitId("1")
                .build();
        occupation.setId("1");
        occupation.setStatus(Occupation.Status.CURRENT);
        var occupationVacated = new Occupation.OccupationBuilder()
                .tenantId("2")
                .startDate(LocalDate.now())
                .unitId("2")
                .build();
        occupationVacated.setId("2");
        occupationVacated.setStatus(Occupation.Status.VACATED);
        // when
        Flux<Notice> find = noticeRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all notices!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationRepository.save(occupationVacated))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeRepository.save(notice))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(noticeService.findPaginated(Optional.of("1"), Optional.empty(),
                        Optional.empty(), OrderType.ASC));
        // then
        StepVerifier
                .create(find)
                .expectNextMatches(n -> n.getStatus().equals(Notice.Status.ACTIVE) && n.getId().equals("1"))
                .verifyComplete();

        // when
        Flux<Notice> findAll = noticeRepository.save(noticeWithOccupationMoved)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(noticeService.findPaginated(Optional.empty(), Optional.empty(), Optional.empty(),
                        OrderType.DESC));
        // then
        StepVerifier
                .create(findAll)
                .expectNextMatches(o -> o.getId().equals("2"))
                .expectNextMatches(o -> o.getId().equals("1"))
                .verifyComplete();

        // when
        Flux<Notice> findThrowsCustomNotFoundException = noticeService.findPaginated(Optional.of("3000"),
                Optional.empty(), Optional.of("1"), OrderType.ASC);
        // then
        StepVerifier
                .create(findThrowsCustomNotFoundException)
                .expectComplete()
                .verify();
    }

    @Test
    void deleteById() {
        // then
        Mono<Boolean> createThenDelete = noticeRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all notices!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all occupations!"))
                .then(noticeRepository.save(notice))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeService.deleteById("1"));
        // then
        StepVerifier
                .create(createThenDelete)
                .expectNext(true)
                .verifyComplete();

        // when
        Mono<Boolean> deleteThatDoesNotExist = noticeService.deleteById("3090");
        // then
        StepVerifier
                .create(deleteThatDoesNotExist)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Notice with id 3090 does not exist!"))
                .verify();
    }
}