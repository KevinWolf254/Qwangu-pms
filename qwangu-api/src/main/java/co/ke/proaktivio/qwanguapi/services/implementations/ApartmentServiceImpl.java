package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.repositories.ApartmentRepository;
import co.ke.proaktivio.qwanguapi.services.ApartmentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {
    private final ApartmentRepository apartmentRepository;

    @Override
    public Mono<Apartment> create(ApartmentDto dto) {
        LocalDateTime now = LocalDateTime.now();
        return apartmentRepository.save(new Apartment(dto.getName(), now, now));
    }

    @Override
    public Mono<Apartment> update(Apartment apartment) {
        apartment.setModified(LocalDateTime.now());
        return apartmentRepository.save(apartment);
    }

//    @Override
//    public Flux<Apartment> find(Predicate<Apartment> predicate, Page<Apartment> page) {
//        return null;
//    }

//    @Override
//    public Flux<Apartment> find(Optional<String> id, Optional<String> name) {
//        Predicate<Apartment> predicate = null;
//        return apartmentRepository.findAll(predicate, Sort.sort(id));
//    }

//    @Override
//    public Mono<Apartment> find(String name) {
//        return apartmentRepository.findByName(name)
//                .switchIfEmpty(Mono.error(new CustomNotFoundException("Apartment %s was not found!".formatted(name))));
//    }

    @Override
    public Mono<Void> deleteById(String id) {
        return apartmentRepository.deleteById("");
    }
}
