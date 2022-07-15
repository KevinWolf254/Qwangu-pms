package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "TENANT")
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @Id
    private String id;
    private String firstName;
    private String middleName;
    private String surname;
    private String mobileNumber;
    private String emailAddress;
    private LocalDateTime created;
    private LocalDateTime modified;
}
