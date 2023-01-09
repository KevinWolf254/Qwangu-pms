package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(value = "PAYMENT")
public class Payment {
    @Id
    private String id;
    private Status status;
    private Type type;
    @Indexed(unique = true)
    private String transactionId;
    private String transactionType;
    private LocalDateTime transactionTime;
    private Unit.Currency currency;
    private BigDecimal amount;
    private String shortCode;
    private String referenceNo;
    private String invoiceNo;
    private String balance;
    private String thirdPartyId;
    private String mobileNumber;
    private String firstName;
    private String middleName;
    private String lastName;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        MPESA_PAY_BILL("MPESA_PAY_BILL"),
        MPESA_TILL("MPESA_TILL"),
        PAYPAL("PAYPAL");
        private final String type;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        NEW("NEW"),
        PROCESSED("PROCESSED");
        private final String state;
    }

    public static class PaymentBuilder {
        private Status status;
        private Type type;
        private String transactionId;
        private String transactionType;
        private LocalDateTime transactionTime;
        private Unit.Currency currency;
        private BigDecimal amount;
        private String shortCode;
        private String referenceNo;
        private String invoiceNo;
        private String balance;
        private String thirdPartyId;
        private String mobileNumber;
        private String firstName;
        private String middleName;
        private String lastName;

        public PaymentBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public PaymentBuilder type(Type type) {
            this.type = type;
            return this;
        }

        public PaymentBuilder transactionId(String transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public PaymentBuilder transactionType(String transactionType) {
            this.transactionType = transactionType;
            return this;
        }

        public PaymentBuilder transactionTime(LocalDateTime transactionTime) {
            this.transactionTime = transactionTime;
            return this;
        }

        public PaymentBuilder currency(Unit.Currency currency) {
            this.currency = currency;
            return this;
        }

        public PaymentBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public PaymentBuilder shortCode(String shortCode) {
            this.shortCode = shortCode;
            return this;
        }

        public PaymentBuilder referenceNo(String referenceNo) {
            this.referenceNo = referenceNo;
            return this;
        }

        public PaymentBuilder invoiceNo(String invoiceNo) {
            this.invoiceNo = invoiceNo;
            return this;
        }

        public PaymentBuilder balance(String balance) {
            this.balance = balance;
            return this;
        }

        public PaymentBuilder thirdPartyId(String thirdPartyId) {
            this.thirdPartyId = thirdPartyId;
            return this;
        }

        public PaymentBuilder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public PaymentBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public PaymentBuilder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public PaymentBuilder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Payment build() {
            var payment = new Payment();
            payment.setStatus(this.status != null ? this.status : Status.NEW);
            payment.setType(this.type);
            payment.setTransactionId(this.transactionId);
            payment.setTransactionType(this.transactionType);
            payment.setTransactionTime(this.transactionTime);
            payment.setCurrency(this.currency);
            payment.setAmount(this.amount);
            payment.setShortCode(this.shortCode);
            payment.setReferenceNo(this.referenceNo);
            payment.setInvoiceNo(this.invoiceNo);
            payment.setBalance(this.balance);
            payment.setThirdPartyId(this.thirdPartyId);
            payment.setMobileNumber(this.mobileNumber);
            payment.setFirstName(this.firstName);
            payment.setMiddleName(this.middleName);
            payment.setLastName(this.lastName);
            return payment;
        }
    }
}
