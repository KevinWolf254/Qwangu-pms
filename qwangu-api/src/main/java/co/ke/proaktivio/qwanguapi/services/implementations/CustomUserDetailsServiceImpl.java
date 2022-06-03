package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.CustomUserDetails;
import co.ke.proaktivio.qwanguapi.repositories.AuthorityRepository;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import co.ke.proaktivio.qwanguapi.services.CustomUserDetailsService;
import co.ke.proaktivio.qwanguapi.services.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findOne(Example.of(new User(username)))
                .switchIfEmpty(Mono.error(new CustomNotFoundException("User %s could not be found!".formatted(username))))
                .flatMap(user -> roleRepository
                        .findOne(Example.of(new Role(user.getRoleId())))
                        .switchIfEmpty(Mono.error(new CustomNotFoundException("Role for user %s could not be found!".formatted(username))))
                        .map(role -> (UserDetails) new CustomUserDetails(user, role)));
    }
}
