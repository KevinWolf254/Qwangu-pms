package co.ke.proaktivio.qwanguapi.configs.routers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.Before;
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
import co.ke.proaktivio.qwanguapi.handlers.InvoiceHandler;
import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Invoice.Type;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import co.ke.proaktivio.qwanguapi.pojos.InvoiceDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.InvoiceService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {InvoiceConfigs.class, InvoiceHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class InvoiceConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
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
    void create_returnsUnauthorized_status401_whenUserIsUnauthorised() {
    	when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/invoices")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenTypeIsNullAndOccupationIdHasNoText() {
        // given
        var dto = new InvoiceDto();

        // then
        client.post()
                .uri("/v1/invoices")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), InvoiceDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Type is required. Occupation id is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenTypeIsRent_andStartDateAndEndDateAreNull() {
        // given
        var dto = new InvoiceDto();
        dto.setType(Type.RENT);
        dto.setOccupationId("1");

        // then
        client.post()
                .uri("/v1/invoices")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), InvoiceDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Start date is required. End date is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenTypeIsUtilities_andStartDateAndEndDateAndOtherAmountsAreNull() {
        // given
        var dto = new InvoiceDto();
        dto.setType(Type.UTILITIES);
        dto.setOccupationId("1");

        // then
        client.post()
                .uri("/v1/invoices")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), InvoiceDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Start date is required. End date is required. Currency is required. Other amounts is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenTypeIsUtilities_andOtherAmountsIsEmpty() {
        // given
        var dto = new InvoiceDto();
        dto.setType(Type.UTILITIES);
        dto.setOccupationId("1");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(15));
        dto.setOtherAmounts(new HashMap<>());

        // then
        client.post()
                .uri("/v1/invoices")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), InvoiceDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Currency is required. OtherAmounts should not be empty!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @SuppressWarnings("serial")
	@Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsBadRequest_status400_whenTypeIsUtilities_andStartDateIsAfterEndDate() {
        // given
        var dto = new InvoiceDto();
        dto.setType(Type.UTILITIES);
        dto.setOccupationId("1");
        dto.setStartDate(LocalDate.now().plusDays(15));
        dto.setEndDate(LocalDate.now());
        dto.setOtherAmounts(new HashMap<>() {{put("SECURITY", BigDecimal.valueOf(500l));}});

        // then
        client.post()
                .uri("/v1/invoices")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), InvoiceDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("End date should be after Start date! Currency is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsInvoice_status200_whenSuccessful() {
        // given
		var occupationId = "1";
		Type type = Invoice.Type.UTILITIES;
        var currency = Currency.KES;
        LocalDate now = LocalDate.now();
        LocalDate endDate = now.plusDays(15);
        Map<String, BigDecimal> otherAmounts = new HashMap<>();
        otherAmounts.put("SECURITY", BigDecimal.valueOf(500l));
        
		var dto = new InvoiceDto();
        dto.setType(type);
        dto.setOccupationId(occupationId);
		dto.setStartDate(now);
		dto.setEndDate(endDate);
		dto.setCurrency(currency);
		dto.setOtherAmounts(otherAmounts);

		var invoiceId = "1";
		var invoiceNo = "12345";
		Invoice invoice = new Invoice.InvoiceBuilder()
				.type(type)
				.startDate(now)
				.endDate(endDate)
				.currency(currency)
				.otherAmounts(otherAmounts)
				.occupationId(occupationId)
				.build();
		invoice.setId(invoiceId);
		invoice.setNumber(invoiceNo);
		invoice.setCreatedBy("SYSTEM");
		invoice.setCreatedOn(LocalDateTime.now());
		invoice.setModifiedBy("SYSTEM");
		invoice.setModifiedOn(LocalDateTime.now());

        //when
		when(invoiceService.create(dto)).thenReturn(Mono.just(invoice));
		
        // then
        client.post()
                .uri("/v1/invoices")
                .accept(MediaType.APPLICATION_JSON)
    	        .body(Mono.just(dto), InvoiceDto.class)
    	        .exchange()
    	        .expectStatus().isCreated()
    	        .expectHeader().contentType("application/json")
    	        .expectBody()
    	        .jsonPath("$").isNotEmpty()
    	        .jsonPath("$.success").isEqualTo(true)
    	        .jsonPath("$.message").isEqualTo("Invoice created successfully.")
    	        .jsonPath("$.data").isNotEmpty()
    	        .jsonPath("$.data.id").isEqualTo("1")
    	        .jsonPath("$.data.number").isEqualTo(invoiceNo)
    	        .jsonPath("$.data.type").isEqualTo(type.name())
    	        .jsonPath("$.data.startDate").isNotEmpty()
    	        .jsonPath("$.data.endDate").isNotEmpty()
    	        .jsonPath("$.data.currency").isEqualTo(currency.name())
    	        .jsonPath("$.data.otherAmounts").isNotEmpty()
    	        .jsonPath("$.data.occupationId").isEqualTo(occupationId)
    	        .jsonPath("$.data.createdOn").isNotEmpty()
    	        .jsonPath("$.data.createdBy").isEqualTo("SYSTEM")
    	        .jsonPath("$.data.modifiedOn").isNotEmpty()
    	        .jsonPath("$.data.modifiedBy").isEqualTo("SYSTEM")
    	        .consumeWith(System.out::println);
    }

    @Test
    void findById_returnsUnauthorized_status401() {
        // given
        var invoiceId = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/invoices/{invoiceId}", invoiceId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
	@Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
	void findById_returnsNotFound_whenInvoiceDoesNotExist() {
		// given
		var invoiceId = "1";

		// when
        when(invoiceService.findById(invoiceId)).thenReturn(Mono.empty());
        
        // then
        client
	        .get()
	        .uri("/v1/invoices/{invoiceId}", invoiceId)
	        .exchange()
	        .expectStatus().isNotFound()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Invoice with id %s does not exist!".formatted(invoiceId))
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);
	}

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void findById_returnsInvoice_status200_whenSuccessful() {
        // given
		var occupationId = "1";
		Type type = Invoice.Type.UTILITIES;
        var currency = Currency.KES;
        LocalDate now = LocalDate.now();
        LocalDate endDate = now.plusDays(15);
        Map<String, BigDecimal> otherAmounts = new HashMap<>();
        otherAmounts.put("SECURITY", BigDecimal.valueOf(500l));
        
		var invoiceId = "1";
		var invoiceNo = "12345";
		Invoice invoice = new Invoice.InvoiceBuilder()
				.type(type)
				.startDate(now)
				.endDate(endDate)
				.currency(currency)
				.otherAmounts(otherAmounts)
				.occupationId(occupationId)
				.build();
		invoice.setId(invoiceId);
		invoice.setNumber(invoiceNo);
		invoice.setCreatedBy("SYSTEM");
		invoice.setCreatedOn(LocalDateTime.now());
		invoice.setModifiedBy("SYSTEM");
		invoice.setModifiedOn(LocalDateTime.now());
		
        //when
        when(invoiceService.findById(invoiceId)).thenReturn(Mono.just(invoice));

        // then
        client
                .get()
                .uri("/v1/invoices/{invoiceId}", invoiceId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
    	        .jsonPath("$").isNotEmpty()
    	        .jsonPath("$.success").isEqualTo(true)
    	        .jsonPath("$.message").isEqualTo("Invoice found successfully.")
    	        .jsonPath("$.data").isNotEmpty()
    	        .jsonPath("$.data.id").isEqualTo("1")
    	        .jsonPath("$.data.number").isEqualTo(invoiceNo)
    	        .jsonPath("$.data.type").isEqualTo(type.name())
    	        .jsonPath("$.data.startDate").isNotEmpty()
    	        .jsonPath("$.data.endDate").isNotEmpty()
    	        .jsonPath("$.data.currency").isEqualTo(currency.name())
    	        .jsonPath("$.data.otherAmounts").isNotEmpty()
    	        .jsonPath("$.data.occupationId").isEqualTo(occupationId)
    	        .jsonPath("$.data.createdOn").isNotEmpty()
    	        .jsonPath("$.data.createdBy").isEqualTo("SYSTEM")
    	        .jsonPath("$.data.modifiedOn").isNotEmpty()
    	        .jsonPath("$.data.modifiedBy").isEqualTo("SYSTEM")
    	        .consumeWith(System.out::println);
    }

    @Test
    void findAll_returnsUnauthorized_status401() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .get()
                .uri("/v1/invoices")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
	@Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
	void findAll_returnsBadRequest_whenTypeIsOfUnknownType() {
		// given
		var wrongType = "WRONG_TYPE";
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/invoices")
                .queryParam("type", wrongType)
                .build();
		// then
        client
	        .get()
	        .uri(uriFunc)
	        .exchange()
	        .expectStatus().isBadRequest()
	        .expectHeader().contentType("application/json")
	        .expectBody()
	        .jsonPath("$").isNotEmpty()
	        .jsonPath("$.success").isEqualTo(false)
	        .jsonPath("$.message").isEqualTo("Type should be RENT_ADVANCE or RENT or PENALTY or UTILITIES!")
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);    
	}
    
	@Test    
	@WithMockUser(roles = {"SUPER_ADMIN"})
	void findAll_returnsEmpty_status200_whenInvoicesDoNotExist() {
		// given
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/invoices")
                .build();
		// when
        when(invoiceService.findAll(null, null, null, OrderType.DESC)).thenReturn(Flux.empty());

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
	        .jsonPath("$.message").isEqualTo("Invoices could not be found!")
	        .jsonPath("$.data").isEmpty()
	        .consumeWith(System.out::println);        
	}
    
	@Test
	@WithMockUser(roles = {"SUPER_ADMIN"})
	void findAll_returnsInvoices_whenSuccessfullyFound() {
		// given
		var occupationId = "1";
		Type type = Invoice.Type.UTILITIES;
        var currency = Currency.KES;
        LocalDate now = LocalDate.now();
        LocalDate endDate = now.plusDays(15);
        Map<String, BigDecimal> otherAmounts = new HashMap<>();
        otherAmounts.put("SECURITY", BigDecimal.valueOf(500l));
        
		var invoiceId = "1";
		var invoiceNo = "12345";
		Invoice invoice = new Invoice.InvoiceBuilder()
				.type(type)
				.startDate(now)
				.endDate(endDate)
				.currency(currency)
				.otherAmounts(otherAmounts)
				.occupationId(occupationId)
				.build();
		invoice.setId(invoiceId);
		invoice.setNumber(invoiceNo);
		invoice.setCreatedBy("SYSTEM");
		invoice.setCreatedOn(LocalDateTime.now());
		invoice.setModifiedBy("SYSTEM");
		invoice.setModifiedOn(LocalDateTime.now());
		
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
        uriBuilder
                .path("/v1/invoices")
                .build();
		// when
        when(invoiceService.findAll(null, null, null, OrderType.DESC)).thenReturn(Flux.just(invoice));
		
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
	        .jsonPath("$.message").isEqualTo("Invoices found successfully.")
	        .jsonPath("$.data").isNotEmpty()
	        .jsonPath("$.data.[0].id").isEqualTo("1")
	        .jsonPath("$.data.[0].number").isEqualTo(invoiceNo)
	        .jsonPath("$.data.[0].type").isEqualTo(type.name())
	        .jsonPath("$.data.[0].startDate").isNotEmpty()
	        .jsonPath("$.data.[0].endDate").isNotEmpty()
	        .jsonPath("$.data.[0].currency").isEqualTo(currency.name())
	        .jsonPath("$.data.[0].otherAmounts").isNotEmpty()
	        .jsonPath("$.data.[0].occupationId").isEqualTo(occupationId)
	        .jsonPath("$.data.[0].createdOn").isNotEmpty()
	        .jsonPath("$.data.[0].createdBy").isEqualTo("SYSTEM")
	        .jsonPath("$.data.[0].modifiedOn").isNotEmpty()
	        .jsonPath("$.data.[0].modifiedBy").isEqualTo("SYSTEM")
	        .consumeWith(System.out::println);
	}

}
