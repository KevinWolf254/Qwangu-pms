package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.PaymentHandler;
import co.ke.proaktivio.qwanguapi.models.Payment;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentStatus;
import co.ke.proaktivio.qwanguapi.models.Payment.PaymentType;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.PaymentService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.function.Function;
@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {PaymentConfigs.class, PaymentHandler.class, SecurityConfig.class,
		GlobalErrorConfig.class,GlobalErrorWebExceptionHandler.class})
class PaymentConfigsTest {
    @Autowired
    private WebTestClient client;
    @Autowired
    private ApplicationContext context;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;
    
    @MockBean
    private PaymentService paymentService;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }
    
    @Test
    void findById_returnsUnauthorized_status401_whenUserIsNotAuthorised() {
    	// given
    	var paymentId = "1234";
    	// when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/payments/{paymentId}", paymentId)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsNotFound_whenMpesaPaymentIdDoesNotExist_withStatus200() {
    	// given
    	var paymentId = "1234";
    	// when
    	when(paymentService.findById(paymentId)).thenReturn(Mono.empty());
    	// then
        client
	        .get()
	        .uri("/v1/payments/{paymentId}", paymentId)
	        .exchange()
	        .expectStatus().isNotFound()
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.timestamp").isNotEmpty()
	        .jsonPath("$.path").isEqualTo("/v1/payments/1234")
	        .jsonPath("$.status").isEqualTo(HttpStatus.NOT_FOUND.value())
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Payment with id %s does not exist!".formatted(paymentId))
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);   	
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsPayment_whenSuccessful_withStatus200() {
    	// given
    	var paymentId = "1234";
    	Currency kes = Currency.KES;
    	BigDecimal amount = BigDecimal.valueOf(20000l);
    	String referenceNumber = "KMDPNWPQ2444O4";
    	String occupationNumber = "12345";
    	PaymentType type = PaymentType.MOBILE;
    	PaymentStatus status = PaymentStatus.CLAIMED;
    	
    	var payment = new Payment();
    	payment.setId(paymentId);
		payment.setStatus(status);
		payment.setType(type);
    	payment.setOccupationNumber(occupationNumber);
    	payment.setReferenceNumber(referenceNumber);
		payment.setCurrency(kes);
		payment.setAmount(amount);
    	payment.setCreatedOn(LocalDateTime.now());
    	payment.setModifiedOn(LocalDateTime.now());
    	// when
    	when(paymentService.findById(paymentId)).thenReturn(Mono.just(payment));
    	// then
        client
	        .get()
	        .uri("/v1/payments/{paymentId}", paymentId)
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.timestamp").isNotEmpty()
	        .jsonPath("$.path").isEqualTo("/v1/payments/1234")
	        .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
	        .jsonPath("$.success").isEqualTo(true)
	        .jsonPath("$.message").isEqualTo("Payment found successfully.")
	        .jsonPath("$.data").isNotEmpty()
            .jsonPath("$.data.id").isEqualTo(paymentId)
            .jsonPath("$.data.status").isEqualTo(status.getState())
            .jsonPath("$.data.type").isEqualTo(type.getType())
            .jsonPath("$.data.occupationNumber").isEqualTo(occupationNumber)
            .jsonPath("$.data.referenceNumber").isEqualTo(referenceNumber)
            .jsonPath("$.data.currency").isEqualTo(kes.name())
            .jsonPath("$.data.amount").isEqualTo(amount.toPlainString())
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
                .uri("/v1/payments")
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnEmptyList_whenPaymentsDoNotExists_withStatus200() {
    	// given
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/payments")
                        .build();
    	// when
    	when(paymentService.findAll(null, null, null, OrderType.DESC)).thenReturn(Flux.empty());
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
	        .jsonPath("$.path").isEqualTo("/v1/payments")
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Payments with those parameters do not exist!")
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnBadRequest_whenStatusIsNotIdentified_withStatus400() {
    	// given
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/payments")
                        .queryParam("status", "ITS_WRONG")
                        .build();
    	// when
    	// then
        client
	        .get()
	        .uri(uriFunc)
	        .exchange()
	        .expectStatus().isBadRequest()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.timestamp").isNotEmpty()
	        .jsonPath("$.path").isEqualTo("/v1/payments")
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Status should be UNCLAIMED or CLAIMED!")
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnBadRequest_whenTypeIsNotIdentified_withStatus400() {
    	// given
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/payments")
                        .queryParam("type", "ITS_WRONG")
                        .build();
    	// when
    	// then
        client
	        .get()
	        .uri(uriFunc)
	        .exchange()
	        .expectStatus().isBadRequest()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.timestamp").isNotEmpty()
	        .jsonPath("$.path").isEqualTo("/v1/payments")
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Type should be MOBILE or CARD or PAYPAL!")
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findAll_returnsPayment_whenSuccessful_withStatus200() {
    	// given
    	var paymentId = "1234";
    	Currency kes = Currency.KES;
    	BigDecimal amount = BigDecimal.valueOf(20000l);
    	String referenceNumber = "KMDPNWPQ2444O4";
    	String occupationNumber = "12345";
    	PaymentType type = PaymentType.MOBILE;
    	PaymentStatus status = PaymentStatus.CLAIMED;
    	
    	var payment = new Payment();
    	payment.setId(paymentId);
		payment.setStatus(status);
		payment.setType(type);
    	payment.setOccupationNumber(occupationNumber);
    	payment.setReferenceNumber(referenceNumber);
		payment.setCurrency(kes);
		payment.setAmount(amount);
    	payment.setCreatedOn(LocalDateTime.now());
    	payment.setModifiedOn(LocalDateTime.now());

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/payments")
                .queryParam("status", status.getState())
                .queryParam("type", type.getType())
                .queryParam("referenceNumber", referenceNumber)
                .queryParam("order", OrderType.ASC.name())
                .build();
    	// when
    	when(paymentService.findAll(status, type, referenceNumber, OrderType.ASC)).thenReturn(Flux.just(payment));
    	// then
        client
	        .get()
	        .uri(uriFunc)
	        .exchange()
	        .expectStatus().isOk()
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.timestamp").isNotEmpty()
	        .jsonPath("$.path").isEqualTo("/v1/payments")
	        .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
	        .jsonPath("$.success").isEqualTo(true)
	        .jsonPath("$.message").isEqualTo("Payments found successfully.")
	        .jsonPath("$.data").isNotEmpty()
            .jsonPath("$.data.[0].id").isEqualTo(paymentId)
            .jsonPath("$.data.[0].status").isEqualTo(status.getState())
            .jsonPath("$.data.[0].type").isEqualTo(type.getType())
            .jsonPath("$.data.[0].occupationNumber").isEqualTo(occupationNumber)
            .jsonPath("$.data.[0].referenceNumber").isEqualTo(referenceNumber)
            .jsonPath("$.data.[0].currency").isEqualTo(kes.name())
            .jsonPath("$.data.[0].amount").isEqualTo(amount.toPlainString())
            .jsonPath("$.data.[0].createdOn").isNotEmpty()
            .jsonPath("$.data.[0].modifiedOn").isNotEmpty()
	        .consumeWith(System.out::println);    	
    }
}