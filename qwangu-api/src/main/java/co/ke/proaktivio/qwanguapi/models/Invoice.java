package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.BiFunction;

@Data
@ToString(exclude = {"generateInvoiceNumber"})
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "INVOICE")
public class Invoice {
    @Id
    private String id;
    @Indexed(unique = true)
    private String number;
    private Type type;
    private LocalDate startDate;
    private LocalDate endDate;
    private Unit.Currency currency;
    private BigDecimal rentAmount;
    private BigDecimal securityAmount;
    private BigDecimal garbageAmount;
    private Map<String, BigDecimal> otherAmounts;
    private String occupationId;
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
        PENALTY("PENALTY"),
        UTILITIES("UTILITIES");

        private final String name;
    }

    @Transient
    protected BiFunction<Invoice, Occupation, String> generateInvoiceNumber = (invoice, occupation) -> {
        String prefix = "INV";
        String previousInvoiceNumber = invoice.getNumber();

        if (!StringUtils.hasText(previousInvoiceNumber)) {
            return prefix + "100000" + occupation.getNumber();
        }

        Integer previousNumber = Integer.valueOf(previousInvoiceNumber.substring(3, 9));
        var nextNumber = previousNumber + 1;
        return prefix + nextNumber + occupation.getNumber();
    };

    public static class InvoiceBuilder {
        private Type type;
        private String number;
        private LocalDate startDate;
        private LocalDate endDate;
        private Unit.Currency currency;
        private BigDecimal rentAmount;
        private BigDecimal securityAmount;
        private BigDecimal garbageAmount;
        private Map<String, BigDecimal> otherAmounts;
        private String occupationId;
        private final Invoice invoice = new Invoice();

        public InvoiceBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public InvoiceBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
            return this;
        }

        public InvoiceBuilder endDate(LocalDate endDate) {
            this.endDate = endDate;
            return this;
        }

        public InvoiceBuilder currency(Unit.Currency currency) {
            this.currency = currency;
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

        public InvoiceBuilder number(Invoice previousInvoice, Occupation occupation) {
            this.number = invoice.generateInvoiceNumber.apply(previousInvoice, occupation);
            return this;
        }

        public InvoiceBuilder occupationId(String occupationId) {
            this.occupationId = occupationId;
            return this;
        }

        public Invoice build() {
            invoice.setType(this.type);
            invoice.setStartDate(this.startDate);
            invoice.setEndDate(this.endDate);
            invoice.setCurrency(this.currency);
            invoice.setRentAmount(this.rentAmount);
            invoice.setSecurityAmount(this.securityAmount);
            invoice.setGarbageAmount(this.garbageAmount);
            invoice.setOtherAmounts(this.otherAmounts);
            invoice.setNumber(this.number);
            invoice.setOccupationId(this.occupationId);
            return invoice;
        }
    }
}
