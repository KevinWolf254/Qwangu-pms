package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.AuthorityHandler;
import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.ErrorCode;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

@ContextConfiguration(classes = {AuthorityConfigs.class, AuthorityHandler.class})
@WebFluxTest
class AuthorityConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private AuthorityService authorityService;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    @DisplayName("Find returns a Flux of Authorities")
    void find_returnsFluxOfAuthorities_status200() {
        // given
        String page = "1";
        String id = "1";
        String name = "ADMIN";
        LocalDateTime now = LocalDateTime.now();
        var authority = new Authority("1", name, true, true, true, true, true, now, null);

        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        OrderType order = OrderType.ASC;

        // when
        Mockito.when(authorityService.findPaginated(
                Optional.of(id),
                Optional.of(name),
                finalPage - 1,
                finalPageSize,
                order)).thenReturn(Flux.just(authority));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/authorities")
                        .queryParam("id", id)
                        .queryParam("name", name)
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
                .jsonPath("$.message").isEqualTo("Authorities found successfully.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].name").isEqualTo(name)
                .jsonPath("$.data.[0].create").isEqualTo(true)
                .jsonPath("$.data.[0].read").isEqualTo(true)
                .jsonPath("$.data.[0].write").isEqualTo(true)
                .jsonPath("$.data.[0].delete").isEqualTo(true)
                .jsonPath("$.data.[0].authorize").isEqualTo(true)
                .jsonPath("$.data.[0].created").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("find returns CustomNotFoundException with status 404")
    void find_returnsCustomNotFoundException_status404() {
        // given
        String id = "1";
        String name = "ADMIN";
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        String order = OrderType.ASC.name();

        // when
        Mockito.when(authorityService.findPaginated(
                        Optional.of(id),
                        Optional.of(name),
                        finalPage - 1,
                        finalPageSize,
                        OrderType.valueOf(order)))
                .thenReturn(Flux.error(new CustomNotFoundException("Authorities were not found!")));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/authorities")
                        .queryParam("id", id)
                        .queryParam("name", name)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", order)
                        .build();
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.NOT_FOUND_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Not found!")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Authorities were not found!")
                .consumeWith(System.out::println);

    }

    @Test
    @DisplayName("find returns Exception with status 500")
    void find_returnsException_status500() {
        // given
        String id = "1";
        String name = "ADMIN";
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");

        // when
        OrderType order = OrderType.ASC;
        Mockito.when(authorityService.findPaginated(
                        Optional.of(id),
                        Optional.of(name),
                        finalPage - 1,
                        finalPageSize,
                        order))
                .thenReturn(Flux.error(new RuntimeException("Something happened!")));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/authorities")
                        .queryParam("id", id)
                        .queryParam("name", name)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", order)
                        .build();
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.toString())
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Something happened!")
                .consumeWith(System.out::println);
    }
}