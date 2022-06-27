package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.models.OneTimeToken;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.repositories.OneTimeTokenRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class OneTimeTokenServiceImplTest {
    @Mock
    OneTimeTokenRepository oneTimeTokenRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    OneTimeTokenServiceImpl oneTimeTokenService;

    @Test
    @DisplayName("create returns a Mono of OneTimeToken when user id exists")
    void create_returnOneTimeToken_whenUserIdExists() {
        // given
        String userId = "1";
        Person person = new Person("John", "Doe", "Doe");
        String emailAddress = "person@gmail.com";
        String roleId = "1";
        User user = new User(userId, person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), null);
        LocalDateTime now = LocalDateTime.now();
        OneTimeToken token = new OneTimeToken("1", UUID.randomUUID().toString(), now, now.plusHours(12), userId);
        // when
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        Mockito.when(oneTimeTokenRepository.save(token)).thenReturn(Mono.just(token));
        // then
        StepVerifier
                .create(oneTimeTokenService.create(userId))
                .expectNext(token)
                .verifyComplete();
    }

    @Test
    void find() {
    }

    @Test
    void deleteById() {
    }
}