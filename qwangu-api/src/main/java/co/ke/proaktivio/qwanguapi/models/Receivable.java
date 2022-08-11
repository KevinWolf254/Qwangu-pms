package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "RECEIVABLE")
public class Receivable {
    @Id
    private String id;
    private Type type;
    private LocalDate period;
    private BigDecimal rentAmount;
    private BigDecimal securityAmount;
    private BigDecimal garbageAmount;
    private Map<String, BigDecimal> otherAmounts;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;

    @Getter
    @RequiredArgsConstructor
    public enum Type {
        RENT("RENT"),
        PENALTY("PENALTY"),
        BOOKING("BOOKING");

        private final String name;
    }
}
