package co.ke.proaktivio.qwanguapi.configs.routers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.function.Function;

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

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.ReceiptHandler;
import co.ke.proaktivio.qwanguapi.models.Receipt;
import co.ke.proaktivio.qwanguapi.pojos.CreateNoticeDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.ReceiptDto;
import co.ke.proaktivio.qwanguapi.services.ReceiptService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {ReceiptConfigs.class, ReceiptHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class ReceiptConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
	private ReceiptService receiptService;
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
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/receipts")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenOccupationIdHasNoText() {
        // given
        var dto = new ReceiptDto();
        dto.setPaymentId("1");
        //when
        when(receiptService.create(dto)).thenReturn(Mono.empty());
        // then
        client.post()
                .uri("/v1/receipts")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ReceiptDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Occupation id is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenPaymentIdHasNoText() {
        // given
        var dto = new ReceiptDto();
        dto.setOccupationId("1");
        //when
        when(receiptService.create(dto)).thenReturn(Mono.empty());
        // then
        client.post()
                .uri("/v1/receipts")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ReceiptDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Payment id is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsReceipt_status200_whenSuccessful() {
        // given
        var dto = new ReceiptDto();
        dto.setPaymentId("1");
        dto.setOccupationId("1");
        
        var receipt = new Receipt();
        receipt.setCreatedBy("SYSTEM");
        receipt.setCreatedOn(LocalDateTime.now());
        receipt.setId("1");
        receipt.setModifiedBy("SYSTEM");
        receipt.setModifiedOn(LocalDateTime.now());
        receipt.setNumber("123456");
        receipt.setOccupationId("1");
        receipt.setPaymentId("1");
        
        //when
        when(receiptService.create(dto)).thenReturn(Mono.just(receipt));
        // then
        client
	        .post()
	        .uri("/v1/receipts")
	        .accept(MediaType.APPLICATION_JSON)
	        .body(Mono.just(dto), CreateNoticeDto.class)
	        .exchange()
	        .expectStatus().isCreated()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(true)
	        .jsonPath("$.message").isEqualTo("Receipt created successfully.")
	        .jsonPath("$.data").isNotEmpty()
	        .jsonPath("$.data.id").isEqualTo("1")
	        .jsonPath("$.data.number").isEqualTo("123456")
	        .jsonPath("$.data.occupationId").isEqualTo("1")
	        .jsonPath("$.data.paymentId").isEqualTo("1")
	        .jsonPath("$.data.createdOn").isNotEmpty()
	        .jsonPath("$.data.createdBy").isEqualTo("SYSTEM")
	        .jsonPath("$.data.modifiedOn").isNotEmpty()
	        .jsonPath("$.data.modifiedBy").isEqualTo("SYSTEM")
	        .consumeWith(System.out::println);
    }

    @Test
    void findById_returnsUnauthorized_status401_whenNotAuthorized() {
        // given
        var receiptId = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/receipts/{receiptId}", receiptId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsNotFound_status404_whenReceiptIsNotFound() {
        // given      
    	var receiptId = "1";
        
        //when
        when(receiptService.findById("1")).thenReturn(Mono.empty());
        // then
        client
	        .get()
	        .uri("/v1/receipts/{receiptId}", receiptId)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
	        .expectStatus().isNotFound()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Receipt with id %s does not exist!".formatted(receiptId))
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsReceipt_status200_whenReceiptIsFound() {
        // given      
    	var receiptId = "1";
        var receipt = new Receipt();
        receipt.setCreatedBy("SYSTEM");
        receipt.setCreatedOn(LocalDateTime.now());
        receipt.setId(receiptId);
        receipt.setModifiedBy("SYSTEM");
        receipt.setModifiedOn(LocalDateTime.now());
        receipt.setNumber("123456");
        receipt.setOccupationId("1");
        receipt.setPaymentId("1");
        
        //when
        when(receiptService.findById("1")).thenReturn(Mono.just(receipt));
        // then
        client
	        .get()
	        .uri("/v1/receipts/{receiptId}", receiptId)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
	        .expectStatus().isOk()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(true)
	        .jsonPath("$.message").isEqualTo("Receipt found successfully.")
	        .jsonPath("$.data").isNotEmpty()
	        .jsonPath("$.data.id").isEqualTo("1")
	        .jsonPath("$.data.number").isEqualTo("123456")
	        .jsonPath("$.data.occupationId").isEqualTo("1")
	        .jsonPath("$.data.paymentId").isEqualTo("1")
	        .jsonPath("$.data.createdOn").isNotEmpty()
	        .jsonPath("$.data.createdBy").isEqualTo("SYSTEM")
	        .jsonPath("$.data.modifiedOn").isNotEmpty()
	        .jsonPath("$.data.modifiedBy").isEqualTo("SYSTEM")
	        .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void findAll_returnsUnauthorized_status401() {
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/receipts")
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
    void find_returnsSuccessAsFalse_status200_whenReceiptsDoNotExist() {
    	// given
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        		uriBuilder
				        .path("/v1/receipts")
				        .build();

        // when
        when(receiptService.findAll(
                null,
                null,
                OrderType.DESC
        )).thenReturn(Flux.empty());
        // then
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Receipts could not be found!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void find_returnsSuccessAsTrue_status200_whenReceiptsExist() {
    	// given
    	var occupationId = "1";
    	var paymentId = "123";
		var receiptId = "12345";
		OrderType orderType = OrderType.DESC;
		var order = orderType.name();
		
        var receipt = new Receipt();
        String number = "123456";
        receipt.setCreatedBy("SYSTEM");
        receipt.setCreatedOn(LocalDateTime.now());
        receipt.setId(receiptId);
        receipt.setModifiedBy("SYSTEM");
        receipt.setModifiedOn(LocalDateTime.now());
		receipt.setNumber(number);
        receipt.setOccupationId(occupationId);
        receipt.setPaymentId(paymentId);
        
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        		uriBuilder
				        .path("/v1/receipts")
				        .queryParam("occupationId", occupationId)
				        .queryParam("paymentId", paymentId)
				        .queryParam("order", order)
				        .build();

        // when
        when(receiptService.findAll(
        		occupationId,
        		paymentId,
                orderType
        )).thenReturn(Flux.just(receipt));
        // then
        client
	        .get()
            .uri(uriFunc)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
	        .expectStatus().isOk()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(true)
	        .jsonPath("$.message").isEqualTo("Receipts found successfully.")
	        .jsonPath("$.data").isNotEmpty()
	        .jsonPath("$.data.[0].id").isEqualTo(receiptId)
	        .jsonPath("$.data.[0].number").isEqualTo(number)
	        .jsonPath("$.data.[0].occupationId").isEqualTo(occupationId)
	        .jsonPath("$.data.[0].paymentId").isEqualTo(paymentId)
	        .jsonPath("$.data.[0].createdOn").isNotEmpty()
	        .jsonPath("$.data.[0].createdBy").isEqualTo("SYSTEM")
	        .jsonPath("$.data.[0].modifiedOn").isNotEmpty()
	        .jsonPath("$.data.[0].modifiedBy").isEqualTo("SYSTEM")
	        .consumeWith(System.out::println);
    }
}
