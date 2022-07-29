package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "UNIT")
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Unit {
    @Id
    private String id;
    private Status status;
    @Indexed(unique = true)
    private String accountNo;
    private Type type;
    private Identifier identifier;
    private Integer floorNo;
    private Integer noOfBedrooms;
    private Integer noOfBathrooms;
    private Integer advanceInMonths;
    private Currency currency;
    private Integer rentPerMonth;
    private Integer securityPerMonth;
    private Integer garbagePerMonth;
    private LocalDateTime created;
    private LocalDateTime modified;
    private String apartmentId;

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        VACANT("VACANT"),
        AWAITING_OCCUPATION("AWAITING_OCCUPATION"),
        OCCUPIED("OCCUPIED");

        private final String state;
    }

    @Getter
    @AllArgsConstructor
    public enum Type{
        APARTMENT_UNIT("APARTMENT_UNIT"),
        TOWN_HOUSE("TOWN_HOUSE"),
        MAISONETTES("MAISONETTES"),
        VILLA("VILLA");

        private String type;
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
