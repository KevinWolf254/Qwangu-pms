package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "REFUND")
public class Refund {
    @Id
    private String id;
    private Status status;
    private LocalDateTime refundDate;
    private Unit.Currency currency;
    private BigDecimal rent;
    private BigDecimal security;
    private BigDecimal garbage;
    private Map<String, BigDecimal> others;
    private String invoiceId;
    private String occupationId;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    public BigDecimal getTotal() {
        var total = BigDecimal.ZERO;
        if(getOthers() != null)
            total = getOthers().values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        total = total
                .add(getRent() != null ? getRent() : BigDecimal.ZERO)
                .add(getSecurity() != null ? getSecurity() : BigDecimal.ZERO)
                .add(getGarbage() != null ? getGarbage() : BigDecimal.ZERO);

        return total;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        PENDING_REVISION("PENDING_REVISION"),
        PENDING_FULFILLMENT("PENDING_FULFILLMENT"),
        FULFILLED("FULFILLED");

        private final String state;
    }
}
