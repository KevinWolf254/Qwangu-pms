package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "INVOICE")
public class Invoice {
    @Id
    private String id;
    private String occupationId;
    @Indexed(unique = true)
    private String invoiceNo;
    private Type type;
    // TODO - ENSURE PERIOD IS MONTH AND YEAR
    private LocalDate period;
    private BigDecimal rentAmount;
    private BigDecimal securityAmount;
    private BigDecimal garbageAmount;
    private Map<String, BigDecimal> otherAmounts;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        RENT_ADVANCE("RENT_ADVANCE"),
        RENT("RENT"),
        PENALTY("PENALTY");
//        BOOKING("BOOKING");

        private final String name;
    }


    public static class InvoiceBuilder {
        private Type type;
        private LocalDate period;
        private BigDecimal rentAmount;
        private BigDecimal securityAmount;
        private BigDecimal garbageAmount;
        private Map<String, BigDecimal> otherAmounts;
        private String invoiceNo;
        private String occupationId;

        public InvoiceBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public InvoiceBuilder period(LocalDate period) {
            this.period = period;
            return this;
        }

        public InvoiceBuilder rentAmount(BigDecimal rentAmount) {
            this.rentAmount = rentAmount;
            return this;
        }

        public InvoiceBuilder securityAmount(BigDecimal securityAmount) {
            this.securityAmount = securityAmount;
            return this;
        }

        public InvoiceBuilder garbageAmount(BigDecimal garbageAmount) {
            this.garbageAmount = garbageAmount;
            return this;
        }

        public InvoiceBuilder otherAmounts(Map<String, BigDecimal> otherAmounts) {
            this.otherAmounts = otherAmounts;
            return this;
        }

        public InvoiceBuilder invoiceNo(String invoiceNo) {
            this.invoiceNo = invoiceNo;
            return this;
        }

        public InvoiceBuilder occupationId(String occupationId) {
            this.occupationId = occupationId;
            return this;
        }

        public Invoice build() {
            Invoice invoice = new Invoice();
            invoice.setType(this.type);
            invoice.setPeriod(this.period);
            invoice.setRentAmount(this.rentAmount);
            invoice.setSecurityAmount(this.securityAmount);
            invoice.setGarbageAmount(this.garbageAmount);
            invoice.setOtherAmounts(this.otherAmounts);
            invoice.setInvoiceNo(this.invoiceNo);
            invoice.setOccupationId(this.occupationId);
            return invoice;
        }
    }
}
