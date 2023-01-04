package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.handlers.NoticeHandler;
import co.ke.proaktivio.qwanguapi.models.Notice;
import co.ke.proaktivio.qwanguapi.pojos.*;
import co.ke.proaktivio.qwanguapi.services.NoticeService;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {NoticeConfigs.class, NoticeHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
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
    private final LocalDate today = LocalDate.now();
    private final LocalDate now = LocalDate.now();

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
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsNotice_whenSuccessful() {
        // given
        var dto = new CreateNoticeDto(now, today.plusDays(30), "1");
        var dtoNotValid = new CreateNoticeDto(null ,null, null);
        var notice = new Notice("1", Notice.Status.ACTIVE, now, today.plusDays(40), "1", LocalDateTime.now(), null, null,
                null);
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
                .jsonPath("$.data.isActive").isEqualTo(true)
                .jsonPath("$.data.notifiedOn").isNotEmpty()
                .jsonPath("$.data.vacatingOn").isNotEmpty()
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
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
                .jsonPath("$.message").isEqualTo("Notification date required. Vacating date is required. Occupation id is required.")
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
                .uri("/v1/notices/{noticeId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsNotice_whenSuccessful() {
        // given
        var id = "1";
        var dto = new UpdateNoticeDto(now, today.plusDays(30), Notice.Status.ACTIVE);
        var dtoNotValid = new UpdateNoticeDto(null ,null, null);
        var notice = new Notice("1", Notice.Status.ACTIVE, now, today.plusDays(40), "1",
                LocalDateTime.now(), null, LocalDateTime.now(), null);

        // when
        when(noticeService.update(id, dto)).thenReturn(Mono.just(notice));
        // then
        client
                .put()
                .uri("/v1/notices/{noticeId}", id)
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
                .jsonPath("$.data.isActive").isEqualTo(true)
                .jsonPath("$.data.notifiedOn").isNotEmpty()
                .jsonPath("$.data.vacatingOn").isNotEmpty()
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isNotEmpty()
                .consumeWith(System.out::println);

        // when
        when(noticeService.update("1", dtoNotValid)).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/notices/{noticeId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNotValid), CreateNoticeDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("IsActive is required. Notification date required. Vacating date is required.")
                .jsonPath("$.data").isEmpty()
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
                        .queryParam("noticeId", id)
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
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void find_returnsNotices_whenSuccessful() {
        // given
        var id = "1";
        var occupationId = "1";
        var notice = new Notice("1", Notice.Status.ACTIVE, now, today.plusDays(40), "1",
                LocalDateTime.now(), null, LocalDateTime.now(), null);
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        OrderType order = OrderType.ASC;

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/notices")
                        .queryParam("noticeId", id)
                        .queryParam("isActive", "true")
                        .queryParam("occupationId", occupationId)
                        .queryParam("page", 1)
                        .queryParam("pageSize", 10)
                        .queryParam("order", OrderType.ASC)
                        .build();

        Function<UriBuilder, URI> uriFunc2 = uriBuilder ->
                uriBuilder
                        .path("/v1/notices")
                        .queryParam("isActive", "NOT_VALID")
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
                .jsonPath("$.data.[0].isActive").isEqualTo(true)
                .jsonPath("$.data.[0].notifiedOn").isNotEmpty()
                .jsonPath("$.data.[0].vacatingOn").isNotEmpty()
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .jsonPath("$.data.[0].modifiedOn").isNotEmpty()
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
                .jsonPath("$.message").isEqualTo("isActive should be a true or false!")
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
                .uri("/v1/notices/{noticeId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void deleteById_returnTrue_whenSuccessful() {
        // given
        String id = "1";
        // when
        when(noticeService.deleteById(id)).thenReturn(Mono.just(true));
        // then
        client
                .delete()
                .uri("/v1/notices/{noticeId}", id)
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