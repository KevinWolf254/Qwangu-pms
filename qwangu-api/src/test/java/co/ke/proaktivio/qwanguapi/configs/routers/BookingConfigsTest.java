package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.BookingHandler;
import co.ke.proaktivio.qwanguapi.models.Booking;
import co.ke.proaktivio.qwanguapi.pojos.CreateBookingDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UpdateBookingDto;
import co.ke.proaktivio.qwanguapi.services.BookingService;
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
@ContextConfiguration(classes = {BookingConfigs.class, BookingHandler.class, SecurityConfig.class})
class BookingConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private BookingService bookingService;

    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    private final LocalDate today = LocalDate.now();
    private final LocalDateTime now = LocalDateTime.now();

    @Before
    public void setUp() {client = WebTestClient.bindToApplicationContext(context).build();}

    @Test
    @DisplayName("create returns unauthorised when user is not authenticated status 401")
    void create_returnsUnauthorized_status401() {
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/apartments")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    void create() {
        // given
        var dto = new CreateBookingDto(today, "1", "1");
        var booking = new Booking("1", Booking.Status.BOOKED, today, now, null, "1", "1");
        var dtoNotValid = new CreateBookingDto(null, null, null);
        //when
        when(bookingService.create(dto)).thenReturn(Mono.just(booking));

        // then
        client
                .post()
                .uri("/v1/bookings")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), CreateBookingDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Booking created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.status").isEqualTo(Booking.Status.BOOKED.getState())
                .jsonPath("$.data.unitId").isEqualTo("1")
                .jsonPath("$.data.paymentId").isEqualTo("1")
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isEmpty()
                .consumeWith(System.out::println);

        // then
        client
                .post()
                .uri("/v1/bookings")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNotValid), CreateBookingDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Occupation date required. Payment id is required. Unit id is required.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns unauthorised when user is not authenticated status 401")
    void update_returnsUnauthorized_status401() {
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/apartments")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    void update() {
        // given
        var id = "1";
        var dto = new UpdateBookingDto(today);
        var dtoNotValid = new UpdateBookingDto();
        var booking = new Booking("1", Booking.Status.BOOKED, today, now, null, "1", "1");
        // when
        when(bookingService.update(id, dto)).thenReturn(Mono.just(booking));
        // then
        client
                .put()
                .uri("/v1/bookings/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UpdateBookingDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Booking updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.status").isEqualTo(Booking.Status.BOOKED.getState())
                .jsonPath("$.data.unitId").isEqualTo("1")
                .jsonPath("$.data.paymentId").isEqualTo("1")
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isEmpty()
                .consumeWith(System.out::println);
        // then
        client
                .put()
                .uri("/v1/bookings/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNotValid), UpdateBookingDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Occupation date required.")
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
                        .path("/v1/bookings")
                        .build();
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
        var id ="1";
        var status = "BOOKED";
        var statusNotValid = "NOT_VALID_STATUS";
        var unitId = "1";
        var page = "1";
        var pageSize = "10";
        var asc = "ASC";
        OrderType order = OrderType.valueOf(asc);
        var now = LocalDateTime.now();
        var booking = new Booking("1", Booking.Status.BOOKED, today, now, null, "1", "1");

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/bookings")
                        .queryParam("id", id)
                        .queryParam("status", status)
                        .queryParam("unitId", unitId)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", asc)
                        .build();

        Function<UriBuilder, URI> uriFuncStatusNotValid = uriBuilder ->
                uriBuilder
                        .path("/v1/bookings")
                        .queryParam("status", statusNotValid)
                        .build();
        // when
        when(bookingService.findPaginated(
                Optional.of(id),
                Optional.of(Booking.Status.BOOKED),
                Optional.of(unitId),
                1,
                10,
                order
        )).thenReturn(Flux.just(booking));
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
                .jsonPath("$.message").isEqualTo("Bookings found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].status").isEqualTo(Booking.Status.BOOKED.getState())
                .jsonPath("$.data.[0].unitId").isEqualTo("1")
                .jsonPath("$.data.[0].paymentId").isEqualTo("1")
                .jsonPath("$.data.[0].created").isNotEmpty()
                .jsonPath("$.data.[0].modified").isEmpty()
                .consumeWith(System.out::println);
        // then
        client
                .get()
                .uri(uriFuncStatusNotValid)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data").isEqualTo("Status should be BOOKED or PENDING_OCCUPATION or OCCUPIED or CANCELLED!")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Delete by id returns unauthorised when user is not authenticated status 401")
    void deleteById_returnsUnauthorized_status401() {
        // given
        String id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/apartments/{id}", id)
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
        when(bookingService.deleteById(id)).thenReturn(Mono.just(true));
        // then
        client
                .delete()
                .uri("/v1/bookings/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Booking with id %s deleted successfully.".formatted(id))
                .consumeWith(System.out::println);
    }
}