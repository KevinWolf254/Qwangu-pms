package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.pojos.OrderType;
import co.ke.proaktivio.qwanguapi.repositories.AuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository repository;
    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void findPaginated_returnFluxOfApartments_whenSuccessful() {
        // given
        String name = "ADMIN";
        LocalDateTime now = LocalDateTime.now();
        var a1 = new Role("1", name, now, null);
        // when
        when(repository.findPaginated(Optional.of("1"), Optional.of(name),1,10, OrderType.ASC))
                .thenReturn(Flux.just(a1));
        // then
        Flux<Role> request = roleService.findPaginated(Optional.of("1"), Optional.of(name), 1, 10, OrderType.ASC);
        StepVerifier
                .create(request)
                .expectNextMatches(a -> a.getId().equalsIgnoreCase("1") && a.getName().equalsIgnoreCase(name))
                .verifyComplete();
    }
}