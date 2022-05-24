package co.ke.proaktivio.qwanguapi.controllers;

import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.services.ApartmentService;
import co.ke.proaktivio.qwanguapi.utils.CustomUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Function;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = ApartmentController.class)
class ApartmentControllerTest {
    @Autowired
    private WebTestClient client;
    @MockBean
    private ApartmentService apartmentService;

    @Test
    @DisplayName("Create returns a Mono of Apartment when name does not exist")
    void create_returnMonoApartment_status201() throws Exception {
        // given
        String name = "Luxury Apartments";
        var dto = new ApartmentDto(name);
        var apartment = new Apartment(name,LocalDateTime.now(), LocalDateTime.now());
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
    @DisplayName("Update returns a Mono of updated Apartment when name does not exist")
    void update_returnsUpdatedApartment_status200() throws Exception {
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
                .consumeWith(System.out::println)
                .jsonPath("$.id").isEqualTo(id)
                .jsonPath("$.name").isEqualTo(name2)
                .jsonPath("$.modified").isNotEmpty();
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

        // when
        OrderType order = OrderType.ASC;
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
                .consumeWith(System.out::println)
                .jsonPath("$").isArray()
                .jsonPath("$.[0].id").isEqualTo(id)
                .jsonPath("$.[0].name").isEqualTo(name)
                .jsonPath("$.[0].created").isNotEmpty();
    }

    @Test
    @DisplayName("DeleteById returns a Mono of boolean when id exists")
    void deleteById_returnsTrue_status200() {
        // given
        String id = "1";

        // when
        Mockito.when(apartmentService.deleteById(id)).thenReturn(Mono.just(true));

        // then
        client
                .delete()
                .uri("/v1/apartments/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody()
                .consumeWith(System.out::println);
    }
}