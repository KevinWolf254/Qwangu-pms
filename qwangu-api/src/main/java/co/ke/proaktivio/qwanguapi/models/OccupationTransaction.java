package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "OCCUPATION_TRANSACTION")
public class OccupationTransaction {
    @Id
    private String id;
    private Type type;
    private BigDecimal totalAmountOwed;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalAmountCarriedForward;
    private String occupationId;
    private String receivableId;
    private String paymentId;
    @CreatedDate
    private LocalDateTime createdOn;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        CREDIT("CREDIT"),
        DEBIT("DEBIT");

        private final String type;
    }
}
