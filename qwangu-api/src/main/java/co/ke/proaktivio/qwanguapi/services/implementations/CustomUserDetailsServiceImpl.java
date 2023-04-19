package co.ke.proaktivio.qwanguapi.services.implementations;

import co.ke.proaktivio.qwanguapi.exceptions.CustomBadRequestException;
import co.ke.proaktivio.qwanguapi.exceptions.CustomNotFoundException;
import co.ke.proaktivio.qwanguapi.pojos.CustomUserDetails;
import co.ke.proaktivio.qwanguapi.repositories.UserRepository;
import co.ke.proaktivio.qwanguapi.services.UserAuthorityService;
import co.ke.proaktivio.qwanguapi.services.UserRoleService;
import co.ke.proaktivio.qwanguapi.services.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsServiceImpl implements CustomUserDetailsService {
    private final UserRepository userRepository;
    private final UserAuthorityService userAuthorityService;
    private final UserRoleService userRoleService;
    
    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByEmailAddress(username)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User %s could not be found!".formatted(username))))
                .flatMap(user -> {
                    if (StringUtils.hasText(user.getRoleId()))
                        return userRoleService.findById(user.getRoleId())
                                .switchIfEmpty(Mono.error(new CustomNotFoundException("Role for user %s could not be found!".formatted(username))))
                                .flatMap(role -> userAuthorityService
                                        .findByRoleId(role.getId())
                                        .collectList()
                                        .map(authorities -> (UserDetails) new CustomUserDetails(user, role, authorities)));
                    return Mono.error(new CustomBadRequestException("User %s is not assigned a role!".formatted(username)));
                });
    }
}
