package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "APARTMENT")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Apartment extends BasicEntity {
    @Indexed(unique = true)
    private String name;
    private LocalDateTime created;
    private LocalDateTime modified;
}
