package co.ke.proaktivio.qwanguapi.models;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "ONE_TIME_TOKEN")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class OneTimeToken {
    @Id
    private String id;
    private String token;
    private LocalDateTime created;
    private LocalDateTime expiration;
    private String userId;
}
