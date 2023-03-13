package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.repositories.UserRoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.services.UserAuthorityService;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

@ExtendWith(SpringExtension.class)
class CustomUserDetailsServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserRoleRepository roleRepository;
    @Mock
    private UserAuthorityService userAuthorityService;
    @InjectMocks
    private CustomUserDetailsServiceImpl customUserDetailsService;

    @Test
    void findByUsername_returnsMonoOfUserDetails_whenUsernameExists() {
        // given
        String id = "1";
        String username = "person@gmail.com";
        LocalDateTime now = LocalDateTime.now();
        Person person = new Person("John", "Doe", "Doe");
        User user = new User(id, person, username, "1", null, false, false, false, true, now, null, null ,null);

        String roleName = "ADMIN";
		var admin = new UserRole.UserRoleBuilder()
				.name(roleName)
				.build();
		admin.setId(id);
		
        String authorityName = "APARTMENT";
        var authority = new UserAuthority("1", authorityName, true, true, true, true,
                true, "1", LocalDateTime.now(), null, null, null);
        var userRole = new UserRole();
        userRole.setId(user.getRoleId());
        // when
        Mockito.when(userRepository.findOne(Example.of(new User(username)))).thenReturn(Mono.just(user));
        Mockito.when(roleRepository.findOne(Example.of(userRole))).thenReturn(Mono.just(admin));
        Mockito.when(userAuthorityService.findByRoleId("1")).thenReturn(Flux.just(authority));
        // then
        Mono<UserDetails> request = customUserDetailsService.findByUsername(username)
                .doOnSuccess(System.out::println)
                .doOnError(System.out::println);
        StepVerifier
                .create(request)
                .expectNextMatches(cud ->
                        cud.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_" + roleName)) &&
                        cud.getAuthorities().contains(new SimpleGrantedAuthority(authorityName + "_UPDATE")) &&
                        cud.getAuthorities().contains(new SimpleGrantedAuthority(authorityName + "_AUTHORIZE")) &&
                        cud.getAuthorities().contains(new SimpleGrantedAuthority(authorityName + "_CREATE")) &&
                        cud.getAuthorities().contains(new SimpleGrantedAuthority(authorityName + "_READ")) &&
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
        User user = new User(id, person, username, null, null, false, false, false, true, now, null, null ,null);

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
        User user = new User(id, person, username, "1", null, false, false, false, true, now, null, null ,null);
        var userRole = new UserRole();
        userRole.setId(user.getRoleId());
        // when
        Mockito.when(userRepository.findOne(Example.of(new User(username))))
                .thenReturn(Mono.just(user));

        Mockito.when(roleRepository.findOne(Example.of(userRole)))
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