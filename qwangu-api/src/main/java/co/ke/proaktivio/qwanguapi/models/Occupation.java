package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
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
    private Boolean active;
    private LocalDateTime started;
    private LocalDateTime ended;
    private String tenantId;
    private String unitId;
    private LocalDateTime created;
    private LocalDateTime modified;
}
