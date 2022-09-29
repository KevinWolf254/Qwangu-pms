package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
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
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    public OccupationTransaction(String id, Type type, BigDecimal totalAmountOwed, BigDecimal totalAmountPaid,
                                 BigDecimal totalAmountCarriedForward, String occupationId, String receivableId, String paymentId) {
        this.id = id;
        this.type = type;
        this.totalAmountOwed = totalAmountOwed;
        this.totalAmountPaid = totalAmountPaid;
        this.totalAmountCarriedForward = totalAmountCarriedForward;
        this.occupationId = occupationId;
        this.receivableId = receivableId;
        this.paymentId = paymentId;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        CREDIT("CREDIT"),
        DEBIT("DEBIT");

        private final String type;
    }
}
