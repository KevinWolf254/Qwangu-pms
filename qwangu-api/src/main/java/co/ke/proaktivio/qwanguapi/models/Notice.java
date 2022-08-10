package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "NOTICE")
public class Notice {
    @Id
    private String id;
    private Boolean isActive;
    private LocalDateTime notifiedOn;
    private LocalDate vacatingOn;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
    private String occupationId;
}
