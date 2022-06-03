package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.ApartmentHandler;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.ErrorCode;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.pojos.SuccessResponse;
import co.ke.proaktivio.qwanguapi.services.ApartmentService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import org.junit.Before;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

@ContextConfiguration(classes = {ApartmentConfigs.class, ApartmentHandler.class})
@WebFluxTest
class ApartmentConfigsTest {
    @Autowired
    private ApplicationContext context;
    @Autowired
    private WebTestClient client;
    @MockBean
    private ApartmentService apartmentService;

    @Before
    public void setUp() {
        client = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    @DisplayName("create returns a Mono of Apartment when name does not exist")
    void create_returnsMonoApartment_status201() {
        // given
        String name = "Luxury Apartments";
        var dto = new ApartmentDto(name);
        LocalDateTime now = LocalDateTime.now();
        var apartment = new Apartment("1", name, now, null);

        //when
        Mockito.when(apartmentService.create(dto)).thenReturn(Mono.just(apartment));

        // then
        client.post()
                .uri("/v1/apartments")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Apartment created successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.name").isEqualTo(name)
                .jsonPath("$.data.created").isNotEmpty()
                .jsonPath("$.data.modified").isEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("create returns CustomAlreadyExistsException with status 400")
    void create_returnsCustomAlreadyExistsException_status400() {
        // given
        String name = "Luxury Apartments";
        var dto = new ApartmentDto(name);

        //when
        Mockito.when(apartmentService.create(dto)).thenThrow(new CustomAlreadyExistsException("Apartment %s already exists!".formatted(name)));

        //then
        client.post()
                .uri("/v1/apartments")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Apartment %s already exists!".formatted(name))
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("create returns CustomBadRequestException when apartment name is null or blank with status 403")
    void create_returnsCustomBadRequestException_whenApartmentNameIsNullOrBlank_status403() {
        // given
        var dto = new ApartmentDto("");

        //when
        Mockito.when(apartmentService.create(dto)).thenThrow(new CustomBadRequestException("Name is required. Name must be at least 6 characters in length."));

        // then
        client.post()
                .uri("/v1/apartments")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0]").isEqualTo("Name is required. Name must be at least 6 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("create returns CustomBadRequestException when apartment name length is less than 6 with status 403")
    void create_returnsCustomBadRequestException_whenApartmentNameLengthLessThan6_status403() {
        // given
        var dto = new ApartmentDto("Apart");

        //when
        Mockito.when(apartmentService.create(dto)).thenThrow(new CustomBadRequestException("Name must be at least 6 characters in length."));

        // then
        client.post()
                .uri("/v1/apartments")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0]").isEqualTo("Name must be at least 6 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("create returns Exception with status 500")
    void create_returnsException_status500() {
        // given
        String name = "Luxury Apartments";
        var dto = new ApartmentDto(name);

        //when
        Mockito.when(apartmentService.create(dto)).thenThrow(new RuntimeException("Something happened!"));

        //then
        client.post()
                .uri("/v1/apartments")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.toString())
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Something happened!")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns a Mono of updated Apartment when name does not exist")
    void update_returnsUpdatedApartment_status200() {
        // given
        String name = "Luxury Apartments";
        var dto = new ApartmentDto(name);

        String id = "1";
        String name2 = "Luxury Apartments B";
        LocalDateTime now = LocalDateTime.now();
        var apartment = new Apartment("1", name2, now, now);

        //when
        Mockito.when(apartmentService.update(id, dto)).thenReturn(Mono.just(apartment));

        // then
        client
                .put()
                .uri("/v1/apartments/{id}",id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Apartment updated successfully.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.id").isEqualTo("1")
                .jsonPath("$.data.name").isEqualTo(name2)
                .jsonPath("$.data.modified").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns CustomBadRequestException when apartment name is null or blank with status 403")
    void update_returnsCustomBadRequestException_whenApartmentNameIsNullOrBlank_status403() {
        // given
        String id = "1";
        var dto = new ApartmentDto(null);

        //when
        Mockito.when(apartmentService.update(id, dto)).thenThrow(new CustomBadRequestException("Name is required."));

        // then
        client
                .put()
                .uri("/v1/apartments/{id}",id)
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0]").isEqualTo("Name is required.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns CustomBadRequestException when apartment name length is less than 6 with status 403")
    void update_returnsCustomBadRequestException_whenApartmentNameLengthLessThan6_status403() {
        // given
        var dto = new ApartmentDto("Apart");

        //when
        Mockito.when(apartmentService.create(dto)).thenThrow(new CustomBadRequestException("Name must be at least 6 characters in length."));

        // then
        client.post()
                .uri("/v1/apartments")
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isNotEmpty()
                .jsonPath("$.data.[0]").isEqualTo("Name must be at least 6 characters in length.")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns CustomAlreadyExistsException with status 400")
    void update_returnsCustomAlreadyExistsException_status400() {
        // given
        String id = "1";
        String name = "Luxury Apartments";
        var dto = new ApartmentDto(name);

        //when
        Mockito.when(apartmentService.update(id, dto)).thenThrow(new CustomAlreadyExistsException("Apartment %s already exists!".formatted(name)));

        //then
        client.put()
                .uri("/v1/apartments/%s".formatted(id))
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.BAD_REQUEST_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Bad request.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Apartment %s already exists!".formatted(name))
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("update returns Exception with status 500")
    void update_returnsException_status500() {
        // given
        String id = "1";
        String name = "Luxury Apartments";
        var dto = new ApartmentDto(name);

        //when
        Mockito.when(apartmentService.update(id, dto)).thenThrow(new RuntimeException("Something happened!"));

        //then
        client.put()
                .uri("/v1/apartments/%s".formatted(id))
                .accept(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), ApartmentDto.class)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.toString())
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Something happened!")
                .consumeWith(System.out::println);

    }

    @Test
    @DisplayName("Find returns a Flux of Apartments")
    void find_returnsFluxOfApartments_status200() {
        // given
        String page = "1";
        String id = "1";
        String name = "Luxury Apartments";
        LocalDateTime now = LocalDateTime.now();
        var apartment = new Apartment("1",name, now, null);
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        OrderType order = OrderType.ASC;

        // when
        Mockito.when(apartmentService.findPaginated(
                Optional.of(id),
                Optional.of(name),
                finalPage - 1,
                finalPageSize,
                order)).thenReturn(Flux.just(apartment));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/apartments")
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
                .jsonPath("$.message").isEqualTo("Apartments found successfully.")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data.[0].id").isEqualTo("1")
                .jsonPath("$.data.[0].name").isEqualTo(name)
                .jsonPath("$.data.[0].created").isNotEmpty()
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("find returns CustomNotFoundException with status 404")
    void find_returnsCustomNotFoundException_status404() {
        // given
        String id = "1";
        String name = "Luxury Apartments";
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");
        String order = OrderType.ASC.name();

        // when
        Mockito.when(apartmentService.findPaginated(
                Optional.of(id),
                Optional.of(name),
                finalPage - 1,
                finalPageSize,
                OrderType.valueOf(order)))
                .thenReturn(Flux.error(new CustomNotFoundException("Apartments were not found!")));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/apartments")
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
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Apartments were not found!")
                .consumeWith(System.out::println);

    }

    @Test
    @DisplayName("find returns Exception with status 500")
    void find_returnsException_status500() {
        // given
        String id = "1";
        String name = "Luxury Apartments";
        String page = "1";
        String pageSize = "10";
        Integer finalPage = CustomUtils.convertToInteger(page, "Page");
        Integer finalPageSize = CustomUtils.convertToInteger(pageSize, "Page size");

        // when
        OrderType order = OrderType.ASC;
        Mockito.when(apartmentService.findPaginated(
                Optional.of(id),
                Optional.of(name),
                finalPage - 1,
                finalPageSize,
                order))
                .thenReturn(Flux.error(new RuntimeException("Something happened!")));

        //then
        Function<UriBuilder, URI> uriFunc = uriBuilder ->
                uriBuilder
                        .path("/v1/apartments")
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
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Something happened!")
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("DeleteById returns a Mono of boolean when id exists")
    void deleteById_returnsTrue_status200() {
        // given
        String id = "1";

        // when
        Mockito.when(apartmentService.deleteById(id))
                .thenReturn(Mono.just(true));

        // then
        client
                .delete()
                .uri("/v1/apartments/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("Apartment with id %s deleted successfully.".formatted(id))
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("deleteById returns CustomNotFoundException with status 404")
    void deleteById_returnsCustomNotFoundException_status404() {
        // given
        String id = "1";

        // when
        Mockito.when(apartmentService.deleteById(id))
                .thenReturn(Mono.error(new CustomNotFoundException("Apartment with id %s does not exist!".formatted(id))));

        // then
        client
                .delete()
                .uri("/v1/apartments/{id}", id)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.NOT_FOUND_ERROR.toString())
                .jsonPath("$.message").isEqualTo("Not found!")
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Apartment with id %s does not exist!".formatted(id))
                .consumeWith(System.out::println);
    }

    @Test
    @DisplayName("deleteById returns Exception with status 500")
    void deleteById_returnsException_status500() {
        // given
        String id = "1";

        // when
        Mockito.when(apartmentService.deleteById(id))
                .thenReturn(Mono.error(new RuntimeException("Something happened!")));

        // then
        client
                .delete()
                .uri("/v1/apartments/{id}", id)
                .exchange()
                .expectStatus().isEqualTo(500)
                .expectBody()
                .jsonPath("$").isNotEmpty()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Something happened!")
                .jsonPath("$.errorCode").isEqualTo(ErrorCode.INTERNAL_SERVER_ERROR.toString())
                .jsonPath("$.data").isArray()
                .jsonPath("$.data[0]").isEqualTo("Something happened!")
//                .expectBody(String.class).isEqualTo("Something happened!")
                .consumeWith(System.out::println);
    }
}