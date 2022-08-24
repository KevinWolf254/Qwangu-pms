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
import org.springframework.data.mongodb.core.ReactiveMongoOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Log4j2
@Configuration
public class BootstrapConfig {
    Role adminRole = new Role(null, "ADMIN", LocalDateTime.now(), null);
    Flux<Authority> adminAuthorities = Flux.just(
            new Authority(null, "APARTMENT", true, true, true, true, true,
                    null, LocalDateTime.now(), null)
    );
    Person person = new Person("John", "David", "Doe");
    User user = new User(null, person, "johnDoe@email.com", null, null, false,
            false, false, true, LocalDateTime.now(), null);

    @Bean
    public CommandLineRunner init(UserRepository userRepository, RoleRepository roleRepository,
                                  AuthorityRepository authorityRepository, PasswordEncoder encoder) {
        return args -> {
            userRepository.findAll().collectList()
                    .filter(List::isEmpty)
                    .map($ -> {
                        user.setPassword(encoder.encode("12345678"));
                        return user;
                    })
                    .flatMap(userRepository::save)
                    .filter(Objects::nonNull)
                    .doOnSuccess(r -> log.info(" Created: " + r))
                    .flatMapMany(savedUser -> roleRepository.findAll().collectList()
                            .filter(List::isEmpty)
                            .map($ -> adminRole)
                            .flatMapMany(admin -> roleRepository
                                    .save(admin)
                                    .doOnSuccess(r -> System.out.println(" Created: " + r))
                                    .flatMapMany(savedAdmin -> adminAuthorities
                                            .map(authority -> {
                                                authority.setRoleId(savedAdmin.getId());
                                                return authority;
                                            })
                                            .doOnNext(r -> System.out.println(" Created: " + r))
                                            .flatMap(authorityRepository::save)
                                            .map($ -> {
                                                savedUser.setRoleId(savedUser.getId());
                                                return savedUser;
                                            })
                                            .flatMap(userRepository::save)
                                            .doOnNext(r -> System.out.println(" Updated: " + r))
                                    )
                            ))
                    .subscribe();
        };
    }

}
