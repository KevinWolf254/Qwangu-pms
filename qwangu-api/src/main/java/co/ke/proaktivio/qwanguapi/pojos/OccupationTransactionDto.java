package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.OccupationTransaction;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OccupationTransactionDto {
    private OccupationTransaction.Type type;
    private BigDecimal totalAmountOwed;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalAmountCarriedForward;
    private String occupationId;
    private String invoiceId;
    private String receiptId;
}
