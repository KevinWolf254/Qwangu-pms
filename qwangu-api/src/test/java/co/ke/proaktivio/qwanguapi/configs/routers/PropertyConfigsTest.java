package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.configs.GlobalErrorConfig;
import co.ke.proaktivio.qwanguapi.configs.properties.MpesaPropertiesConfig;
import co.ke.proaktivio.qwanguapi.configs.security.SecurityConfig;
import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.handlers.PropertyHandler;
import co.ke.proaktivio.qwanguapi.handlers.GlobalErrorWebExceptionHandler;
import co.ke.proaktivio.qwanguapi.models.Property;
import co.ke.proaktivio.qwanguapi.pojos.PropertyDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.PropertyService;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@EnableConfigurationProperties(value = {MpesaPropertiesConfig.class})
@ContextConfiguration(classes = {PropertyConfigs.class, PropertyHandler.class, SecurityConfig.class,
        GlobalErrorConfig.class, GlobalErrorWebExceptionHandler.class})
class PropertyConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private PropertyService propertyService;

    @MockBean
    private ReactiveAuthenticationManager authenticationManager;
    @MockBean
    private ServerSecurityContextRepository contextRepository;

    @Before
    public void setUp() {client = WebTestClient.bindToApplicationContext(context).build();}

    @Test
    @DisplayName("create returns unauthorised when user is not authenticated status 401")
    void create_returnsUnauthorized_status401() {
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .post()
                .uri("/v1/properties")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("create returns a Mono of property when name does not exist")
    void create_returnsProperty_whenSuccessful_withStatus201() {
        // given
        String name = "Luxury properties";
        var dto = new PropertyDto(name);
        LocalDateTime now = LocalDateTime.now();
        var property = new Property(name);
        property.setId("1");
        property.setCreatedOn(LocalDateTime.now());

        //when
        when(propertyService.create(dto)).thenReturn(Mono.just(property));

        // then
        client
                .post()
                .uri("/v1/properties")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Property created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.name").isEqualTo(name)
                .jsonPath("$.data.createdOn").isNotEmpty()
                .jsonPath("$.data.modifiedOn").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("create returns CustomAlreadyExistsException with status 400")
    void create_returnsCustomAlreadyExistsException_status400() {
        // given
        String name = "Luxury properties";
        var dto = new PropertyDto(name);

        //when
        when(propertyService.create(dto)).thenThrow(new CustomAlreadyExistsException("Property %s already exists!".formatted(name)));

        //then
        client
                .post()
                .uri("/v1/properties")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Property %s already exists!".formatted(name))
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("create returns CustomBadRequestException when property name is null or blank with status 403")
    void create_returnsCustomBadRequestException_whenPropertyNameIsNullOrBlank_status403() {
        // given
        var dto = new PropertyDto("");

        //when
        when(propertyService.create(dto)).thenThrow(new CustomBadRequestException("Name is required. Name must be at least 6 characters in length."));

        // then
        client.post()
                .uri("/v1/properties")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Name is required. Name must be at least 6 characters in length.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("create returns CustomBadRequestException when property name length is less than 6 with status 403")
    void create_returnsCustomBadRequestException_whenPropertyNameLengthLessThan6_status403() {
        // given
        var dto = new PropertyDto("Apart");

        //when
        when(propertyService.create(dto)).thenThrow(new CustomBadRequestException("Name must be at least 6 characters in length."));

        // then
        client.post()
                .uri("/v1/properties")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Name must be at least 6 characters in length.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("create returns Exception with status 500")
    void create_returnsException_status500() {
        // given
        String name = "Luxury properties";
        var dto = new PropertyDto(name);

        //when
        when(propertyService.create(dto)).thenThrow(new RuntimeException("Something happened!"));

        //then
        client.post()
                .uri("/v1/properties")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns unauthorised when user is not authenticated status 401")
    void update_returnsUnauthorized_status401() {
        // given
        String id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/properties/{id}",id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("update returns a Mono of updated property when name does not exist")
    void update_returnsUpdatedProperty_status200() {
        // given
        String name = "Luxury properties";
        var dto = new PropertyDto(name);

        String id = "1";
        String name2 = "Luxury properties B";
        LocalDateTime now = LocalDateTime.now();
        var property = new Property(name2);
        property.setId("1");
        property.setCreatedOn(now);
        property.setModifiedOn(now);

        //when
        when(propertyService.update(id, dto)).thenReturn(Mono.just(property));

        // then
        client
                .put()
                .uri("/v1/properties/{propertyId}",id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Property updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.name").isEqualTo(name2)
                .jsonPath("$.data.modifiedOn").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("update returns CustomBadRequestException when property name is null or blank with status 403")
    void update_returnsCustomBadRequestException_whenPropertyNameIsNullOrBlank_status403() {
        // given
        String id = "1";
        var dto = new PropertyDto(null);

        //when
        when(propertyService.update(id, dto)).thenThrow(new CustomBadRequestException("Name is required."));

        // then
        client
                .put()
                .uri("/v1/properties/{propertyId}",id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Name is required.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("update returns CustomBadRequestException when property name length is less than 6 with status 403")
    void update_returnsCustomBadRequestException_whenPropertyNameLengthLessThan6_status403() {
        // given
        var dto = new PropertyDto("Apart");

        //when
        when(propertyService.create(dto)).thenThrow(new CustomBadRequestException("Name must be at least 6 characters in length."));

        // then
        client.put()
                .uri("/v1/properties/{propertyId}", "1")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Name must be at least 6 characters in length.")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("update returns CustomAlreadyExistsException with status 400")
    void update_returnsCustomAlreadyExistsException_status400() {
        // given
        String id = "1";
        String name = "Luxury properties";
        var dto = new PropertyDto(name);

        //when
        when(propertyService.update(id, dto)).thenThrow(new CustomAlreadyExistsException("Property %s already exists!".formatted(name)));

        //then
        client.put()
                .uri("/v1/properties/{propertyId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
                .jsonPath("$.message").isEqualTo("Property %s already exists!".formatted(name))
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("update returns Exception with status 500")
    void update_returnsException_status500() {
        // given
        String id = "1";
        String name = "Luxury properties";
        var dto = new PropertyDto(name);

        //when
        when(propertyService.update(id, dto)).thenThrow(new RuntimeException("Something happened!"));

        //then
        client.put()
                .uri("/v1/properties/{propertyId}", id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), PropertyDto.class)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);

    }

    @Test
    @DisplayName("find returns unauthorised when user is not authenticated status 401")
    void find_returnsUnauthorized_status401() {
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/properties")
                        .queryParam("id", 1)
                        .queryParam("name", "name")
                        .queryParam("page", 1)
                        .queryParam("pageSize", 10)
                        .queryParam("order", OrderType.ASC)
                        .build();
        client
                .put()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("find returns a Flux of properties")
    void find_returnsFluxOfProperties_status200() {
        // given
        String name = "Luxury properties";
        LocalDateTime now = LocalDateTime.now();
        var property = new Property(name);
        property.setId("1");
        property.setCreatedOn(now);

        OrderType order = OrderType.ASC;

        // when
        when(propertyService.find(
                Optional.of(name),
                order)).thenReturn(Flux.just(property));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/properties")
                        .queryParam("name", name)
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
                .jsonPath("$.message").isEqualTo("Properties found successfully.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].name").isEqualTo(name)
                .jsonPath("$.data.[0].createdOn").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("find returns CustomNotFoundException with status 404")
    void find_returnsCustomNotFoundException_status404() {
        // given
        String name = "Luxury properties";
        String order = OrderType.ASC.name();

        // when
        when(propertyService.find(
                Optional.of(name),
                OrderType.valueOf(order)))
                .thenReturn(Flux.just());

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/properties")
                        .queryParam("name", name)
                        .queryParam("order", order)
                        .build();
        client
                .get()
                .uri(uriFunc)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.message").isEqualTo("Properties with those parameters do  not exist!")
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("find returns Exception with status 500")
    void find_returnsException_status500() {
        // given
        String name = "Luxury properties";

        // when
        OrderType order = OrderType.ASC;
        when(propertyService.find(
                Optional.of(name),
                order))
                .thenReturn(Flux.error(new RuntimeException("Something happened!")));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/properties")
                        .queryParam("name", name)
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
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("Delete by id  returns unauthorised when user is not authenticated status 401")
    void deleteById_returnsUnauthorized_status401() {
        // given
        String id = "1";
        // when
        when(contextRepository.load(any())).thenReturn(Mono.empty());
        // then
        client
                .put()
                .uri("/v1/properties/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("Delete by id returns a Mono of boolean when id exists")
    void deleteById_returnsTrue_status200() {
        // given
        String id = "1";

        // when
        when(propertyService.deleteById(id))
                .thenReturn(Mono.just(true));

        // then
        client
                .delete()
                .uri("/v1/properties/{propertyId}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Property with id %s deleted successfully.".formatted(id))
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("deleteById returns CustomNotFoundException with status 404")
    void deleteById_returnsCustomNotFoundException_status404() {
        // given
        String id = "1";

        // when
        when(propertyService.deleteById(id))
                .thenReturn(Mono.just(false));

        // then
        client
                .delete()
                .uri("/v1/properties/{propertyId}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.status").isEqualTo(HttpStatus.OK.value())
                .jsonPath("$.message").isEqualTo("Property with id %s does not exist!".formatted(id))
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @WithMockUser(roles = {"SUPER_ADMIN"})
    @DisplayName("deleteById returns Exception with status 500")
    void deleteById_returnsException_status500() {
        // given
        String id = "1";

        // when
        when(propertyService.deleteById(id))
                .thenReturn(Mono.error(new RuntimeException("Something happened!")));

        // then
        client
                .delete()
                .uri("/v1/properties/{propertyId}", id)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .jsonPath("$.data").isEmpty()
                .consumeWith(System.out::println);
    }
}