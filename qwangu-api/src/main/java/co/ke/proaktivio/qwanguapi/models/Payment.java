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
    @CreatedDate
    private LocalDateTime createdOn;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    
    @Getter
    @RequiredArgsConstructor
    public enum PaymentType {
        MOBILE("MOBILE"),
        CARD("CARD"),
        PAYPAL("PAYPAL");
        private final String type;
    }

    @Getter
    @RequiredArgsConstructor
    public enum PaymentStatus {
    	UNCLAIMED("UNCLAIMED"),
        CLAIMED("CLAIMED");
        private final String state;
    }
    
    public static class PaymentBuilder {
        private PaymentType type;
        private String occupationNumber;
        private String referenceNumber;
        private Unit.Currency currency;
        private BigDecimal amount;
        
		public PaymentBuilder type(PaymentType type) {
			this.type = type;
			return this;
		}
		
		public PaymentBuilder occupationNumber(String occupationNumber) {
			this.occupationNumber = occupationNumber;
			return this;
		}
		
		public PaymentBuilder referenceNumber(String referenceNumber) {
			this.referenceNumber = referenceNumber;
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

		public Payment build() {
			var payment = new Payment();
			payment.setStatus(PaymentStatus.UNCLAIMED);
			payment.setType(type);
			payment.setOccupationNumber(occupationNumber);
			payment.setReferenceNumber(referenceNumber);
			payment.setCurrency(currency);
			payment.setAmount(amount);
			return payment;
		}
    }
}
