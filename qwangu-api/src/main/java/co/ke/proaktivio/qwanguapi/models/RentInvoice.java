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
@Document(value = "RENT_INVOICE_TRANSACTION")
public class RentInvoice {
    @Id
    private String id;
    private Type type;
    private BigDecimal rentAmount;
    private BigDecimal securityAmount;
    private BigDecimal garbageAmount;
    private BigDecimal rentAmountCarriedForward;
    private BigDecimal securityAmountCarriedForward;
    private BigDecimal garbageAmountCarriedForward;
    private BigDecimal penaltyAmount;
    private BigDecimal penaltyAmountCarriedForward;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
    private String occupationId;
    private String paymentId;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        DEBIT("DEBIT"),
        CREDIT("CREDIT");

        private final String type;
    }
}
