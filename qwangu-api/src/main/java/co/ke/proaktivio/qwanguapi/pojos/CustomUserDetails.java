package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.UserAuthority;
import co.ke.proaktivio.qwanguapi.models.UserRole;
import co.ke.proaktivio.qwanguapi.models.User;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Data
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private static final long serialVersionUID = -7827040727961360292L;
	private final User user;
    private final UserRole role;
    private final List<UserAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return generateSimpleGrantedAuthorities(authorities);
    }

    private Set<SimpleGrantedAuthority> generateSimpleGrantedAuthorities(List<UserAuthority> authorities) {
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
        return !user.getIsAccountExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !user.getIsAccountLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !user.getIsCredentialsExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.getIsEnabled();
    }
}
