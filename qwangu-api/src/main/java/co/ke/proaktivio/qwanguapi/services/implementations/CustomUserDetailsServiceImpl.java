package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.CustomUserDetails;
import co.ke.proaktivio.qwanguapi.repositories.RoleRepository;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.services.AuthorityService;
import co.ke.proaktivio.qwanguapi.services.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Example;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuthorityService authorityService;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findOne(Example.of(new User(username)))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User %s could not be found!".formatted(username))))
                .flatMap(user -> {
                    if (StringUtils.hasText(user.getRoleId()))
                        return roleRepository
                                .findOne(Example.of(new UserRole(user.getRoleId())))
                                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role for user %s could not be found!".formatted(username))))
                                .flatMap(role -> authorityService
                                        .findByRoleId(role.getId())
                                        .collectList()
                                        .map(authorities -> (UserDetails) new CustomUserDetails(user, role, authorities)));
                    return Mono.error(new CustomNotFoundException("Role for user %s could not be found!".formatted(username)));
                });
    }
}
