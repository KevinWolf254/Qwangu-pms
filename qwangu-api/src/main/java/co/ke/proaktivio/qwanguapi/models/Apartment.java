package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "APARTMENT")
@EqualsAndHashCode
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Apartment extends BasicEntity {
    private String name;
    private LocalDateTime created;
    private LocalDateTime modified;
}
