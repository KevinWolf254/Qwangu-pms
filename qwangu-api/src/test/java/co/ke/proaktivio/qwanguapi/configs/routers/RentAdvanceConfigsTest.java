package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.RentAdvanceHandler;
import co.ke.proaktivio.qwanguapi.models.RentAdvance;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.RentAdvanceDto;
import co.ke.proaktivio.qwanguapi.pojos.UpdateRentAdvanceDto;
import co.ke.proaktivio.qwanguapi.services.RentAdvanceService;
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
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {RentAdvanceConfigs.class, RentAdvanceHandler.class, SecurityConfig.class})
class RentAdvanceConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private RentAdvanceService rentAdvanceService;

    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    @Before
    public void setUp() {client = WebTestClient.bindToApplicationContext(context).build();}

    private LocalDate today = LocalDate.now();
    private final LocalDateTime now = LocalDateTime.now();
    private final String advanceId = "1";
    private final RentAdvance advance = new RentAdvance(advanceId, RentAdvance.Status.RELEASED, "Details!", "1",
            "1", now, now, today);

    @Test
    @DisplayName("create returns unauthorised when user is not authenticated status 401")
    void create_returnsUnauthorized_status401() {
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/advances")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("create returns a RentAdvance with status 201")
    void create_returnsRentAdvance_status201() {
        // given
        var advance = new RentAdvance("1", RentAdvance.Status.HOLDING, null,
                "1", "1", LocalDateTime.now(), null, null);
        var dto = new RentAdvanceDto(RentAdvance.Status.HOLDING, "1", "1");

        //when
        when(rentAdvanceService.create(dto)).thenReturn(Mono.just(advance));

        // then
        client
                .post()
                .uri("/v1/advances")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), RentAdvanceDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Advance created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.status").isEqualTo(RentAdvance.Status.HOLDING.getState())
                .jsonPath("$.data.returnDetails").isEqualTo(null)
                .jsonPath("$.data.occupationId").isEqualTo("1")
                .jsonPath("$.data.paymentId").isEqualTo("1")
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .jsonPath("$.data.returnedOn").isEqualTo(null)
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns unauthorised when user is not authenticated status 401")
    void update_returnsUnauthorized_status401() {
        // given
        String id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/advances/{id}",id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("update returns a RentAdvance with status 200")
    void update_returnsRentAdvance_status200() {
        // given
       var dto = new UpdateRentAdvanceDto(RentAdvance.Status.RELEASED, "Details!", today);
        // when
        when(rentAdvanceService.update(advanceId, dto)).thenReturn(Mono.just(advance));
        // then
        client
                .put()
                .uri("/v1/advances/{id}",advanceId)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UpdateRentAdvanceDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Advance updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo(advanceId)
                .jsonPath("$.data.status").isEqualTo(RentAdvance.Status.RELEASED.getState())
                .jsonPath("$.data.returnDetails").isEqualTo("Details!")
                .jsonPath("$.data.occupationId").isEqualTo("1")
                .jsonPath("$.data.paymentId").isEqualTo("1")
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isNotEmpty()
                .jsonPath("$.data.returnedOn").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void find_returnsUnauthorized_status401() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/apartments")
                        .queryParam("id", 1)
                        .queryParam("status", RentAdvance.Status.HOLDING.getState())
                        .queryParam("occupationId", 1)
                        .queryParam("paymentId", 1)
                        .queryParam("page", 1)
                        .queryParam("pageSize", 10)
                        .queryParam("order", OrderType.ASC)
                        .build();
        client
                .put()
                .uri(uriFunc)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("find returns RentAdvances with status 200")
    void find_returnsRentAdvance_status200() {
        // given
        var status = RentAdvance.Status.RENT_PAYMENT;
        var occupationId = "1";
        var paymentId = "1";
        var page = 1;
        var pageSize = 10;
        OrderType order = OrderType.ASC;

        // when
        when(rentAdvanceService.findPaginated(
                Optional.of(advanceId),
                Optional.of(status),
                Optional.of(occupationId),
                Optional.of(paymentId),
                page,
                pageSize,
                order)).thenReturn(Flux.just(advance));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/advances")
                        .queryParam("id", advanceId)
                        .queryParam("status", status)
                        .queryParam("occupationId", occupationId)
                        .queryParam("paymentId", paymentId)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", order)
                        .build();
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Advances found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0].id").isEqualTo(advanceId)
                .jsonPath("$.data.[0].status").isEqualTo(RentAdvance.Status.RELEASED.getState())
                .jsonPath("$.data.[0].returnDetails").isEqualTo("Details!")
                .jsonPath("$.data.[0].occupationId").isEqualTo("1")
                .jsonPath("$.data.[0].paymentId").isEqualTo("1")
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .jsonPath("$.data.[0].modifiedOn").isNotEmpty()
                .jsonPath("$.data.[0].returnedOn").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Delete by id  returns unauthorised when user is not authenticated status 401")
    void deleteById_returnsUnauthorized_status401() {
        // given
        String id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/advances/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("Delete by id returns a Mono of boolean when id exists")
    void deleteById_returnsTrue_status200() {
        // given
        String id = "1";
        // when
        when(rentAdvanceService.deleteById(id)).thenReturn(Mono.just(true));
        // then
        client
                .delete()
                .uri("/v1/advances/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Advance with id %s deleted successfully.".formatted(id))
                .consumeWith(System.out::println);
    }
}