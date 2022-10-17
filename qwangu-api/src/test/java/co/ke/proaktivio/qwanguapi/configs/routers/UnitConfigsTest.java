package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.handlers.UnitHandler;
import co.ke.proaktivio.qwanguapi.models.Unit;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.UnitDto;
import co.ke.proaktivio.qwanguapi.services.UnitService;
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

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {UnitConfigs.class, UnitHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
public class UnitConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private UnitService unitService;
    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    @Before
    public void setUp() {client = WebTestClient.bindToApplicationContext(context).build();}

    @Test
    @DisplayName("create returns unauthorised when user is not authenticated status 401")
    void create_returnsUnauthorized_status401() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/units")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void create_returnsUnit_whenSuccessful_withStatus201() {
        // given
        var dto = new UnitDto(Unit.Status.VACANT, Unit.Type.APARTMENT_UNIT, Unit.Identifier.A, 0, 2,
                2, 2, Unit.Currency.KES, BigDecimal.valueOf(25000), BigDecimal.valueOf(500),
                BigDecimal.valueOf(300), "1");
//        var unit = new Unit("1", Unit.Status.VACANT, false, "TE99", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.A, 0, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(25000), BigDecimal.valueOf(500),BigDecimal.valueOf(300), LocalDateTime.now(),
//                null, "1");
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
                .booked(false)
                .accountNo("TE99")
                .type(Unit.Type.APARTMENT_UNIT)
                .identifier(Unit.Identifier.A)
                .floorNo(0)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(25000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId("1").build();
        unit.setId("1");
        unit.setCreatedOn(LocalDateTime.now());
        var dtoNonApartmentUnit = new UnitDto(Unit.Status.VACANT, Unit.Type.MAISONETTES, null, null,
                2, 2, 2, Unit.Currency.KES, BigDecimal.valueOf(25000),
                BigDecimal.valueOf(500), BigDecimal.valueOf(300), null);
//        var unitNonApartment = new Unit("2", Unit.Status.VACANT, false, "TE99", Unit.Type.MAISONETTES,
//                null, 0, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(25000), BigDecimal.valueOf(500), BigDecimal.valueOf(300), LocalDateTime.now(),
//                null, null);
        var unitNonApartment = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
                .booked(false)
                .accountNo("TE99")
                .type(Unit.Type.MAISONETTES)
                .floorNo(0)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(25000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300)).build();
        unitNonApartment.setId("2");
        unitNonApartment.setCreatedOn(LocalDateTime.now());
        var dtoFailsValidation = new UnitDto(null, null, null, null, null, null,
                null, null, null, null, null, null);
        var dtoApartmentUnitFailsValidation = new UnitDto(Unit.Status.VACANT, Unit.Type.APARTMENT_UNIT, null, null, null, null,
                null, null, null, null, null, null);
        var dtoNoneApartmentUnitFailsValidation = new UnitDto(Unit.Status.VACANT, Unit.Type.MAISONETTES, null, null, null, null,
                null, null, null, null, null, null);

        // when
        when(unitService.create(dto)).thenReturn(Mono.just(unit));

        // then
        client
                .post()
                .uri("/v1/units")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UnitDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Unit created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.accountNo").isEqualTo("TE99")
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .consumeWith(System.out::println);

        // when
        when(unitService.create(dtoNonApartmentUnit)).thenReturn(Mono.just(unitNonApartment));

        // then
        client
                .post()
                .uri("/v1/units")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNonApartmentUnit), UnitDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Unit created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("2")
                .jsonPath("$.data.accountNo").isEqualTo("TE99")
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .consumeWith(System.out::println);

        // when
        when(unitService.create(dtoFailsValidation)).thenReturn(Mono.just(unitNonApartment));

        // then
        client
                .post()
                .uri("/v1/units")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoFailsValidation), UnitDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Type is required. No of bedrooms is required. No of bathrooms is required. Advance in months is required. Currency is required. Rent per month is required. Security per month is required. Garbage per month is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

        // when
        when(unitService.create(dtoApartmentUnitFailsValidation)).thenReturn(Mono.just(unitNonApartment));

        // then
        client
                .post()
                .uri("/v1/units")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoApartmentUnitFailsValidation), UnitDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Identifier is required. Floor No is required. No of bedrooms is required. No of bathrooms is required. Advance in months is required. Currency is required. Rent per month is required. Security per month is required. Garbage per month is required. Apartment Id is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

        // when
        when(unitService.create(dtoNoneApartmentUnitFailsValidation)).thenReturn(Mono.just(unitNonApartment));

        // then
        client
                .post()
                .uri("/v1/units")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dtoNoneApartmentUnitFailsValidation), UnitDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("No of bedrooms is required. No of bathrooms is required. Advance in months is required. Currency is required. Rent per month is required. Security per month is required. Garbage per month is required.")
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
                .uri("/v1/units/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    void update_returnsUnit_whenSuccessful_withStatus200() {
        // given
        var id = "1";
        var dto = new UnitDto(Unit.Status.VACANT, Unit.Type.APARTMENT_UNIT, Unit.Identifier.A, 0, 2,
                2, 2, Unit.Currency.KES, BigDecimal.valueOf(25000), BigDecimal.valueOf(500),
                BigDecimal.valueOf(300), "1");
//        var unit = new Unit(id, Unit.Status.VACANT, false, "TE99", Unit.Type.APARTMENT_UNIT,
//                Unit.Identifier.A, 0, 2, 1, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(25000), BigDecimal.valueOf(500),BigDecimal.valueOf(300), LocalDateTime.now(),
//                null, "1");
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
                .booked(false)
                .accountNo("TE99")
                .type(Unit.Type.APARTMENT_UNIT)
                .identifier(Unit.Identifier.A)
                .floorNo(0)
                .noOfBedrooms(2)
                .noOfBathrooms(1)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(25000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300)).build();
        unit.setId(id);
        unit.setCreatedOn(LocalDateTime.now());
        // when
        when(unitService.update(id, dto)).thenReturn(Mono.just(unit));

        // then
        client
                .put()
                .uri("/v1/units/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), UnitDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Unit updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.accountNo").isEqualTo("TE99")
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void find_returnsUnauthorized_status401() {
        // given
        var id = "1";
        String accountNo = "TE99";
        Unit.Type type = Unit.Type.APARTMENT_UNIT;
        Unit.Identifier identifier = Unit.Identifier.A;
        int floorNo = 0;
        int noOfBedrooms = 2;
        int noOfBathrooms = 1;
        String apartmentId = "1";

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/units")
                        .queryParam("id", id)
                        .queryParam("accountNo", accountNo)
                        .queryParam("type", type.name())
                        .queryParam("identifier", identifier.name())
                        .queryParam("floorNo", floorNo)
                        .queryParam("noOfBedrooms", noOfBedrooms)
                        .queryParam("noOfBathrooms", noOfBathrooms)
                        .queryParam("apartmentId", apartmentId)
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
    void find_returnsUnits_whenSuccessful_withStatus200() {
        // given
        var id = "1";
        String accountNo = "TE99";
        Unit.Type type = Unit.Type.APARTMENT_UNIT;
        Unit.Identifier identifier = Unit.Identifier.A;
        int floorNo = 0;
        int noOfBedrooms = 2;
        int noOfBathrooms = 1;
        String apartmentId = "1";
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        OrderType order = OrderType.ASC;
//        var unit = new Unit(id, Unit.Status.VACANT, false, accountNo, type, identifier,
//                floorNo, noOfBedrooms, noOfBathrooms, 2, Unit.Currency.KES,
//                BigDecimal.valueOf(25000), BigDecimal.valueOf(500),BigDecimal.valueOf(300), LocalDateTime.now(),
//                null, apartmentId);
        var unit = new Unit.UnitBuilder()
                .status(Unit.Status.VACANT)
                .booked(false)
                .accountNo(accountNo)
                .type(type)
                .identifier(identifier)
                .floorNo(floorNo)
                .noOfBedrooms(noOfBedrooms)
                .noOfBathrooms(noOfBathrooms)
                .advanceInMonths(2)
                .currency(Unit.Currency.KES)
                .rentPerMonth(BigDecimal.valueOf(25000))
                .securityPerMonth(BigDecimal.valueOf(500))
                .garbagePerMonth(BigDecimal.valueOf(300))
                .apartmentId(apartmentId).build();
        unit.setId(id);

        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/units")
                        .queryParam("unitId", id)
                        .queryParam("status", "VACANT")
                        .queryParam("accountNo", accountNo)
                        .queryParam("type", "APARTMENT_UNIT")
                        .queryParam("identifier", identifier.name())
                        .queryParam("floorNo", floorNo)
                        .queryParam("noOfBedrooms", noOfBedrooms)
                        .queryParam("noOfBathrooms", noOfBathrooms)
                        .queryParam("apartmentId", apartmentId)
                        .queryParam("page", page)
                        .queryParam("pageSize", pageSize)
                        .queryParam("order", order)
                        .build();

        // when
        when(unitService.findPaginated(
                Optional.of(Unit.Status.VACANT),
                Optional.of(accountNo),
                Optional.of(type),
                Optional.of(identifier),
                Optional.of(floorNo),
                Optional.of(noOfBedrooms),
                Optional.of(noOfBathrooms),
                Optional.of(apartmentId),
                finalPage,
                finalPageSize,
                order
                )).thenReturn(Flux.just(unit));

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
                .jsonPath("$.message").isEqualTo("Units found successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].accountNo").isEqualTo("TE99")
                .consumeWith(System.out::println);

        Function<UriBuilder, URI> uriFunc2 = uriBuilder ->
                uriBuilder
                        .path("/v1/units")
                        .queryParam("status", "NOT_TYPE")
                        .build();

        Function<UriBuilder, URI> uriFunc3 = uriBuilder ->
                uriBuilder
                        .path("/v1/units")
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
                .jsonPath("$.message").isEqualTo("Status should be VACANT or OCCUPIED!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
        // then
        client
                .get()
                .uri(uriFunc3)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Type should be APARTMENT_UNIT or TOWN_HOUSE or MAISONETTES or VILLA!")
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
                .uri("/v1/units/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }
    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("Delete by id returns a Mono of boolean when id exists")
    void deleteById_returnsTrue_withStatus200() {
        // given
        String id = "1";

        // when
        when(unitService.deleteById(id)).thenReturn(Mono.just(true));

        // then
        client
                .delete()
                .uri("/v1/units/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Unit with id %s deleted successfully.".formatted(id))
                .consumeWith(System.out::println);
    }
}
