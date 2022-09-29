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
@Document(value = "BOOKING_REFUND")
public class BookingRefund {
    @Id
    private String id;
    private BigDecimal amount;
    private String refundDetails;
    private String receivableId;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    @NoArgsConstructor
    public static class BookingRefundBuilder {
        private BigDecimal amount;
        private String refundDetails;
        private String receivableId;

        public BookingRefundBuilder amount(BigDecimal amount) {
            this.amount = amount;
            return this;
        }

        public BookingRefundBuilder refundDetails(String refundDetails) {
            this.refundDetails = refundDetails;
            return this;
        }

        public BookingRefundBuilder receivableId(String receivableId) {
            this.receivableId = receivableId;
            return this;
        }

        public BookingRefund build() {
            var refund = new BookingRefund();
            refund.setAmount(this.amount);
            refund.setRefundDetails(this.refundDetails);
            refund.setReceivableId(this.receivableId);
            return refund;

        }
    }
}
