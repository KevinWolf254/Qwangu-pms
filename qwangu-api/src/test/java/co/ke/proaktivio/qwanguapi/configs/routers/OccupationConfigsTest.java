package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.OccupationHandler;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {OccupationConfigs.class, OccupationHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class OccupationConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private OccupationService occupationService;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @NotNull
    private static Occupation getOccupation() {
        String tenantId = "1", unitId = "1";
        Occupation occupation = new Occupation.OccupationBuilder()
                .tenantId(tenantId)
                .startDate(LocalDate.now())
                .unitId(unitId)
                .build();
        occupation.setId("1");
        occupation.setNumber("234567");
        occupation.setStatus(Occupation.Status.CURRENT);
        occupation.setCreatedOn(LocalDateTime.now());
        return occupation;
    }

    @Test
    @DisplayName("create returns unauthorised when user is not authenticated status 401")
    void create_returnsUnauthorized_status401() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/occupations")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsOccupation_whenSuccessful() {
        var paymentId = "1";
        LocalDate now = LocalDate.now();
        var tenant = new TenantDto("John", "Doe", "Doe", "0720000000",
                "johndoe@gmail.com");
        var dto = new OccupationForNewTenantDto(null, tenant, new OccupationDto(now, "1", paymentId));
        Occupation occupation = getOccupation();

        // when
        when(occupationService.create(dto)).thenReturn(Mono.just(occupation));

        // then
        client
                .post()
                .uri("/v1/occupations")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), OccupationDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Occupation created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.status").isEqualTo(Occupation.Status.CURRENT.getState())
                .jsonPath("$.data.endDate").isEmpty()
                .jsonPath("$.data.unitId").isEqualTo("1")
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_whenValidationFails() {
        var dtoNotValid = new OccupationForNewTenantDto();

        // when
        when(occupationService.create(dtoNotValid)).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/occupations")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNotValid), OccupationDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Occupation is required. Tenant or Tenant id is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns unauthorised when user is not authenticated status 401")
    void update_returnsUnauthorized_status401() {
        // given
        var id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/occupations/{occupationId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void findById_returnsUnauthorized_status401() {
        // given
        var occupationId = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/occupations/{occupationId}", occupationId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsOccupation_whenSuccessful() {
        // given
        var occupationId = "1";
        Occupation occupation = getOccupation();
        //when
        when(occupationService.findById(occupationId)).thenReturn(Mono.just(occupation));

        // then
        client
                .get()
                .uri("/v1/occupations/{occupationId}", occupationId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Occupation found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.status").isEqualTo(Occupation.Status.CURRENT.getState())
                .jsonPath("$.data.startDate").isNotEmpty()
                .jsonPath("$.data.endDate").isEmpty()
                .jsonPath("$.data.tenantId").isEqualTo("1")
                .jsonPath("$.data.unitId").isEqualTo("1")
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void find_returnsUnauthorized_status401() {
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/occupations")
                        .queryParam("status", "CURRENT")
                        .build();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri(uriFunc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void find_returnsEmpty_whenNoOccupationExists() {
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/occupations")
                        .queryParam("status", "CURRENT")
                        .build();

        // when
        when(occupationService.findAll(
                Occupation.Status.CURRENT,
                null,
                null,
                null,
                OrderType.DESC
        )).thenReturn(Flux.empty());
        // then
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Occupations with those parameters do not exist!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void find_returnsOccupations_whenSuccessful() {
        // given
        OrderType order = OrderType.ASC;
        var occupation = getOccupation();
        String number = occupation.getNumber();
        String unitId = occupation.getUnitId();
        String tenantId = occupation.getTenantId();

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/occupations")
                        .queryParam("status", "CURRENT")
                        .queryParam("occupationNo", number)
                        .queryParam("unitId", unitId)
                        .queryParam("tenantId", tenantId)
                        .queryParam("order", "ASC")
                        .build();

        // when
        when(occupationService.findAll(
                Occupation.Status.CURRENT,
                number,
                unitId,
                tenantId,
                order
        )).thenReturn(Flux.just(occupation));

        // then
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Occupations found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].status").isEqualTo(Occupation.Status.CURRENT.getState())
                .jsonPath("$.data.[0].startDate").isNotEmpty()
                .jsonPath("$.data.[0].endDate").isEmpty()
                .jsonPath("$.data.[0].tenantId").isEqualTo("1")
                .jsonPath("$.data.[0].unitId").isEqualTo("1")
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .jsonPath("$.data.[0].modifiedOn").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void find_returnsBadRequest_whenStatusIsInvalid() {
        // given
        String unitId = "1";
        String tenantId = "1";
        OrderType order = OrderType.ASC;
        var occupation = new Occupation.OccupationBuilder()
                .tenantId("1")
                .startDate(LocalDate.now())
                .unitId("1")
                .build();
        occupation.setId("1");
        occupation.setStatus(Occupation.Status.CURRENT);
        occupation.setCreatedOn(LocalDateTime.now());

        Function<UriBuilder, URI> uriFunc2 = uriBuilder ->
                uriBuilder
                        .path("/v1/occupations")
                        .queryParam("status", "NOT_TYPE")
                        .queryParam("unitId", unitId)
                        .queryParam("tenantId", tenantId)
                        .queryParam("order", order)
                        .build();

        // then
        client
                .get()
                .uri(uriFunc2)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Status should be PENDING_OCCUPATION or CURRENT or PENDING_VACATING or VACATED!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("delete returns unauthorised when user is not authenticated status 401")
    void delete_returnsUnauthorized_status401() {
        // given
        var id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .delete()
                .uri("/v1/occupations/{occupationId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void deleteById_returnsTrue_whenSuccessful() {
        // given
        String id = "1";
        // when
        when(occupationService.deleteById(id)).thenReturn(Mono.just(true));
        // then
        client
                .delete()
                .uri("/v1/occupations/{occupationId}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Occupation with id %s deleted successfully.".formatted(id))
                .consumeWith(System.out::println);
    }
}