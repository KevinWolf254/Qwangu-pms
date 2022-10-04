package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
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
    @Indexed
    private String occupationId;
    private String invoiceId;
    private String receiptId;
    private BigDecimal totalAmountOwed;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalAmountCarriedForward;
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
        CREDIT("CREDIT"),
        DEBIT("DEBIT");

        private final String type;
    }

    public static class OccupationTransactionBuilder {
        private Type type;
        private String occupationId;
        private String invoiceId;
        private String receiptId;
        private BigDecimal totalAmountOwed;
        private BigDecimal totalAmountPaid;
        private BigDecimal totalAmountCarriedForward;

        public OccupationTransactionBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public OccupationTransactionBuilder occupationId(String occupationId) {
            this.occupationId = occupationId;
            return this;
        }

        public OccupationTransactionBuilder invoiceId(String invoiceId) {
            this.invoiceId = invoiceId;
            return this;
        }

        public OccupationTransactionBuilder receiptId(String receiptId) {
            this.receiptId = receiptId;
            return this;
        }

        public OccupationTransactionBuilder totalAmountOwed(BigDecimal totalAmountOwed) {
            this.totalAmountOwed = totalAmountOwed;
            return this;
        }

        public OccupationTransactionBuilder totalAmountPaid(BigDecimal totalAmountPaid) {
            this.totalAmountPaid = totalAmountPaid;
            return this;
        }

        public OccupationTransactionBuilder totalAmountCarriedForward(BigDecimal totalAmountCarriedForward) {
            this.totalAmountCarriedForward = totalAmountCarriedForward;
            return this;
        }

        public OccupationTransaction build() {
            OccupationTransaction occupationTransaction = new OccupationTransaction();
            occupationTransaction.setType(this.type);
            occupationTransaction.setOccupationId(this.occupationId);
            occupationTransaction.setInvoiceId(this.invoiceId);
            occupationTransaction.setReceiptId(this.receiptId);
            occupationTransaction.setReceiptId(this.receiptId);
            occupationTransaction.setTotalAmountOwed(this.totalAmountOwed);
            occupationTransaction.setTotalAmountPaid(this.totalAmountPaid);
            occupationTransaction.setTotalAmountCarriedForward(this.totalAmountCarriedForward);
            return occupationTransaction;
        }
    }
}
