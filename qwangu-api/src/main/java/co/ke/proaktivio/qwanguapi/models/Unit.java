package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "UNIT")
public class Unit {
    @Id
    private String id;
    private Status status;
    private Boolean isBooked;
    @Indexed(unique = true)
    private String accountNo;
    private Type type;
    private Identifier identifier;
    private Integer floorNo;
    private Integer noOfBedrooms;
    private Integer noOfBathrooms;
    private Integer advanceInMonths;
    private Currency currency;
    private BigDecimal rentPerMonth;
    private BigDecimal securityPerMonth;
    private BigDecimal garbagePerMonth;
    private LocalDateTime createdOn;
    private LocalDateTime modifiedOn;
    private String apartmentId;

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        VACANT("VACANT"),
        OCCUPIED("OCCUPIED");

        private final String state;
    }

    @Getter
    @RequiredArgsConstructor
    public enum Type{
        APARTMENT_UNIT("APARTMENT_UNIT"),
        TOWN_HOUSE("TOWN_HOUSE"),
        MAISONETTES("MAISONETTES"),
        VILLA("VILLA");

        private final String type;
    }

    public enum Identifier {
        A,B,C,D,E,F,G,H,I,J
    }

    public enum Currency {
        KES,
        DOLLAR,
        POUND
    }
}
