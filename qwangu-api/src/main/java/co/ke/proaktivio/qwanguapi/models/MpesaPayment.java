package co.ke.proaktivio.qwanguapi.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@Document(value = "MPESA_PAYMENT")
public class MpesaPayment {

    @Id
    private String id;
    private MpesaPaymentType type;
    private Boolean isProcessed;
    @Indexed(unique = true)
    private String transactionId;
    @Indexed
    private String transactionType;
    private LocalDateTime transactionTime;
    private Unit.Currency currency;
    private BigDecimal amount;
    @Indexed
    private String shortCode;
    @Indexed
    private String referenceNumber;
    private String invoiceNo;
    private String balance;
    private String thirdPartyId;
    @Indexed
    private String mobileNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    @CreatedDate
    private LocalDateTime createdOn;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    

    @Getter
    @RequiredArgsConstructor
    public enum MpesaPaymentType {
        MPESA_PAY_BILL("MPESA_PAY_BILL"),
        MPESA_TILL("MPESA_TILL");
    	
        private final String name;
    }
}
