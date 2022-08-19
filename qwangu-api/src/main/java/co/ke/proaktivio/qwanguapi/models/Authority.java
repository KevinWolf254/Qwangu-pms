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
@Document(value = "AUTHORITY")
public class Authority {
    @Id
    private String id;
    private String name;
    private Boolean create;
    private Boolean read;
    private Boolean write;
    private Boolean delete;
    private Boolean authorize;
    private String roleId;
    private LocalDateTime created;
    private LocalDateTime modified;
}
