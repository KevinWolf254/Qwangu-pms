package co.ke.proaktivio.qwanguapi.models;

import co.ke.proaktivio.qwanguapi.pojos.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "USER")
public class User {
    @Id
    private String id;
    private Person person;
    @Indexed(unique = true)
    private String emailAddress;
    private String roleId;
    private String password;
    private Boolean isAccountExpired;
    private Boolean isCredentialsExpired;
    private Boolean isAccountLocked;
    private Boolean isEnabled;
    private LocalDateTime created;
    private LocalDateTime modified;

    public User(String emailAddress) {
        this.emailAddress = emailAddress;
    }
}
