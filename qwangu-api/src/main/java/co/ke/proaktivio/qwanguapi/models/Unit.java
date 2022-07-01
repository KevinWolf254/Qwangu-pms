package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(value = "UNIT")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Data
public class Unit {
    @Id
    private String id;
    private String name;
    private String account;
    private UnitType type;
    private String number;
    private Integer floor;
    private Integer bedrooms;
    private Integer bathrooms;
    private Integer rent;
    private Currency currency;
    private Integer security;
    private Integer garbage;
    private LocalDateTime created;
    private LocalDateTime modified;
    private String apartmentId;

    public enum UnitType{

    }

    public enum Currency {
        KES,
        DOLLAR,
        POUND
    }
}
