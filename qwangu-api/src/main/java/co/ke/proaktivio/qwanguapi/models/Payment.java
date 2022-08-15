package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
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
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    public enum Type{
        MPESA_PAY_BILL,
        MPESA_TILL,
        PAYPAL
    }

    @Getter
    @RequiredArgsConstructor
    public enum Status{
        NEW("NEW"),
        PROCESSED("PROCESSED");
        private final String state;
    }
}
