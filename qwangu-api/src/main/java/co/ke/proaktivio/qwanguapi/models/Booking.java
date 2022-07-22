package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "BOOKING")
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    private String id;
    private Boolean active;
    private LocalDateTime occupation;
    private LocalDateTime created;
    private LocalDateTime modified;
    private String paymentId;
    private String unitId;
}
