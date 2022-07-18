package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
    private Boolean active;
    private LocalDateTime notificationDate;
    private LocalDateTime vacatingDate;
    private LocalDateTime created;
    private LocalDateTime modified;
    private String occupationId;
}
