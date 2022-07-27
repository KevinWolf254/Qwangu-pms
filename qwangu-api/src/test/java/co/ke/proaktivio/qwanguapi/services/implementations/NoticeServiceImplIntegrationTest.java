package co.ke.proaktivio.qwanguapi.services.implementations;

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
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Optional;

@Testcontainers
@DataMongoTest(excludeAutoConfiguration = EmbeddedMongoAutoConfiguration.class)
@ComponentScan(basePackages = {"co.ke.proaktivio.qwanguapi.*"})
class NoticeServiceImplIntegrationTest {
    @Autowired
    private NoticeRepository noticeRepository;
    @Autowired
    private OccupationRepository occupationRepository;
    @Autowired
    private NoticeServiceImpl noticeService;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    private final LocalDateTime now = LocalDateTime.now();
    private final Occupation occupation = new Occupation("1", Occupation.Status.CURRENT, LocalDateTime.now(), null, "1", "1", LocalDateTime.now(), null);
    private final Occupation occupationMoved = new Occupation("2", Occupation.Status.MOVED, LocalDateTime.now(), null, "2", "2", LocalDateTime.now(), null);
    private final Notice notice = new Notice("1", Notice.Status.AWAITING_EXIT, now, now.plusDays(40), now, null, "1");
    private final Notice noticeWithOccupationMoved = new Notice("2", Notice.Status.AWAITING_EXIT, now, now.plusDays(40), now, null, "2");

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Test
    void create() {
        // given
        LocalDateTime now = LocalDateTime.now();
        var dto = new CreateNoticeDto(now, now.plusDays(30), "1");
        var dtoOccupationDoesNotExist = new CreateNoticeDto(now, now.plusDays(30), "3000");
        var dtoOccupationNotActive = new CreateNoticeDto(now, now.plusDays(30), "2");
        // when
        Mono<Notice> notice = noticeRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all notices!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationRepository.save(occupationMoved))
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
        LocalDateTime now = LocalDateTime.now();
        var dto = new UpdateNoticeDto(Notice.Status.AWAITING_EXIT, now, now.plusDays(35));
        var noticeWithOccupationDoesNotExist = new Notice("3000", Notice.Status.AWAITING_EXIT, now, now.plusDays(40), now, null, "3000");
        // when
        Mono<Notice> updateNotice = noticeRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all notices!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationRepository.save(occupationMoved))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeRepository.save(notice))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeService.update("1", dto));
        // then
        StepVerifier
                .create(updateNotice)
                .expectNextMatches(n -> n.getModified() != null)
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
//
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
        // when
        Flux<Notice> find = noticeRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all notices!"))
                .then(occupationRepository.deleteAll())
                .doOnSuccess(t -> System.out.println("---- Deleted all occupations!"))
                .then(occupationRepository.save(occupation))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(occupationRepository.save(occupationMoved))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(noticeRepository.save(notice))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(noticeService.findPaginated(Optional.of("1"), Optional.empty(), Optional.empty(),
                        1, 10, OrderType.ASC));
        // then
        StepVerifier
                .create(find)
                .expectNextMatches(n -> n.getStatus().equals(Notice.Status.AWAITING_EXIT) && n.getId().equals("1"))
                .verifyComplete();

        // when
        Flux<Notice> findAll = noticeRepository.save(noticeWithOccupationMoved)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(noticeService.findPaginated(Optional.empty(), Optional.empty(), Optional.empty(),
                        1, 10, OrderType.DESC));
        // then
        StepVerifier
                .create(findAll)
                .expectNextMatches(o -> o.getId().equals("2"))
                .expectNextMatches(o -> o.getId().equals("1"))
                .verifyComplete();

        // when
        Flux<Notice> findThrowsCustomNotFoundException = noticeService.findPaginated(Optional.of("3000"), Optional.empty(), Optional.of("1"),
                1, 10, OrderType.ASC);
        // then
        StepVerifier
                .create(findThrowsCustomNotFoundException)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Notices were not found!"))
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