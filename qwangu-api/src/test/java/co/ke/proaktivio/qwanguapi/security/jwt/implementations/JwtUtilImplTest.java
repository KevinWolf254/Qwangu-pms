package co.ke.proaktivio.qwanguapi.security.jwt.implementations;

import co.ke.proaktivio.qwanguapi.configs.JwtPropertiesConfig;
import co.ke.proaktivio.qwanguapi.models.Role;
import co.ke.proaktivio.qwanguapi.models.User;
import co.ke.proaktivio.qwanguapi.pojos.Person;
import co.ke.proaktivio.qwanguapi.security.jwt.JwtUtil;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = JwtPropertiesConfig.class)
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
class JwtUtilImplTest {
    @Autowired
    private JwtPropertiesConfig configs;

    private JwtUtil util;
    private String token;

    @BeforeEach
    public void setUp() {
        util = new JwtUtilImpl(configs);
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User("1", person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), null);
        Role role = new Role(roleId, "ADMIN", Set.of(), LocalDateTime.now(), null);
        token = util.generateToken(user, role);
        System.out.println(token);
    }

    @Test
    void init() {
        assertThat(this.configs.getSecret()).isEqualTo("alksossowmdkdofnonoffkbffjwpnwjluopkhgyipkjhtrgfhipokhgrr");
        assertThat(this.configs.getExpiration()).isEqualTo(3600);
    }

    @Test
    void getClaims_returnsClaims() {
        // when
        Claims claims = util.getClaims(token);
        assertThat(claims).isNotEmpty();
    }

    @Test
    void getUsername() {
        // given
        String emailAddress = "person@gmail.com";

        // when
        String username = util.getUsername(token);

        // then
        assertThat(username).isNotEmpty();
        assertThat(username).isEqualTo(emailAddress);
    }

    @Test
    void getExpirationDate_returnsALocalDateTime() {
        // when
        LocalDateTime expirationDate = util.getExpirationDate(token);

        // then
        assertThat(expirationDate).isExactlyInstanceOf(LocalDateTime.class);
        assertThat(expirationDate.isAfter(LocalDateTime.now())).isTrue();
        assertThat(expirationDate.isBefore(LocalDateTime.now().plusSeconds(3600))).isTrue();
    }

    @Test
    void isTokenExpired_returnsFalseWhenTokenHasNoExpired() {
        // when
        Boolean hasExpired = util.isTokenExpired(token);

        // then
        assertThat(hasExpired).isFalse();
    }

    @Test
    void validate_returnsTrueWhenTokenHasNotExpired() {
        // when
        Boolean isValid = util.validate(token);

        // then
        assertThat(isValid).isTrue();
    }

    @Test
    void generateToken_returnsAStringAsAToken() {
        // given
        String roleId = "1";
        String emailAddress = "person@gmail.com";
        Person person = new Person("John", "Doe", "Doe");
        User user = new User("1", person, emailAddress, roleId, null, false, false, false, true, LocalDateTime.now(), null);
        Role role = new Role(roleId, "ADMIN", Set.of(), LocalDateTime.now(), null);

        // when
        String token = util.generateToken(user, role);

        // then
        assertThat(token).isNotEmpty();
    }
}