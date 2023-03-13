package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.MpesaIPAddressWhiteListFilter;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.PaymentHandler;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentResponse;
import co.ke.proaktivio.qwanguapi.services.MpesaPaymentService;
import co.ke.proaktivio.qwanguapi.services.PaymentService;

import org.jetbrains.annotations.NotNull;
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

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {PaymentConfigs.class, PaymentHandler.class, SecurityConfig.class, MpesaIPAddressWhiteListFilter.class})
class PaymentConfigsTest {
    @Autowired
    private ApplicationContext context;
    @MockBean
    private MpesaPaymentService mpesaPaymentService;
    @MockBean
    private PaymentService paymentService;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    private final MpesaPaymentDto dto = new MpesaPaymentDto("RKTQDM7W6S", "Pay Bill", "20191122063845", "10", "600638",
            "T903", "", "49197.00", "", "254708374149", "John", "", "Doe");
    private final MpesaPaymentResponse response = new MpesaPaymentResponse(0, "ACCEPTED");
    
    @Test
    void findById() {
    	fail(() -> "TODO - not implemented!");
    }

    @Test
    void findAll() {
    	fail(() -> "TODO - not implemented!");
    }
    
    @Test
    void validate_returnsMpesaPaymentResponse_whenRemoteHostIsWhiteListed() {
        // given    	
        WebTestClient client = WebTestClient
                .bindToApplicationContext(context)
                .webFilter(new SetRemoteAddressWebFilter("127.0.0.1"))
                .configureClient()        
                .build();
        
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(mpesaPaymentService.validate(dto)).thenReturn(Mono.just(response));
        
        // then
        client
                .post()
                .uri("/v1/payments/mpesa/v2/validate")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), MpesaPaymentDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.ResultCode").isEqualTo(0)
                .jsonPath("$.ResultDesc").isEqualTo("ACCEPTED")
                .consumeWith(System.out::println);
        
    }
    
    @Test
    void validate_returnsUnauthorized_status401_whenRemoteAddressIsNotWhitedListed() {
    	// given
        WebTestClient clientNotWhitedListed = WebTestClient
                .bindToApplicationContext(context)
                .webFilter(new SetRemoteAddressWebFilter("193.200.40.29"))
                .configureClient()
                .build();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        clientNotWhitedListed
                .post()
                .uri("/v1/payments/mpesa/v2/validate")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), MpesaPaymentDto.class)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }

    @Test
    void create_returnsMpesaPaymentResponse_whenRemoteHostIsWhiteListed() {
        // given    	
        WebTestClient client = WebTestClient
                .bindToApplicationContext(context)
                .webFilter(new SetRemoteAddressWebFilter("127.0.0.1"))
                .configureClient()        
                .build();
        
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        when(mpesaPaymentService.create(dto)).thenReturn(Mono.just(response));
        
        // then
        client
                .post()
                .uri("/v1/payments/mpesa/v2/confirm")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), MpesaPaymentDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.ResultCode").isEqualTo(0)
                .jsonPath("$.ResultDesc").isEqualTo("ACCEPTED")
                .consumeWith(System.out::println);
        
    }
    
    @Test
    void create_returnsUnauthorized_status401_whenRemoteAddressIsNotWhitedListed() {
    	// given
        WebTestClient clientNotWhitedListed = WebTestClient
                .bindToApplicationContext(context)
                .webFilter(new SetRemoteAddressWebFilter("193.200.40.29"))
                .configureClient()
                .build();
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        clientNotWhitedListed
                .post()
                .uri("/v1/payments/mpesa/v2/confirm")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), MpesaPaymentDto.class)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }
}

class SetRemoteAddressWebFilter implements WebFilter {

    private final String host;

    public SetRemoteAddressWebFilter(String host) {
        this.host = host;
    }

    @Override
    public @NotNull Mono<Void> filter(@NotNull ServerWebExchange exchange, WebFilterChain chain) {
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
            public @NotNull ServerHttpRequest getRequest() {
                return decorated;
            }
        };
    }
}