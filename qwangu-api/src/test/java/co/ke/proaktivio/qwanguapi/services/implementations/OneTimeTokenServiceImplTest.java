package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
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
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

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
        User user = new User(userId, person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), null, null ,null);
        LocalDateTime now = LocalDateTime.now();
        String uuid = UUID.randomUUID().toString();
        OneTimeToken token = new OneTimeToken("1", uuid, now, now.plusHours(12), userId);
        // when
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.just(user));
        Mockito.when(oneTimeTokenRepository.save(any())).thenReturn(Mono.just(token));
        // then
        StepVerifier
                .create(oneTimeTokenService.create(userId, uuid))
                .expectNext(token)
                .verifyComplete();
    }

    @Test
    @DisplayName("create returns a CustomNotFoundException when user id does not exist")
    void create_returnCustomNotFoundException_whenUserIdExists() {
        // given
        String userId = "1";
        // when
        Mockito.when(userRepository.findById(userId)).thenReturn(Mono.empty());
        // then
        StepVerifier
                .create(oneTimeTokenService.create(userId, any(String.class)))
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("User with id %s could not be found!".formatted(userId)))
                .verify();
    }

    @Test
    @DisplayName("find returns a Mono of OneTimeToken when user id exists")
    void find_returnOneTimeToken_whenTokenExists() {
        // given
        String uuid = UUID.randomUUID().toString();
        String userId = "1";
        LocalDateTime now = LocalDateTime.now();
        OneTimeToken token = new OneTimeToken("1", uuid, now, now.plusHours(12), userId);
        // when
        Mockito.when(oneTimeTokenRepository.findOne(any())).thenReturn(Mono.just(token));
        // then
        StepVerifier
                .create(oneTimeTokenService.find(uuid, userId))
                .expectNext(token)
                .verifyComplete();
    }
    @Test
    @DisplayName("find returns a CustomNotFoundException when token does not exist")
    void create_returnCustomNotFoundException_whenTokenDoesNotExist() {
        // given
        String uuid = UUID.randomUUID().toString();
        String userId = "1";
        // when
        Mockito.when(oneTimeTokenRepository.findOne(any())).thenReturn(Mono.empty());
        // then
        StepVerifier
                .create(oneTimeTokenService.find(uuid, userId))
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Token could not be found!"))
                .verify();
    }

    @Test
    @DisplayName("deleteById returns true when token is deleted successfully")
    void deleteById_returnVoid() {
        // given
        String tokenId = "1";
        String uuid = UUID.randomUUID().toString();
        var now = LocalDateTime.now();
        OneTimeToken token = new OneTimeToken("1", uuid, now, now.plusHours(12), "1");
        //when
        Mockito.when(oneTimeTokenRepository.findById(any(String.class))).thenReturn(Mono.just(token));
        // then
        var mockVoid = Mono.empty();
        StepVerifier
                .create(oneTimeTokenService.deleteById(tokenId))
                .expectComplete();
    }
    @Test
    @DisplayName("deleteById returns CustomNotFoundException when token id does not exist")
    void deleteById_returnsCustomNotFoundException() {
        // given
        String tokenId = "1";
        String uuid = UUID.randomUUID().toString();
        var now = LocalDateTime.now();
        OneTimeToken token = new OneTimeToken("1", uuid, now, now.plusHours(12), "1");
        //when
        Mockito.when(oneTimeTokenRepository.findById(tokenId)).thenReturn(Mono.empty());
        // then
        StepVerifier
                .create(oneTimeTokenService.deleteById(tokenId))
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equals("Token could not be found!"))
                .verify();
    }
}