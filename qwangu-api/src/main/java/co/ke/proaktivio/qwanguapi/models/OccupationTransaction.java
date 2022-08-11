package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "OCCUPATION_TRANSACTION")
public class OccupationTransaction {
    @Id
    private String id;
    private BigDecimal totalAmountOwed;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalAmountRemaining;
    private String occupationId;
    private String receivableId;
    private String paymentId;
    private LocalDateTime createdOn;
}
