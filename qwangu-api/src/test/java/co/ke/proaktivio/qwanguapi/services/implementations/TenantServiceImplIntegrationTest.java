package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.configs.BootstrapConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Tenant;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.TenantDto;
import co.ke.proaktivio.qwanguapi.repositories.TenantRepository;
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

import java.time.LocalDateTime;
import java.util.Optional;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class TenantServiceImplIntegrationTest {
    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private TenantServiceImpl tenantService;
    @MockBean
    private BootstrapConfig bootstrapConfig;
    @MockBean
    private GlobalErrorWebExceptionHandler globalErrorWebExceptionHandler;
    @Container
    private static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer(DockerImageName.parse("mongo:latest"));

    @DynamicPropertySource
    public static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO_DB_CONTAINER::getReplicaSetUrl);
    }

    @Test
    void create() {
        // given
        var dto = new TenantDto("John", "middle", "Doe", "0700000000", "person@gmail.com");
        var dtoWithDuplicate = new TenantDto("John", "middle", "Doe", "0700000000", "person@gmail.com");

        // when
        Mono<Tenant> createTenant = tenantRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"))
                .then(tenantService.create(dto))
                .doOnSuccess(a -> System.out.println("---- Saved " + a));

        // then
        StepVerifier
                .create(createTenant)
                .expectNextMatches(t -> !t.getId().isEmpty() && t.getMobileNumber().equals("0700000000") &&
                        t.getEmailAddress().equals("person@gmail.com"))
                .verifyComplete();

        // when
        Mono<Tenant> createTenantDuplicate = tenantService.create(dtoWithDuplicate);
        // then
        StepVerifier
                .create(createTenantDuplicate)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equals("Tenant already exists!"))
                .verify();
    }

    @Test
    void update() {
        // given
        String id = "1";
        String id2 = "2";
        var tenant = new Tenant(id, "John", "middle", "Doe", "0700000000", "person@gmail.com",
                LocalDateTime.now(), null, null, null);
        var tenant2 = new Tenant(id2, "John", "middle", "Doe", "0700000002", "person2@gmail.com",
                LocalDateTime.now(), null, null, null);
        var dto = new TenantDto("Jane", "Doe", "Doe1", "0700000000", "person@gmail.com");
        var dto2 = new TenantDto("Peter", "Doe", "Doe", "0700000002", "person2@gmail.com");
        var dto3 = new TenantDto("Peter", "Doe", "Doe", "0700000003", "person3@gmail.com");

        // when
        Mono<Tenant> updateTenant = tenantRepository
                .deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"))
                .then(tenantRepository.save(tenant))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(tenantService.update(id, dto))
                .doOnSuccess(a -> System.out.println("---- Updated " + a));

        // then
        StepVerifier
                .create(updateTenant)
                .expectNextMatches(t -> t.getFirstName().equals("Jane") &&
                        t.getMiddleName().equals("Doe") && t.getSurname().equals("Doe1") &&
                        t.getEmailAddress().equals("person@gmail.com") && t.getMobileNumber().equals("0700000000"))
                .verifyComplete();

        // when
        Mono<Tenant> updateDuplicate = tenantRepository
                .save(tenant2)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(tenantService.update(id, dto2));
        // then
        StepVerifier
                .create(updateDuplicate)
                .expectErrorMatches(e -> e instanceof CustomAlreadyExistsException &&
                        e.getMessage().equals("Tenant already exists!"))
                .verify();


        // when
        Mono<Tenant> updateNonExistantId = tenantService.update("39202", dto3);
        // then
        StepVerifier
                .create(updateNonExistantId)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Tenant with id 39202 does not exist!"))
                .verify();
    }

    @Test
    void findPaginated() {
        // given
        String id = "1";
        String mobileNumber = "0700000000";
        String emailAddress = "person@gmail.com";
        var tenant = new Tenant(id, "John", "middle", "Doe", mobileNumber, emailAddress,
                LocalDateTime.now(), null, null, null);
        var tenant2 = new Tenant("2", "John", "middle", "Doe", "0700000002",
                "person2@gmail.com", LocalDateTime.now(), null, null, null);

        // when
        Flux<Tenant> findTenant = tenantRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"))
                .then(tenantRepository.save(tenant))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(tenantService.findPaginated(Optional.of(mobileNumber), Optional.of(emailAddress),
                        OrderType.ASC))
                .doOnNext(a -> System.out.println("---- Found " + a));
        // then
        StepVerifier
                .create(findTenant)
                .expectNextCount(1)
                .verifyComplete();

        // when
        Flux<Tenant> findNotExist = tenantService
                .findPaginated(Optional.of("0700000001"), Optional.of("person2@gmail.com"),
                        OrderType.ASC);
        // then
        StepVerifier
                .create(findNotExist)
                .expectComplete()
                .verify();

        // when
        Flux<Tenant> findPaginatedDesc = tenantRepository.save(tenant2)
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .thenMany(tenantService.findPaginated( Optional.empty(), Optional.empty(),
                        OrderType.DESC))
                .doOnNext(a -> System.out.println("---- Found " + a));
        // then
        StepVerifier
                .create(findPaginatedDesc)
                .expectNextMatches(t  -> t.getId().equals("2"))
                .expectNextMatches(t -> t.getId().equals("1"))
                .verifyComplete();
    }


    @Test
    void deleteById() {
        // given
        String id = "1";
        String mobileNumber = "0700000000";
        String emailAddress = "person@gmail.com";
        var tenant = new Tenant(id, "John", "middle", "Doe", mobileNumber, emailAddress,
                LocalDateTime.now(), null, null, null);

        // when
        Mono<Boolean> createUnitThenDelete = tenantRepository.deleteAll()
                .doOnSuccess(t -> System.out.println("---- Deleted all Tenants!"))
                .then(tenantRepository.save(tenant))
                .doOnSuccess(a -> System.out.println("---- Saved " + a))
                .then(tenantService.deleteById(id));
        // then
        StepVerifier
                .create(createUnitThenDelete)
                .expectNext(true)
                .verifyComplete();

        // when
        Mono<Boolean> deleteThatDoesNotExist = tenantService.deleteById("3090");
        // then
        StepVerifier
                .create(deleteThatDoesNotExist)
                .expectComplete()
                .verify();
    }
}