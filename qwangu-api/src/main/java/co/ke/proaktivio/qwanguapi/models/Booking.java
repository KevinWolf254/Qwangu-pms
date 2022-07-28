package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Document(value = "BOOKING")
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    private String id;
    private Status status;
    private LocalDate occupation;
    private LocalDateTime created;
    private LocalDateTime modified;
    private String paymentId;
    private String unitId;

    @Getter
    public enum Status {
        BOOKED("BOOKED"),
        PENDING_OCCUPATION("PENDING_OCCUPATION"),
        OCCUPIED("OCCUPIED"),
        CANCELLED("CANCELLED");

        private String state;

        Status(String state) {
            this.state = state;
        }
    }
}
