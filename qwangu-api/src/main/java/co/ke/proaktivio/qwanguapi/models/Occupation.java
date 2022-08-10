package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "OCCUPATION")
public class Occupation {
    @Id
    private String id;
    private Status status;
    private LocalDateTime started;
    private LocalDateTime ended;
    private String tenantId;
    private String unitId;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        BOOKED("BOOKED"),
        CURRENT("CURRENT"),
        PREVIOUS("PREVIOUS");

        private final String state;
    }
}
