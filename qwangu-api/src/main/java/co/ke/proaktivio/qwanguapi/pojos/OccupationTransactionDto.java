package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class OccupationTransactionDto {
    private BigDecimal totalAmountOwed;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalAmountRemaining;
    private String occupationId;
    private String receivableId;
    private String paymentId;
}
