package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.handlers.OccupationHandler;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
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
import java.time.LocalDateTime;
import java.util.Optional;
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
        // given
        String tenantId = "1";
        String unitId = "1";
        LocalDateTime now = LocalDateTime.now();
        var dto = new OccupationDto(Occupation.Status.CURRENT, now, null, tenantId, unitId);
        var occupation = new Occupation("1", Occupation.Status.CURRENT, now, null,
                tenantId, unitId, now, null, null, null);
        var dtoNotValid = new OccupationDto(null, null, null, null, null);

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
                .jsonPath("$.data.ended").isEmpty()
                .jsonPath("$.data.tenantId").isEqualTo(tenantId)
                .jsonPath("$.data.unitId").isEqualTo(unitId)
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .consumeWith(System.out::println);

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
                .jsonPath("$.message").isEqualTo("Status is required. Started is required. Tenant id is required. Unit id is required.")
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
                .uri("/v1/occupations/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsOccupation_whenSuccessful() {
        // given
        var id = "1";
        var dto = new OccupationDto(Occupation.Status.CURRENT, LocalDateTime.now(), null, "1", "1");
        var occupation = new Occupation("1", Occupation.Status.CURRENT, LocalDateTime.now(), null,
                "1", "1", LocalDateTime.now(), null, null, null);
        var dtoNotValid = new OccupationDto(null, null, null, null, null);

        // when
        when(occupationService.update(id, dto)).thenReturn(Mono.just(occupation));

        // then
        client
                .put()
                .uri("/v1/occupations/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), OccupationDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Occupation updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.status").isEqualTo(Occupation.Status.CURRENT.getState())
                .jsonPath("$.data.ended").isEmpty()
                .jsonPath("$.data.tenantId").isEqualTo("1")
                .jsonPath("$.data.unitId").isEqualTo("1")
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .consumeWith(System.out::println);

        // when
        when(occupationService.update(id, dtoNotValid)).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/occupations/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNotValid), OccupationDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Status is required. Started is required. Tenant id is required. Unit id is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void find_returnsUnauthorized_status401() {
        // given
        var id = "1";
        String unitId = "1";
        String tenantId = "1";

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/occupations")
                        .queryParam("id", id)
                        .queryParam("status", "CURRENT")
                        .queryParam("unitId", unitId)
                        .queryParam("tenantId", tenantId)
                        .queryParam("page", 1)
                        .queryParam("pageSize", 10)
                        .queryParam("order", OrderType.ASC)
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
    void find_returnsOccupations_whenSuccessful() {
        // given
        String id = "1";
        String unitId = "1";
        String tenantId = "1";
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        OrderType order = OrderType.ASC;
        var occupation = new Occupation("1", Occupation.Status.CURRENT, LocalDateTime.now(), null,
                "1", "1", LocalDateTime.now(), null, null, null);

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/occupations")
                        .queryParam("id", id)
                        .queryParam("status", "CURRENT")
                        .queryParam("unitId", unitId)
                        .queryParam("tenantId", tenantId)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", order)
                        .build();

        Function<UriBuilder, URI> uriFunc2 = uriBuilder ->
                uriBuilder
                        .path("/v1/occupations")
                        .queryParam("id", id)
                        .queryParam("status", "NOT_TYPE")
                        .queryParam("unitId", unitId)
                        .queryParam("tenantId", tenantId)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", order)
                        .build();

        // when
        when(occupationService.findPaginated(
                Optional.of(id),
                Optional.of(Occupation.Status.CURRENT),
                Optional.of(unitId),
                Optional.of(tenantId),
                finalPage,
                finalPageSize,
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
                .jsonPath("$.data.[0].ended").isEmpty()
                .jsonPath("$.data.[0].tenantId").isEqualTo("1")
                .jsonPath("$.data.[0].unitId").isEqualTo("1")
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .jsonPath("$.data.[0].modifiedOn").isEmpty()
                .consumeWith(System.out::println);

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
                .jsonPath("$.message").isEqualTo("Status should be BOOKED or CURRENT or PREVIOUS!")
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
                .uri("/v1/occupations/{id}", id)
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
                .uri("/v1/occupations/{id}", id)
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