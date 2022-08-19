package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.domain.Example;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
class CustomUserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @Test
    void findByUsername_returnsMonoOfUserDetails_whenUsernameExists() {
        // given
        String id = "1";
        String username = "person@gmail.com";
        LocalDateTime now = LocalDateTime.now();
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, username, "1", null, false, false, false, true, now, null);

        String name = "ADMIN";
        var role = new Role("1", name, now, null);

        // when
        Mockito.when(userRepository.findOne(Example.of(new User(username))))
                .thenReturn(Mono.just(user));

        Mockito.when(roleRepository.findOne(Example.of(new Role(user.getRoleId()))))
                .thenReturn(Mono.just(role));

        // then
        Mono<UserDetails> request = customUserDetailsService.findByUsername(username)
                .doOnSuccess(System.out::println)
                .doOnError(System.out::println);
        StepVerifier
                .create(request)
                .expectNextMatches(cud ->
                        cud.getAuthorities().equals(Arrays.asList(new SimpleGrantedAuthority(name))) &&
                        cud.getPassword() == null && cud.isAccountNonExpired() && cud.isCredentialsNonExpired() &&
                        cud.isAccountNonLocked() && cud.isEnabled() && cud.getUsername().equalsIgnoreCase(username))
                .verifyComplete();
    }


    @Test
    void findByUsername_returnsCustomNotFoundException_whenUsernameDoesNotExists() {
        // given
        String username = "person@gmail.com";

        // when
        Mockito.when(userRepository.findOne(Example.of(new User(username))))
                .thenReturn(Mono.empty());

        // then
        Mono<UserDetails> request = customUserDetailsService.findByUsername(username)
                .doOnSuccess(System.out::println)
                .doOnError(System.out::println);
        StepVerifier
                .create(request)
                .expectErrorMatches(e -> e instanceof UsernameNotFoundException &&
                        e.getMessage().equalsIgnoreCase("User %s could not be found!".formatted(username)))
                .verify();
    }

    @Test
    void findByUsername_returnsCustomNotFoundException_whenRoleIdIsNull() {
        // given
        String id = "1";
        String username = "person@gmail.com";
        LocalDateTime now = LocalDateTime.now();
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, username, null, null, false, false, false, true, now, null);

        // when
        Mockito.when(userRepository.findOne(Example.of(new User(username))))
                .thenReturn(Mono.just(user));

//        Mockito.when(roleRepository.findOne(Example.of(new Role(user.getRoleId()))))
//                .thenReturn(Mono.error(new CustomNotFoundException("Role for user %s could not be found!".formatted(username))));

//        Mockito.when(roleRepository.findOne(Example.of(new Role(user.getRoleId()))))
//                .thenReturn(Mono.error(new CustomNotFoundException("Role for user %s could not be found!".formatted(username))));

        // then
        Mono<UserDetails> request = customUserDetailsService.findByUsername(username)
                .doOnSuccess(System.out::println)
                .doOnError(System.out::println);
        StepVerifier
                .create(request)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Role for user %s could not be found!".formatted(username)))
                .verify();
    }


    @Test
    void findByUsername_returnsCustomNotFoundException_whenRoleIdDoesNotExist() {
        // given
        String id = "1";
        String username = "person@gmail.com";
        LocalDateTime now = LocalDateTime.now();
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, username, "1", null, false, false, false, true, now, null);

        // when
        Mockito.when(userRepository.findOne(Example.of(new User(username))))
                .thenReturn(Mono.just(user));

        Mockito.when(roleRepository.findOne(Example.of(new Role(user.getRoleId()))))
                .thenReturn(Mono.empty());

        // then
        Mono<UserDetails> request = customUserDetailsService.findByUsername(username)
                .doOnSuccess(System.out::println)
                .doOnError(System.out::println);
        StepVerifier
                .create(request)
                .expectErrorMatches(e -> e instanceof CustomNotFoundException &&
                        e.getMessage().equalsIgnoreCase("Role for user %s could not be found!".formatted(username)))
                .verify();
    }
}