package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Receivable;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ReceivableDto {
    private Receivable.Type type;
    private LocalDate period;
    private BigDecimal rentAmount;
    private BigDecimal securityAmount;
    private BigDecimal garbageAmount;
    private Map<String, BigDecimal> otherAmounts;
}
