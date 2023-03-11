package co.ke.proaktivio.qwanguapi.configs.routers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
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
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.handlers.OccupationTransactionHandler;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import co.ke.proaktivio.qwanguapi.models.OccupationTransaction.Type;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import co.ke.proaktivio.qwanguapi.services.OccupationTransactionService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {OccupationTransactionConfig.class, OccupationTransactionHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class OccupationTransactionConfigTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private OccupationTransactionService occupationTransactionService;
    @MockBean
    private InvoiceService invoiceService;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    void findById_returnsUnauthorized_status401() {
        // given
        var occupationTransactionId = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/occupationTransactions/{occupationTransactionId}", occupationTransactionId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsNotFound_status404_whenReceiptIsNotFound() {
        // given      
    	var occupationTransactionId = "1";
        
        //when
        when(occupationTransactionService.findById("1")).thenReturn(Mono.empty());
        // then
        client
	        .get()
	        .uri("/v1/occupationTransactions/{occupationTransactionId}", occupationTransactionId)
	        .accept(MediaType.APPLICATION_JSON)
	        .exchange()
	        .expectStatus().isNotFound()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Occupation transaction with id %s does not exist!".formatted(occupationTransactionId))
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsOccupationTransaction_whenSuccessful() {
        // given
        var occupationTransactionId = "1";
        var occupationTransaction = new OccupationTransaction();
        occupationTransaction.setId(occupationTransactionId);
        occupationTransaction.setType(Type.DEBIT);
        occupationTransaction.setCreatedBy("SYSTEM");
        occupationTransaction.setCreatedOn(LocalDateTime.now());
        occupationTransaction.setInvoiceId("1");
        occupationTransaction.setOccupationId("1");
        occupationTransaction.setTotalAmountOwed(BigDecimal.valueOf(25000l));
        occupationTransaction.setTotalAmountCarriedForward(BigDecimal.valueOf(-25000l));
        occupationTransaction.setModifiedOn(LocalDateTime.now());
        occupationTransaction.setModifiedBy("SYSTEM");
        
        //when
        when(occupationTransactionService.findById(occupationTransactionId)).thenReturn(Mono.just(occupationTransaction));

        // then
        client
                .get()
                .uri("/v1/occupationTransactions/{occupationTransactionId}", occupationTransactionId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Occupation Transaction found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.type").isEqualTo(Type.DEBIT.getType())
                .jsonPath("$.data.occupationId").isEqualTo("1")
                .jsonPath("$.data.invoiceId").isEqualTo("1")
                .jsonPath("$.data.receiptId").isEmpty()
                .jsonPath("$.data.totalAmountOwed").isEqualTo(BigDecimal.valueOf(25000l))
                .jsonPath("$.data.totalAmountPaid").isEmpty()
                .jsonPath("$.data.totalAmountCarriedForward").isEqualTo(BigDecimal.valueOf(-25000l))
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void find_returnsUnauthorized_status401() {
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/occupationTransactions")
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
    void find_returnsEmpty_whenNoOccupationExists() {
    	// given
		String occupationId = "12345";

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        		uriBuilder
				        .path("/v1/occupationTransactions")
				        .queryParam("occupationId", occupationId)
				        .build();

        // when
        when(occupationTransactionService.findAll(
                null,
                occupationId,
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
                .jsonPath("$.message").isEqualTo("Occupation Transactions with those parameters do not exist!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }


    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void find_returnsBadRequest_whenTypeIsInvalid() {
        // given
        Function<UriBuilder, URI> uriFunc2 = uriBuilder ->
                uriBuilder
                        .path("/v1/occupationTransactions")
                        .queryParam("type", "NOT_TYPE")
                        .build();
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
                .jsonPath("$.message").isEqualTo("Type should be CREDIT or DEBIT!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void find_returnsBadRequest_whenServiceReturnsCustomBadRequestException() {
        // given
        Function<UriBuilder, URI> uriFunc2 = uriBuilder ->
                uriBuilder
                        .path("/v1/occupationTransactions")
                        .build();

        // when
        String message = "Choose either invoiceId or receiptId. Both will not exist!";
		when(occupationTransactionService.findAll(
                null,
                null,
                null,
                null,
                OrderType.DESC
        )).thenReturn(Flux.error(new CustomBadRequestException(message)));
        
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
                .jsonPath("$.message").isEqualTo(message)
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void find_returnsOccupationTransaction_whenSuccessful() {
        // given
        var occupationTransactionId = "1";
        var occupationTransaction = new OccupationTransaction();
        occupationTransaction.setId(occupationTransactionId);
        occupationTransaction.setType(Type.DEBIT);
        occupationTransaction.setCreatedBy("SYSTEM");
        occupationTransaction.setCreatedOn(LocalDateTime.now());
        occupationTransaction.setInvoiceId("1");
        occupationTransaction.setOccupationId("1");
        occupationTransaction.setTotalAmountOwed(BigDecimal.valueOf(25000l));
        occupationTransaction.setTotalAmountCarriedForward(BigDecimal.valueOf(-25000l));
        occupationTransaction.setModifiedOn(LocalDateTime.now());
        occupationTransaction.setModifiedBy("SYSTEM");
        
        //when
		when(occupationTransactionService.findAll(
                null,
                null,
                null,
                null,
                OrderType.DESC
        )).thenReturn(Flux.just(occupationTransaction));
        // then
        client
                .get()
                .uri("/v1/occupationTransactions")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Occupation Transactions found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].type").isEqualTo(Type.DEBIT.getType())
                .jsonPath("$.data.[0].occupationId").isEqualTo("1")
                .jsonPath("$.data.[0].invoiceId").isEqualTo("1")
                .jsonPath("$.data.[0].receiptId").isEmpty()
                .jsonPath("$.data.[0].totalAmountOwed").isEqualTo(BigDecimal.valueOf(25000l))
                .jsonPath("$.data.[0].totalAmountPaid").isEmpty()
                .jsonPath("$.data.[0].totalAmountCarriedForward").isEqualTo(BigDecimal.valueOf(-25000l))
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .jsonPath("$.data.[0].modifiedOn").isNotEmpty()
                .consumeWith(System.out::println);
    }
}
