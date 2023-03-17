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
@Document(value = "ONE_TIME_TOKEN")
public class OneTimeToken {
    @Id
    private String id;
    @Indexed(unique = true)
    private String token;
    private LocalDateTime created;
    private LocalDateTime expiration;
    private String userId;

    public OneTimeToken(String token) {
        this.token = token;
    }
}
