package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Document(value = "ROLE")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Role {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private Set<String> authorityIds;
    private LocalDateTime created;
    private LocalDateTime modified;
}
