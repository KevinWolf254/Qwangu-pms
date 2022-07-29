package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "REFUND")
public class Refund {
    @Id
    private String id;
    private Status status;
    private Type type;
    private PaymentType paymentType;
    private BigDecimal amount;
    private String bookingId;
    private String advanceId;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        PENDING_PAYMENT("PENDING_PAYMENT"),
        PAID("PAID");

        private final String state;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        BOOKING("BOOKING"),
        RENT_ADVANCE("RENT_ADVANCE");

        private final String type;
    }
    @Getter
    @RequiredArgsConstructor
    public enum PaymentType {
        FULL_PAYMENT("FULL_PAYMENT"),
        PARTIAL_PAYMENT("PARTIAL_PAYMENT"),
        NO_PAYMENT("NO_PAYMENT");

        private final String type;
    }
}
