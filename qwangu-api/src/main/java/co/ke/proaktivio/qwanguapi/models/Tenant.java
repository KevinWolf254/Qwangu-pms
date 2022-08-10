package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "TENANT")
public class Tenant {
    @Id
    private String id;
    private String firstName;
    private String middleName;
    private String surname;
    @Indexed(unique = true)
    private String mobileNumber;
    @Indexed(unique = true)
    private String emailAddress;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
}
