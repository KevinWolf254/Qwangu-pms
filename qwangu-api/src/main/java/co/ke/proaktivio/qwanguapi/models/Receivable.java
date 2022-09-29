package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "RECEIVABLE")
public class Receivable {
    @Id
    private String id;
    private Type type;
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
        RENT("RENT"),
        PENALTY("PENALTY"),
        BOOKING("BOOKING");

        private final String name;
    }

    public static class ReceivableBuilder {
        private Type type;
        private LocalDate period;
        private BigDecimal rentAmount;
        private BigDecimal securityAmount;
        private BigDecimal garbageAmount;
        private Map<String, BigDecimal> otherAmounts;

        public ReceivableBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public ReceivableBuilder period(LocalDate period) {
            this.period = period;
            return this;
        }

        public ReceivableBuilder rentAmount(BigDecimal rentAmount) {
            this.rentAmount = rentAmount;
            return this;
        }

        public ReceivableBuilder securityAmount(BigDecimal securityAmount) {
            this.securityAmount = securityAmount;
            return this;
        }

        public ReceivableBuilder garbageAmount(BigDecimal garbageAmount) {
            this.garbageAmount = garbageAmount;
            return this;
        }

        public ReceivableBuilder otherAmounts(Map<String, BigDecimal> otherAmounts) {
            this.otherAmounts = otherAmounts;
            return this;
        }

        public Receivable build() {
            Receivable receivable = new Receivable();
            receivable.setType(this.type);
            receivable.setPeriod(this.period);
            receivable.setRentAmount(this.rentAmount);
            receivable.setSecurityAmount(this.securityAmount);
            receivable.setGarbageAmount(this.garbageAmount);
            receivable.setOtherAmounts(this.otherAmounts);
            return receivable;
        }
    }
}
