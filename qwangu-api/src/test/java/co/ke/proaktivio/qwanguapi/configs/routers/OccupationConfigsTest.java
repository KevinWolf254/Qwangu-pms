package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.OccupationHandler;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.OccupationService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
@ContextConfiguration(classes = {OccupationConfigs.class, OccupationHandler.class, SecurityConfig.class})
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
    @WithMockUser
    void create() {
        // given
        String tenantId = "1";
        String unitId = "1";
        var dto = new CreateOccupationDto(true, LocalDateTime.now(), null, tenantId, unitId);
        var occupation = new Occupation("1", true, LocalDateTime.now(), null, tenantId, unitId, LocalDateTime.now(), null);
        var dtoNotValid = new CreateOccupationDto(null, null, null, null, null);

        // when
        when(occupationService.create(dto)).thenReturn(Mono.just(occupation));

        // then
        client
                .post()
                .uri("/v1/occupations")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), CreateOccupationDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Occupation created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.active").isEqualTo(true)
                .jsonPath("$.data.ended").isEmpty()
                .jsonPath("$.data.tenantId").isEqualTo(tenantId)
                .jsonPath("$.data.unitId").isEqualTo(unitId)
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isEmpty()
                .consumeWith(System.out::println);

        // then
        client
                .post()
                .uri("/v1/occupations")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNotValid), CreateOccupationDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Active is required. Started is required. Tenant id is required. Unit id is required.")
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
    @WithMockUser
    void update() {
        // given
        var id = "1";
        var dto = new UpdateOccupationDto(true, LocalDateTime.now(), null);
        var occupation = new Occupation("1", true, LocalDateTime.now(), null, "1", "1", LocalDateTime.now(), null);
        var dtoNotValid = new UpdateOccupationDto(null, null, null);

        // when
        when(occupationService.update(id, dto)).thenReturn(Mono.just(occupation));

        // then
        client
                .put()
                .uri("/v1/occupations/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), CreateOccupationDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Occupation updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.active").isEqualTo(true)
                .jsonPath("$.data.ended").isEmpty()
                .jsonPath("$.data.tenantId").isEqualTo("1")
                .jsonPath("$.data.unitId").isEqualTo("1")
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isEmpty()
                .consumeWith(System.out::println);

        // when
        when(occupationService.update(id, dtoNotValid)).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/occupations/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNotValid), CreateOccupationDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Active is required. Started is required.")
                .consumeWith(System.out::println);

    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void find_returnsUnauthorized_status401() {
        // given
        var id = "1";
        var isActive = true;
        String unitId = "1";
        String tenantId = "1";

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/occupations")
                        .queryParam("id", id)
                        .queryParam("isActive", isActive)
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
    @WithMockUser
    void find() {
        // given
        String id = "1";
        String unitId = "1";
        String tenantId = "1";
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        OrderType order = OrderType.ASC;
        var occupation = new Occupation("1", true, LocalDateTime.now(), null, "1", "1", LocalDateTime.now(), null);

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/occupations")
                        .queryParam("id", id)
                        .queryParam("isActive", "Y")
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
                        .queryParam("isActive", "NOT_TYPE")
                        .queryParam("unitId", unitId)
                        .queryParam("tenantId", tenantId)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", order)
                        .build();

        // when
        when(occupationService.findPaginated(
                Optional.of(id),
                Optional.of(true),
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
                .jsonPath("$.data.[0].active").isEqualTo(true)
                .jsonPath("$.data.[0].ended").isEmpty()
                .jsonPath("$.data.[0].tenantId").isEqualTo("1")
                .jsonPath("$.data.[0].unitId").isEqualTo("1")
                .jsonPath("$.data.[0].created").isNotEmpty()
                .jsonPath("$.data.[0].modified").isEmpty()
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
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data").isEqualTo("Is active should be Y or N!")
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
    @WithMockUser
    void deleteById() {
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