package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(value = "PAYMENT")
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    private String id;
    private Type type;
    private Status status;
    private String transactionId;
    private String transactionType;
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
    private LocalDateTime created;
    private LocalDateTime modified;

    public enum Type{
        MPESA_PAY_BILL,
        MPESA_TILL,
        PAYPAL
    }
    public enum Status {
        BOOKING_NEW,
        RENT_NEW,
        PROCESSED_SUCCESSFULLY,
        PROCESSED_FAILED
    }
}
