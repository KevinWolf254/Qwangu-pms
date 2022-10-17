package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "RECEIPT")
public class Receipt {
    @Id
    private String id;
    @Indexed(unique = true)
    private String receiptNo;
    private String occupationId;
    private String paymentId;

    public static class ReceiptBuilder {
        private String receiptNo;
        private String occupationId;
        private String paymentId;

        public ReceiptBuilder receiptNo(String receiptNo) {
            this.receiptNo = receiptNo;
            return this;
        }

        public ReceiptBuilder occupationId(String occupationId) {
            this.occupationId = occupationId;
            return this;
        }

        public ReceiptBuilder paymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public Receipt build() {
            var receipt = new Receipt();
            receipt.setReceiptNo(receiptNo);
            receipt.setOccupationId(occupationId);
            receipt.setPaymentId(paymentId);
            return receipt;
        }
    }
}
