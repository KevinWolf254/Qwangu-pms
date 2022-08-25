package co.ke.proaktivio.qwanguapi.configs;

import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.repositories.AuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Log4j2
@Configuration
public class BootstrapConfig {
    Person person = new Person("John", "David", "Doe");
    Mono<User> user = Mono.just(new User(null, person, "johnDoe@email.com", null, "ABc1234!", false,
            false, false, true, null, null, null, null));

    Mono<UserRole> superAdminRole = Mono.just(new UserRole.RoleBuilder().setName("SUPER_ADMIN").build());

    Flux<UserAuthority> superAdminAuthorities = Flux.just(
            new UserAuthority(null, "APARTMENT", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "UNIT", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "OCCUPATION", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "TENANT", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "NOTICE", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "PAYMENT", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "OCCUPATION_TRANSACTION", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "ADVANCE", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "RECEIVABLE", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "REFUND", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "USER", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "USER_ROLE", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null),
            new UserAuthority(null, "USER_AUTHORITY", true, true, true, true, true,
                    null, LocalDateTime.now(), null, null, null)
    );

    private Mono<Void> deleteAll(UserRepository userRepository, RoleRepository roleRepository,
                         AuthorityRepository authorityRepository) {
        return authorityRepository.deleteAll()
                .doOnSuccess($ -> log.info(" Deleted all authorities"))
                .then(roleRepository.deleteAll())
                .doOnSuccess($ -> log.info(" Deleted all roles"))
                .then(userRepository.deleteAll())
                .doOnSuccess($ -> log.info(" Deleted all users"));
    }

    @Bean
    public CommandLineRunner init(UserRepository userRepository, RoleRepository roleRepository,
                                  AuthorityRepository authorityRepository, PasswordEncoder encoder) {
        return args -> {
            deleteAll(userRepository, roleRepository, authorityRepository)
                    .then(superAdminRole)
                    .flatMap(roleRepository::save)
                    .doOnSuccess(r -> log.info(" Created {}", r))
                    .flatMap(r -> superAdminAuthorities
                                    .doOnNext(a -> a.setRoleId(r.getId()))
                                    .collectList()
                                    .flatMap(a -> authorityRepository.saveAll(a)
                                            .doOnNext(as -> log.info(" Created {}", as))
                                            .collectList())
                                    .flatMap($ -> user
                                            .doOnSuccess(u -> u.setRoleId(r.getId())))
                            )
                    .doOnSuccess(u -> {
                        u.setPassword(encoder.encode(u.getPassword()));
                        log.info(" Encoded password {}", u.getPassword());
                    }).subscribeOn(Schedulers.parallel())
                    .flatMap(userRepository::save)
                    .doOnSuccess(u -> log.info(" Created {}", u))
                    .subscribe();
        };
    }
}
