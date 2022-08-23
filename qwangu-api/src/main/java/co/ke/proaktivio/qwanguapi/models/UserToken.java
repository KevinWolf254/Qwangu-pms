package co.ke.proaktivio.qwanguapi.models;

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
@Document(value = "USER_TOKEN")
public class UserToken {
    @Id
    private String id;
    @Indexed(unique = true)
    private String emailAddress;
    private String token;
    private LocalDateTime firstSignIn;
    private LocalDateTime lastSignIn;
}
