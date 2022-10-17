package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Invoice;
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
    private Invoice.Type type;
    private LocalDate fromDate;
    private BigDecimal rentAmount;
    private BigDecimal securityAmount;
    private BigDecimal garbageAmount;
    private Map<String, BigDecimal> otherAmounts;
    private String occupationId;
}

//class RentAdvanceInvoiceDto {
//    private String occupationId;
//    private BigDecimal rentAmount;
//    private BigDecimal securityAmount;
//    private BigDecimal garbageAmount;
//    private Map<String, BigDecimal> otherAmounts;
//}
//
//class RentInvoiceDto {
//    private String occupationId;
//    private LocalDate fromDate;
//    private BigDecimal rentAmount;
//    private BigDecimal securityAmount;
//    private BigDecimal garbageAmount;
//    private Map<String, BigDecimal> otherAmounts;
//}
//
//class PenaltyInvoiceDto {
//    private String occupationId;
//    private Map<String, BigDecimal> otherAmounts;
//}
