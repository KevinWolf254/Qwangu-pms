package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "PAYMENT")
public class Payment {
    @Id
    private String id;
    private Status status;
    private Type type;
    private String transactionId;
    private String transactionType;
    private LocalDateTime transactionTime;
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
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    public Payment(String id, Status status, Type type, String transactionId, String transactionType,
                   LocalDateTime transactionTime, BigDecimal amount, String shortCode, String referenceNo,
                   String invoiceNo, String balance, String thirdPartyId, String mobileNumber, String firstName,
                   String middleName, String lastName) {
        this.id = id;
        this.status = status;
        this.type = type;
        this.transactionId = transactionId;
        this.transactionType = transactionType;
        this.transactionTime = transactionTime;
        this.amount = amount;
        this.shortCode = shortCode;
        this.referenceNo = referenceNo;
        this.invoiceNo = invoiceNo;
        this.balance = balance;
        this.thirdPartyId = thirdPartyId;
        this.mobileNumber = mobileNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type{
        MPESA_PAY_BILL("MPESA_PAY_BILL"),
        MPESA_TILL("MPESA_TILL"),
        PAYPAL("PAYPAL");
        private final String type;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Status{
        NEW("NEW"),
        PROCESSED("PROCESSED");
        private final String state;
    }
}
