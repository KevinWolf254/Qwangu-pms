package co.ke.proaktivio.qwanguapi.configs.routers;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.handlers.ApartmentHandler;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
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
        var apartment = new Apartment(name, LocalDateTime.now(), LocalDateTime.now());
        apartment.setId("1");

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
                .expectBody(Apartment.class).isEqualTo(apartment)
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
                .expectBody(String.class).isEqualTo("Apartment %s already exists!".formatted(name))
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
                .expectBody(String.class).isEqualTo("Something happened!")
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
        var apartment = new Apartment(name2,LocalDateTime.now(), LocalDateTime.now());
        apartment.setId(id);

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
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo(name2)
                .jsonPath("$.modified").isNotEmpty()
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
                .expectBody(String.class).isEqualTo("Apartment %s already exists!".formatted(name))
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
                .expectBody(String.class).isEqualTo("Something happened!")
                .consumeWith(System.out::println);

    }

    @Test
    @DisplayName("Find returns a Flux of Apartments")
    void find_returnsFluxOfApartments_status200() {
        // given
        String page = "1";
        String id = "1";
        String name = "Luxury Apartments";
        var apartment = new Apartment(name,LocalDateTime.now(), LocalDateTime.now());
        apartment.setId(id);
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
                .jsonPath("$").isArray()
                .jsonPath("$.[0].id").isEqualTo(id)
                .jsonPath("$.[0].name").isEqualTo(name)
                .jsonPath("$.[0].created").isNotEmpty()
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
                .thenReturn(Flux.error(new CustomNotFoundException("Apartments do not exist!")));

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
                .expectBody(String.class).isEqualTo("Apartments do not exist!")
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
                .expectBody(String.class).isEqualTo("Something happened!")
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
                .expectBody(Boolean.class).isEqualTo(true)
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
                .expectBody(String.class).isEqualTo("Apartment with id %s does not exist!".formatted(id))
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
                .expectBody(String.class).isEqualTo("Something happened!")
                .consumeWith(System.out::println);
    }
}