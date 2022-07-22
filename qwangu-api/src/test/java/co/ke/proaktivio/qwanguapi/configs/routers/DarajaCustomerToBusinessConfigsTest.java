package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.*;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.DarajaCustomerToBusinessHandler;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessDto;
import co.ke.proaktivio.qwanguapi.pojos.DarajaCustomerToBusinessResponse;
import co.ke.proaktivio.qwanguapi.services.DarajaCustomerToBusinessService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {DarajaCustomerToBusinessConfigs.class, DarajaCustomerToBusinessHandler.class, SecurityConfig.class})
class DarajaCustomerToBusinessConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    MpesaPropertiesConfig mpesaPropertiesConfig;
    @MockBean
    private DarajaCustomerToBusinessService darajaCustomerToBusinessService;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    private final DarajaCustomerToBusinessDto dto = new DarajaCustomerToBusinessDto("RKTQDM7W6S", "Pay Bill", "20191122063845", "10", "600638",
            "T903", "", "49197.00", "", "254708374149", "John", "", "Doe");
    private final DarajaCustomerToBusinessResponse response = new DarajaCustomerToBusinessResponse(0, "ACCEPTED");

    @Test
    void validate() {
        // given
        WebTestClient client = WebTestClient
                .bindToApplicationContext(context)
                .webFilter(new SetRemoteAddressWebFilter("127.0.0.1"))
                .configureClient()
                .build();

        WebTestClient clientNotWhitedListed = WebTestClient
                .bindToApplicationContext(context)
                .webFilter(new SetRemoteAddressWebFilter("127.0.0.50"))
                .configureClient()
                .build();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(darajaCustomerToBusinessService.validate(dto)).thenReturn(Mono.just(response));
        // then
        client
                .post()
                .uri("/v1/mpesa/c2b/validate")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DarajaCustomerToBusinessDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.ResultCode").isEqualTo(0)
                .jsonPath("$.ResultDesc").isEqualTo("ACCEPTED")
                .consumeWith(System.out::println);
        // then
        clientNotWhitedListed
                .post()
                .uri("/v1/mpesa/c2b/validate")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DarajaCustomerToBusinessDto.class)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void confirm() {
        // given
        WebTestClient client = WebTestClient
                .bindToApplicationContext(context)
                .webFilter(new SetRemoteAddressWebFilter("127.0.0.1"))
                .configureClient()
                .build();
        WebTestClient clientNotWhitedListed = WebTestClient
                .bindToApplicationContext(context)
                .webFilter(new SetRemoteAddressWebFilter("127.0.0.50"))
                .configureClient()
                .build();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(darajaCustomerToBusinessService.confirm(dto)).thenReturn(Mono.just(response));
        // then
        client
                .post()
                .uri("/v1/mpesa/c2b/confirm")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DarajaCustomerToBusinessDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.ResultCode").isEqualTo(0)
                .jsonPath("$.ResultDesc").isEqualTo("ACCEPTED")
                .consumeWith(System.out::println);
        // then
        clientNotWhitedListed
                .post()
                .uri("/v1/mpesa/c2b/validate")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), DarajaCustomerToBusinessDto.class)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}

class SetRemoteAddressWebFilter implements WebFilter {

    private String host;

    public SetRemoteAddressWebFilter(String host) {
        this.host = host;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return chain.filter(decorate(exchange));
    }

    private ServerWebExchange decorate(ServerWebExchange exchange) {
        final ServerHttpRequest decorated = new ServerHttpRequestDecorator(exchange.getRequest()) {
            @Override
            public InetSocketAddress getRemoteAddress() {
                return new InetSocketAddress(host, 80);
            }
        };

        return new ServerWebExchangeDecorator(exchange) {
            @Override
            public ServerHttpRequest getRequest() {
                return decorated;
            }
        };
    }
}