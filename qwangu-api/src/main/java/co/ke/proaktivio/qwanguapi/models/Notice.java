package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "NOTICE")
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Notice {
    @Id
    private String id;
    private Status status;
    private LocalDateTime notificationDate;
    private LocalDateTime vacatingDate;
    private LocalDateTime created;
    private LocalDateTime modified;
    private String occupationId;

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        AWAITING_EXIT("AWAITING_EXIT"),
        EXITED("EXITED");

        private final String state;
    }
}
