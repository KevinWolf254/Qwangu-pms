package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.AuthorityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AuthorityServiceImplTest {

    @Mock
    private AuthorityRepository repository;
    @InjectMocks
    private AuthorityServiceImpl authorityService;

    @Test
    void findPaginated_returnFluxOfApartments_whenSuccessful() {
        // given
        LocalDateTime now = LocalDateTime.now();
        var a1 = new Authority("1", "ADMIN", true, true, true, true, true, now, null);
        // when
        when(repository.findPaginated(Optional.of("1"), Optional.of("ADMIN"),0,10, OrderType.ASC))
                .thenReturn(Flux.just(a1));
        // then
        Flux<Authority> request = authorityService.findPaginated(Optional.of("1"), Optional.of("ADMIN"), 0, 10, OrderType.ASC);
        StepVerifier
                .create(request)
                .expectNextMatches(a -> a.getId().equalsIgnoreCase("1") && a.getName().equalsIgnoreCase("ADMIN"))
                .verifyComplete();
    }

}