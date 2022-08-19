package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "RENT_ADVANCE")
public class RentAdvance {
    @Id
    private String id;
    private Status status;
    private String returnDetails;
    private String occupationId;
    private String paymentId;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
    private LocalDate returnedOn;

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        HOLDING("HOLDING"),
        RELEASED("RELEASED"),
        RENT_PAYMENT("RENT_PAYMENT");

        private final String state;
    }
}
