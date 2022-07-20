package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.NoticeHandler;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.models.Occupation;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.NoticeService;
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
@ContextConfiguration(classes = {NoticeConfigs.class, NoticeHandler.class, SecurityConfig.class})
class NoticeConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private NoticeService noticeService;
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
                .uri("/v1/notices")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    void create() {
        // given
        LocalDateTime now = LocalDateTime.now();
        var dto = new CreateNoticeDto(now, now.plusDays(30), "1");
        var dtoNotValid = new CreateNoticeDto(null ,null, null);
        var notice = new Notice("1", true, now, now.plusDays(40), now, null, "1");
        // when
        when(noticeService.create(dto)).thenReturn(Mono.just(notice));
        // then
        client
                .post()
                .uri("/v1/notices")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), CreateNoticeDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Notice created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.active").isEqualTo(true)
                .jsonPath("$.data.notificationDate").isNotEmpty()
                .jsonPath("$.data.vacatingDate").isNotEmpty()
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isEmpty()
                .consumeWith(System.out::println);

        // when
        when(noticeService.create(dtoNotValid)).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/notices")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNotValid), CreateNoticeDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Notification date required. Vacating date is required. Occupation id is required.")
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
                .uri("/v1/notices/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    void update() {
        // given
        var id = "1";
        LocalDateTime now = LocalDateTime.now();
        var dto = new UpdateNoticeDto(true, now, now.plusDays(30));
        var dtoNotValid = new UpdateNoticeDto(null ,null, null);
        var notice = new Notice("1", true, now, now.plusDays(40), now, now, "1");

        // when
        when(noticeService.update(id, dto)).thenReturn(Mono.just(notice));
        // then
        client
                .put()
                .uri("/v1/notices/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UpdateNoticeDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Notice updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.active").isEqualTo(true)
                .jsonPath("$.data.notificationDate").isNotEmpty()
                .jsonPath("$.data.vacatingDate").isNotEmpty()
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isNotEmpty()
                .consumeWith(System.out::println);

        // when
        when(noticeService.update("1", dtoNotValid)).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/notices/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNotValid), CreateNoticeDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isEqualTo("Active is required. Notification date required. Vacating date is required.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void find_returnsUnauthorized_status401() {
        // given
        var id = "1";
        var occupationId = "1";

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/notices")
                        .queryParam("id", id)
                        .queryParam("isActive", true)
                        .queryParam("occupationId", occupationId)
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
        var id = "1";
        var occupationId = "1";
        LocalDateTime now = LocalDateTime.now();
        var notice = new Notice("1", true, now, now.plusDays(40), now, now, "1");
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        OrderType order = OrderType.ASC;

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/notices")
                        .queryParam("id", id)
                        .queryParam("isActive", "Y")
                        .queryParam("occupationId", occupationId)
                        .queryParam("page", 1)
                        .queryParam("pageSize", 10)
                        .queryParam("order", OrderType.ASC)
                        .build();

        Function<UriBuilder, URI> uriFunc2 = uriBuilder ->
                uriBuilder
                        .path("/v1/notices")
                        .queryParam("id", id)
                        .queryParam("isActive", "NOT_VALID")
                        .queryParam("occupationId", occupationId)
                        .queryParam("page", 1)
                        .queryParam("pageSize", 10)
                        .queryParam("order", OrderType.ASC)
                        .build();
        // when
        when(noticeService.findPaginated(
                Optional.of(id),
                Optional.of(true),
                Optional.of(occupationId),
                finalPage,
                finalPageSize,
                order
        )).thenReturn(Flux.just(notice));
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
                .jsonPath("$.message").isEqualTo("Notices found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].active").isEqualTo(true)
                .jsonPath("$.data.[0].notificationDate").isNotEmpty()
                .jsonPath("$.data.[0].vacatingDate").isNotEmpty()
                .jsonPath("$.data.[0].created").isNotEmpty()
                .jsonPath("$.data.[0].modified").isNotEmpty()
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
                .uri("/v1/notices/{id}", id)
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
        when(noticeService.deleteById(id)).thenReturn(Mono.just(true));
        // then
        client
                .delete()
                .uri("/v1/notices/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Notice with id %s deleted successfully.".formatted(id))
                .consumeWith(System.out::println);
    }
}