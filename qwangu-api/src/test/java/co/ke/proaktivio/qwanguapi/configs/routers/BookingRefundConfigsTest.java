package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.BookingRefundHandler;
import co.ke.proaktivio.qwanguapi.models.BookingRefund;
import co.ke.proaktivio.qwanguapi.pojos.BookingRefundDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.BookingRefundService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
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

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {BookingRefundConfigs.class, BookingRefundHandler.class, SecurityConfig.class})
class BookingRefundConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private BookingRefundService bookingRefundService;

    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    private final BookingRefund bookingRefund = new BookingRefund("1", BigDecimal.valueOf(15000), "QVERDFDEERERT is transactionID", "1",
            LocalDateTime.now(), null, null, null);

    @Test
    @DisplayName("create returns unauthorised when user is not authenticated status 401")
    void create_returnsUnauthorized_status401() {
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/refunds")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("create returns a Mono of BookingRefund when name does not exist")
    void create_returnsBookingRefund_status201() {
        // given
        var dto = new BookingRefundDto(BigDecimal.valueOf(15000), "QVERDFDEERERT is transactionID",
                "1");
        // when
        Mockito.when(bookingRefundService.create(dto)).thenReturn(Mono.just(bookingRefund));

        // then
        client
                .post()
                .uri("/v1/refunds")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), BookingRefundDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Refund created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.amount").isEqualTo("15000")
                .jsonPath("$.data.refundDetails").isEqualTo("QVERDFDEERERT is transactionID")
                .jsonPath("$.data.receivableId").isEqualTo("1")
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
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
                        .path("/v1/refunds")
                        .queryParam("id", 1)
                        .queryParam("receivableId", "1")
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
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("find returns BookingRefunds with status 200")
    void find_returnsBookingRefunds_status200() {
        // given
        var id = "1";
        var receivableId = "1";
        var page = 1;
        var pageSize = 10;
        OrderType order = OrderType.ASC;

        // when
        when(bookingRefundService.findPaginated(
                Optional.of(id),
                Optional.of(receivableId),
                1,
                10,
                order)).thenReturn(Flux.just(bookingRefund));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/refunds")
                        .queryParam("id", id)
                        .queryParam("receivableId", receivableId)
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
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Refunds found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].amount").isEqualTo("15000")
                .jsonPath("$.data.[0].refundDetails").isEqualTo("QVERDFDEERERT is transactionID")
                .jsonPath("$.data.[0].receivableId").isEqualTo("1")
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .jsonPath("$.data.[0].modifiedOn").isEmpty()
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
                .uri("/v1/refunds/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("Delete by id returns true with status 200")
    void deleteById_returnsTrue_status200() {
        // given
        String id = "1";
        // when
        when(bookingRefundService.deleteById(id)).thenReturn(Mono.just(true));
        // then
        client
                .delete()
                .uri("/v1/refunds/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Refund with id %s deleted successfully.".formatted(id))
                .consumeWith(System.out::println);
    }
}