package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.RoleHandler;
import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.pojos.ErrorCode;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.RoleService;
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
import java.util.Set;
import java.util.function.Function;

@ContextConfiguration(classes = {RoleConfigs.class, RoleHandler.class})
@WebFluxTest
class RoleConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private RoleService roleService;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    @DisplayName("Find returns a Flux of roles")
    void find_returnsFluxOfRoles_status200() {
        // given
        String page = "1";
        String id = "1";
        String name = "ADMIN";
        LocalDateTime now = LocalDateTime.now();
        var role = new Role("1", name, Set.of("1"), now, null);

        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        OrderType order = OrderType.ASC;

        // when
        Mockito.when(roleService.findPaginated(
                Optional.of(id),
                Optional.of(name),
                finalPage,
                finalPageSize,
                order)).thenReturn(Flux.just(role));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/roles")
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
                .jsonPath("$.message").isEqualTo("Roles found successfully.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].name").isEqualTo(name)
                .jsonPath("$.data.[0].authorityIds").isNotEmpty()
                .jsonPath("$.data.[0].authorityIds.[0]").isEqualTo("1")
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
        Mockito.when(roleService.findPaginated(
                        Optional.of(id),
                        Optional.of(name),
                        finalPage,
                        finalPageSize,
                        OrderType.valueOf(order)))
                .thenReturn(Flux.error(new CustomNotFoundException("Roles were not found!")));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/roles")
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
                .jsonPath("$.data").isEqualTo("Roles were not found!")
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
        Mockito.when(roleService.findPaginated(
                        Optional.of(id),
                        Optional.of(name),
                        finalPage,
                        finalPageSize,
                        order))
                .thenReturn(Flux.error(new RuntimeException("Something happened!")));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/roles")
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
                .jsonPath("$.data").isEqualTo("Something happened!")
                .consumeWith(System.out::println);
    }
}