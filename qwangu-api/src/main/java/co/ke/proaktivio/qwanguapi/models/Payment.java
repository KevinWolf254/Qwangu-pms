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
    private PaymentStatus status;
    @Indexed
    private PaymentType type;
    @Indexed
    private String occupationNumber;
    @Indexed(unique = true)
    private String referenceNumber;
    private Unit.Currency currency;
    private BigDecimal amount;
    @Indexed(unique = true)
    private String mpesaPaymentId;
    @CreatedDate
    private LocalDateTime createdOn;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    
    @Getter
    @RequiredArgsConstructor
    public enum PaymentType {
        MPESA_PAY_BILL("MPESA_PAY_BILL"),
        MPESA_TILL("MPESA_TILL"),
        PAYPAL("PAYPAL");
        private final String type;
    }

    @Getter
    @RequiredArgsConstructor
    public enum PaymentStatus {
    	UNPROCESSED("UNPROCESSED"),
        PROCESSED("PROCESSED");
        private final String state;
    }
}
