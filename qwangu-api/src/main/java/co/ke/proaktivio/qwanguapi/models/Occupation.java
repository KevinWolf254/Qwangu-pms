package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "OCCUPATION")
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Occupation {
    @Id
    private String id;
    private Status status;
    private LocalDateTime started;
    private LocalDateTime ended;
    private String tenantId;
    private String unitId;
    private LocalDateTime created;
    private LocalDateTime modified;

    @Getter
    public enum Status {
        CURRENT("CURRENT"),
        MOVED("MOVED");

        private String state;

        Status(String state) {
            this.state = state;
        }
    }
}
