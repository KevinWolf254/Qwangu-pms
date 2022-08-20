package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Authority;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;
import java.util.stream.Collectors;

@Data
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final User user;
    private final Role role;
    private final List<Authority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return generateSimpleGrantedAuthorities(authorities);
    }

    private Set<SimpleGrantedAuthority> generateSimpleGrantedAuthorities(List<Authority> authorities) {
        Set<SimpleGrantedAuthority> userAuthorities = new HashSet<>();
        authorities.forEach(authority -> {
            String name = authority.getName();
            userAuthorities.add(new SimpleGrantedAuthority("ROLE_".concat(role.getName().toUpperCase())));
            if(authority.getCreate()) {
                userAuthorities.add(new SimpleGrantedAuthority(name.toUpperCase().concat("_CREATE")));
            }
            if(authority.getRead()) {
                userAuthorities.add(new SimpleGrantedAuthority(name.toUpperCase().concat("_READ")));
            }
            if(authority.getUpdate()) {
                userAuthorities.add(new SimpleGrantedAuthority(name.toUpperCase().concat("_UPDATE")));
            }
            if(authority.getAuthorize()) {
                userAuthorities.add(new SimpleGrantedAuthority(name.toUpperCase().concat("_AUTHORIZE")));
            }
        });
        return userAuthorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getEmailAddress();
    }

    @Override
    public boolean isAccountNonExpired() {
        return !user.getAccountExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.getAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !user.getCredentialsExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.getEnabled();
    }
}
