package co.ke.proaktivio.qwanguapi.configs.routers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import org.springframework.web.util.UriBuilder;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.MpesaIPAddressWhiteListFilter;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.MpesaPaymentHandler;
import co.ke.proaktivio.qwanguapi.models.MpesaPayment;
import co.ke.proaktivio.qwanguapi.models.MpesaPayment.MpesaPaymentType;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentDto;
import co.ke.proaktivio.qwanguapi.pojos.MpesaPaymentResponse;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.MpesaPaymentService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {MpesaPaymentConfigs.class, MpesaPaymentHandler.class, SecurityConfig.class, MpesaIPAddressWhiteListFilter.class,
		GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class MpesaPaymentConfigsTest {
    @Autowired
    private WebTestClient client;
    @Autowired
    private ApplicationContext context;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;
    
    @MockBean
    private MpesaPaymentService mpesaPaymentService;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }
    
    private final MpesaPaymentDto dto = new MpesaPaymentDto("RKTQDM7W6S", "Pay Bill", "20191122063845", "10", "600638",
            "T903", "", "49197.00", "", "254708374149", "John", "", "Doe");
    private final MpesaPaymentResponse response = new MpesaPaymentResponse(0, "ACCEPTED");
    
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
                .uri("/v1/payments/mpesa/validate")
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
                .uri("/v1/payments/mpesa/validate")
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
                .uri("/v1/payments/mpesa")
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
                .uri("/v1/payments/mpesa")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), MpesaPaymentDto.class)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .consumeWith(System.out::println);
    }
    
    @Test
    void findById_returnsUnauthorized_status401_whenUserIsNotAuthorised() {
    	// given
    	var mpesaPaymentId = "1234";
    	// when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/payments/mpesa/{mpesaPaymentId}", mpesaPaymentId)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsNotFound_whenMpesaPaymentIdDoesNotExist_withStatus200() {
    	// given
    	var mpesaPaymentId = "1234";
    	// when
    	when(mpesaPaymentService.findById(mpesaPaymentId)).thenReturn(Mono.empty());
    	// then
        client
	        .get()
	        .uri("/v1/payments/mpesa/{mpesaPaymentId}", mpesaPaymentId)
	        .exchange()
	        .expectStatus().isNotFound()
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.timestamp").isNotEmpty()
	        .jsonPath("$.path").isEqualTo("/v1/payments/mpesa/1234")
	        .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Payment with id %s does not exist!".formatted(mpesaPaymentId))
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);   	
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsMpesaPayment_whenSuccessful_withStatus200() {
    	// given
    	var mpesaPaymentId = "1234";
    	MpesaPaymentType mpesaTill = MpesaPaymentType.MPESA_TILL;
    	String transactionId = "QWERGOEJEOD9E";
    	String transactionType = "WMSOSJ";
    	Currency kes = Currency.KES;
    	BigDecimal amount = BigDecimal.valueOf(20000l);
    	String shortCode = "12345";
    	String referenceNumber = "KMDPNWPQ2444O4";
    	String balance = "200000";
    	String mobileNumber = "0720000000";
    	String firstName = "John";
    	String middleName = "Doe";

    	var mpesaPayment = new MpesaPayment();
    	mpesaPayment.setId(mpesaPaymentId);
		mpesaPayment.setType(mpesaTill);
    	mpesaPayment.setIsProcessed(true);
		mpesaPayment.setTransactionId(transactionId);
		mpesaPayment.setTransactionType(transactionType);
    	mpesaPayment.setTransactionTime(LocalDateTime.now());
		mpesaPayment.setCurrency(kes);
		mpesaPayment.setAmount(amount);
		mpesaPayment.setShortCode(shortCode);
		mpesaPayment.setReferenceNumber(referenceNumber);
    	mpesaPayment.setInvoiceNo(shortCode);
		mpesaPayment.setBalance(balance);
		mpesaPayment.setMobileNumber(mobileNumber);
		mpesaPayment.setFirstName(firstName);
		mpesaPayment.setMiddleName(middleName);
    	mpesaPayment.setCreatedOn(LocalDateTime.now());
    	mpesaPayment.setModifiedOn(LocalDateTime.now());
    	// when
    	when(mpesaPaymentService.findById(mpesaPaymentId)).thenReturn(Mono.just(mpesaPayment));
    	// then
        client
	        .get()
	        .uri("/v1/payments/mpesa/{mpesaPaymentId}", mpesaPaymentId)
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.timestamp").isNotEmpty()
	        .jsonPath("$.path").isEqualTo("/v1/payments/mpesa/1234")
	        .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
	        .jsonPath("$.success").isEqualTo(true)
	        .jsonPath("$.message").isEqualTo("Payment found successfully.")
	        .jsonPath("$.data").isNotEmpty()
            .jsonPath("$.data.id").isEqualTo(mpesaPaymentId)
            .jsonPath("$.data.type").isEqualTo(mpesaTill.getName())
            .jsonPath("$.data.isProcessed").isBoolean()
            .jsonPath("$.data.transactionId").isEqualTo(transactionId)
            .jsonPath("$.data.transactionType").isEqualTo(transactionType)
            .jsonPath("$.data.transactionTime").isNotEmpty()
            .jsonPath("$.data.currency").isEqualTo(kes.name())
            .jsonPath("$.data.amount").isEqualTo(amount.toPlainString())
            .jsonPath("$.data.shortCode").isEqualTo(shortCode)
            .jsonPath("$.data.referenceNumber").isEqualTo(referenceNumber)
            .jsonPath("$.data.balance").isEqualTo(balance)
            .jsonPath("$.data.mobileNumber").isEqualTo(mobileNumber)
            .jsonPath("$.data.firstName").isEqualTo(firstName)
            .jsonPath("$.data.middleName").isEqualTo(middleName)
            .jsonPath("$.data.createdOn").isNotEmpty()
            .jsonPath("$.data.modifiedOn").isNotEmpty()
	        .consumeWith(System.out::println);    	
    }
    
    @Test
    void findAll_returnsUnauthorized_status401_whenUserIsNotAuthorised() {
    	// given
    	// when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
    	// then
        client
                .get()
                .uri("/v1/payments/mpesa")
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnEmptyList_whenPaymentsDoNotExists_withStatus200() {
    	// given
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/payments/mpesa")
                        .build();
    	// when
    	when(mpesaPaymentService.findAll(null, null, null, OrderType.DESC)).thenReturn(Flux.empty());
    	// then
        client
	        .get()
	        .uri(uriFunc)
	        .exchange()
	        .expectStatus().isOk()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.timestamp").isNotEmpty()
	        .jsonPath("$.path").isEqualTo("/v1/payments/mpesa")
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Payments with those parameters do not exist!")
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsPayments_whenSuccessful_withStatus200() {
    	// given
    	var mpesaPaymentId = "1234";
    	MpesaPaymentType mpesaTill = MpesaPaymentType.MPESA_TILL;
    	String transactionId = "QWERGOEJEOD9E";
    	String transactionType = "WMSOSJ";
    	Currency kes = Currency.KES;
    	BigDecimal amount = BigDecimal.valueOf(20000l);
    	String shortCode = "12345";
    	String referenceNumber = "KMDPNWPQ2444O4";
    	String balance = "200000";
    	String mobileNumber = "0720000000";
    	String firstName = "John";
    	String middleName = "Doe";

    	var mpesaPayment = new MpesaPayment();
    	mpesaPayment.setId(mpesaPaymentId);
		mpesaPayment.setType(mpesaTill);
    	mpesaPayment.setIsProcessed(true);
		mpesaPayment.setTransactionId(transactionId);
		mpesaPayment.setTransactionType(transactionType);
    	mpesaPayment.setTransactionTime(LocalDateTime.now());
		mpesaPayment.setCurrency(kes);
		mpesaPayment.setAmount(amount);
		mpesaPayment.setShortCode(shortCode);
		mpesaPayment.setReferenceNumber(referenceNumber);
    	mpesaPayment.setInvoiceNo(shortCode);
		mpesaPayment.setBalance(balance);
		mpesaPayment.setMobileNumber(mobileNumber);
		mpesaPayment.setFirstName(firstName);
		mpesaPayment.setMiddleName(middleName);
    	mpesaPayment.setCreatedOn(LocalDateTime.now());
    	mpesaPayment.setModifiedOn(LocalDateTime.now());
    	
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/payments/mpesa")
                .queryParam("transactionId", transactionId)
                .queryParam("referenceNumber", referenceNumber)
                .queryParam("shortCode", shortCode)
                .queryParam("order", OrderType.ASC.name())
                .build();
    	// when
    	when(mpesaPaymentService.findAll(transactionId, referenceNumber, shortCode, OrderType.ASC)).thenReturn(Flux.just(mpesaPayment));
    	// then
        client
	        .get()
	        .uri(uriFunc)
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.timestamp").isNotEmpty()
	        .jsonPath("$.path").isEqualTo("/v1/payments/mpesa")
	        .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
	        .jsonPath("$.success").isEqualTo(true)
	        .jsonPath("$.message").isEqualTo("Payments found successfully.")
	        .jsonPath("$.data").isNotEmpty()
	        .jsonPath("$.data.[0].id").isEqualTo(mpesaPaymentId)
	        .jsonPath("$.data.[0].type").isEqualTo(mpesaTill.getName())
	        .jsonPath("$.data.[0].isProcessed").isBoolean()
	        .jsonPath("$.data.[0].transactionId").isEqualTo(transactionId)
	        .jsonPath("$.data.[0].transactionType").isEqualTo(transactionType)
	        .jsonPath("$.data.[0].transactionTime").isNotEmpty()
	        .jsonPath("$.data.[0].currency").isEqualTo(kes.name())
	        .jsonPath("$.data.[0].amount").isEqualTo(amount.toPlainString())
	        .jsonPath("$.data.[0].shortCode").isEqualTo(shortCode)
	        .jsonPath("$.data.[0].referenceNumber").isEqualTo(referenceNumber)
	        .jsonPath("$.data.[0].balance").isEqualTo(balance)
	        .jsonPath("$.data.[0].mobileNumber").isEqualTo(mobileNumber)
	        .jsonPath("$.data.[0].firstName").isEqualTo(firstName)
	        .jsonPath("$.data.[0].middleName").isEqualTo(middleName)
	        .jsonPath("$.data.[0].createdOn").isNotEmpty()
	        .jsonPath("$.data.[0].modifiedOn").isNotEmpty()
	        .consumeWith(System.out::println); 
    }
}

final class SetRemoteAddressWebFilter implements WebFilter {

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
