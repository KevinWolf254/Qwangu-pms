package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Apartment;
import co.ke.proaktivio.qwanguapi.pojos.ApartmentDto;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.ApartmentRepository;
import co.ke.proaktivio.qwanguapi.services.ApartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApartmentServiceImpl implements ApartmentService {
    private final ApartmentRepository repository;

    @Override
    public Mono<Apartment> create(ApartmentDto dto) {
        return repository.create(dto);
    }

    @Override
    public Mono<Apartment> update(String id, ApartmentDto dto) {
        return repository.update(id, dto);
    }

    @Override
    public Flux<Apartment> findPaginated(Optional<String> id, Optional<String> name, int page, int pageSize, OrderType order) {
        return repository.findPaginated(id, name, page, pageSize, order);
    }

    @Override
    public Mono<Boolean> deleteById(String id) {
        return repository.delete(id);
    }
}
