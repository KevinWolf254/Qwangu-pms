package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "RENT_ADVANCE")
public class RentAdvance {
    @Id
    private String id;
    private Status status;
    private String returnDetails;
    private String occupationId;
    private String paymentId;
    private LocalDate returnedOn;
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
    public enum Status {
        HOLDING("HOLDING"),
        RELEASED("RELEASED"),
        RENT_PAYMENT("RENT_PAYMENT");

        private final String state;
    }

    @NoArgsConstructor
    public static class RentAdvanceBuilder {
        private Status status;
        private String returnDetails;
        private String occupationId;
        private String paymentId;
        private LocalDate returnedOn;

        public RentAdvanceBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public RentAdvanceBuilder returnDetails(String returnDetails) {
            this.returnDetails = returnDetails;
            return this;
        }

        public RentAdvanceBuilder occupationId(String occupationId) {
            this.occupationId = occupationId;
            return this;
        }

        public RentAdvanceBuilder paymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }

        public RentAdvanceBuilder returnedOn(LocalDate returnedOn) {
            this.returnedOn = returnedOn;
            return this;
        }

        public RentAdvance build() {
            var advance = new RentAdvance();
            advance.setStatus(this.status);
            advance.setReturnDetails(this.returnDetails);
            advance.setOccupationId(this.occupationId);
            advance.setPaymentId(this.paymentId);
            advance.setReturnedOn(this.returnedOn);
            return advance;
        }
    }
}
