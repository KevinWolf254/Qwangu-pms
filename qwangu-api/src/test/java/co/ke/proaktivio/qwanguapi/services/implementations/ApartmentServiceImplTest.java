package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomAlreadyExistsException;
import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.repositories.ApartmentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ApartmentServiceImplTest {

    @Mock
    private ApartmentRepository repository;
    @InjectMocks
    private ApartmentServiceImpl apartmentService;

    @Test
    @DisplayName("Create returns a Mono of Apartment when name does not exist")
    void create_ReturnMonoOfApartment_WhenSuccessful() {

        // given
        String name = "Luxury Apartment";
        ApartmentDto dto = new ApartmentDto(name);

        LocalDateTime now = LocalDateTime.now();
        Apartment apartment = new Apartment();
        apartment.setId("1");
        apartment.setName(name);
        apartment.setCreated(now);
        apartment.setModified(now);

        // when
        when(repository.create(dto)).thenReturn(Mono.just(apartment));

        // then
        StepVerifier.create(apartmentService.create(dto))
                .expectNext(apartment)
                .verifyComplete();
    }

    @Test
    @DisplayName("Create returns a Mono of Apartment when name does not exist")
    void create_ReturnCustomAlreadyExistsException() {

        // given
        String name = "Luxury Apartment";
        ApartmentDto dto = new ApartmentDto(name);

        LocalDateTime now = LocalDateTime.now();
        Apartment apartment = new Apartment();
        apartment.setId("1");
        apartment.setName(name);
        apartment.setCreated(now);
        apartment.setModified(now);

        // when
        when(repository.create(dto)).thenReturn(Mono.error(new CustomAlreadyExistsException("Apartment %s already exists!".formatted(name))));

        // then
        StepVerifier.create(apartmentService.create(dto))
                .expectError(CustomAlreadyExistsException.class)
                .verify();
    }

    @Test
    @DisplayName("Update returns a Mono of Apartment when apartment with id exists")
    void update_ReturnMonoOfApartment_WhenSuccessful() {
        // given
        String name = "Luxury Apartment";
        ApartmentDto dto = new ApartmentDto(name);

        LocalDateTime now = LocalDateTime.now();
        Apartment apartment = new Apartment();
        String id = "1";
        apartment.setId(id);
        apartment.setName(name);
        apartment.setCreated(now);
        apartment.setModified(now);

        // when
        when(repository.update(id, dto)).thenReturn(Mono.just(apartment));

        // then
        StepVerifier.create(apartmentService.update(id, dto))
                .expectNext(apartment)
                .verifyComplete();
    }
//
//    @Test
//    void findPaginated() {
//    }
//
//    @Test
//    void deleteById() {
//    }
}