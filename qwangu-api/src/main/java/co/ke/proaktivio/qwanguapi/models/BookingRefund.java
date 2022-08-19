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
@Document(value = "BOOKING_REFUND")
public class BookingRefund {
    @Id
    private String id;
    private BigDecimal amount;
    private String refundDetails;
    private String receivableId;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
}
