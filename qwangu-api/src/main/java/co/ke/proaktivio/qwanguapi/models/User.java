package co.ke.proaktivio.qwanguapi.models;

import co.ke.proaktivio.qwanguapi.pojos.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "USER")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class User {
    @Id
    private String id;
    private Person person;
    @Indexed(unique = true)
    private String emailAddress;
    private String roleId;
    private LocalDateTime created;
    private LocalDateTime modified;
}
