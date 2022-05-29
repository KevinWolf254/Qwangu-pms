package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "APARTMENT")
@ToString
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Apartment {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private LocalDateTime created;
    private LocalDateTime modified;
}
