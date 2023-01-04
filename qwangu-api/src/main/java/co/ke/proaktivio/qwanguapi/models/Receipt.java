package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

@Data
@ToString(exclude = {"generateReceiptNumber"})
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "RECEIPT")
public class Receipt {
    @Id
    private String id;
    @Indexed(unique = true)
    private String number;
    private String occupationId;
    private String paymentId;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    @Transient
    protected BiFunction<Receipt, Occupation, String> generateReceiptNumber = (receipt, occupation) -> {
        String prefix = "RCT";
        String previousReceiptNumber = receipt.getNumber();

        if (!StringUtils.hasText(previousReceiptNumber)) {
            return prefix + "100000" + occupation.getNumber();
        }

        Integer previousNumber = Integer.valueOf(previousReceiptNumber.substring(3, 9));
        var nextNumber = previousNumber + 1;
        return prefix + nextNumber + occupation.getNumber();
    };

    public static class ReceiptBuilder {
        private String number;
        private String occupationId;
        private String paymentId;
        private final Receipt receipt = new Receipt();

        public ReceiptBuilder number(Receipt previousReceipt, Occupation occupation) {
            this.number = receipt.generateReceiptNumber.apply(previousReceipt, occupation);
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
            receipt.setNumber(number);
            receipt.setOccupationId(occupationId);
            receipt.setPaymentId(paymentId);
            return receipt;
        }
    }
}
