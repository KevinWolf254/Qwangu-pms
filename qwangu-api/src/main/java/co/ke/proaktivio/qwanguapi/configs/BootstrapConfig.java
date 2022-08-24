package co.ke.proaktivio.qwanguapi.configs;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.models.Role;
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

import java.time.LocalDateTime;

@Log4j2
@Configuration
public class BootstrapConfig {
    Person person = new Person("John", "David", "Doe");
    Mono<User> user = Mono.just(new User(null, person, "johnDoe@email.com", null, null, false,
            false, false, true, LocalDateTime.now(), null));

    Mono<Role> superAdminRole = Mono.just(new Role(null, "SUPER_ADMIN", LocalDateTime.now(), null));

    Flux<Authority> superAdminAuthorities = Flux.just(
            new Authority(null, "APARTMENT", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "UNIT", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "OCCUPATION", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "TENANT", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "NOTICE", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "PAYMENT", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "OCCUPATION_TRANSACTION", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "ADVANCE", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "RECEIVABLE", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "REFUND", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "USER", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "USER_ROLE", true, true, true, true, true,
                    null, LocalDateTime.now(), null),
            new Authority(null, "USER_AUTHORITY", true, true, true, true, true,
                    null, LocalDateTime.now(), null)
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
                    .flatMap(userRepository::save)
                    .doOnSuccess(u -> log.info(" Created {}", u))
                    .subscribe();

//            deleteAll(userRepository, roleRepository, authorityRepository)
//                    .then(superAdminRole)
//                    .flatMap(roleRepository::save)
//                    .doOnSuccess(r -> log.info(" Created {}", r))
//                    .flatMap(role -> superAdminAuthorities
//                            .doOnNext(authority -> authority.setRoleId(role.getId()))
//                            .flatMap(authorityRepository::save)
//                            .doOnNext(a -> log.info(" Created {}", a))
//                            .collectList()
//                            .then(user)
//                            .doOnSuccess(u -> u.setRoleId(role.getId())))
//                    .flatMap(userRepository::save)
//                    .doOnSuccess(a -> log.info(" Created {}", a))
//                    .subscribe();
        };
    }

//    public CommandLineRunner init(UserRepository userRepository, RoleRepository roleRepository,
//                                  AuthorityRepository authorityRepository, PasswordEncoder encoder) {
//        return args -> {
//            userRepository.findAll().collectList()
//                    .filter(List::isEmpty)
//                    .map($ -> {
//                        user.setPassword(encoder.encode("12345678"));
//                        return user;
//                    })
//                    .flatMap(userRepository::save)
//                    .filter(Objects::nonNull)
//                    .doOnSuccess(r -> log.info(" Created: " + r))
//                    .flatMapMany(savedUser -> roleRepository.findAll().collectList()
//                            .filter(List::isEmpty)
//                            .map($ -> adminRole)
//                            .flatMapMany(admin -> roleRepository
//                                    .save(admin)
//                                    .doOnSuccess(r -> System.out.println(" Created: " + r))
//                                    .flatMapMany(savedAdmin -> adminAuthorities
//                                            .map(authority -> {
//                                                authority.setRoleId(savedAdmin.getId());
//                                                return authority;
//                                            })
//                                            .doOnNext(r -> System.out.println(" Created: " + r))
//                                            .flatMap(authorityRepository::save)
//                                            .map($ -> {
//                                                savedUser.setRoleId(savedUser.getId());
//                                                return savedUser;
//                                            })
//                                            .flatMap(userRepository::save)
//                                            .doOnNext(r -> System.out.println(" Updated: " + r))
//                                    )
//                            ))
//                    .subscribe();
//        };
//    }
}
