package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Invoice;
import co.ke.proaktivio.qwanguapi.models.Unit.Currency;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceDto {
    // TODO VALIDATE AMOUNTS ACCORDING TO INVOICE TYPE
    private Invoice.Type type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Currency currency;
    private BigDecimal rentAmount;
    private BigDecimal securityAmount;
    private BigDecimal garbageAmount;
    private Map<String, BigDecimal> otherAmounts;
    private String occupationId;
}
