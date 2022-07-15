package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitDto {
    private Boolean vacant;
    private Unit.Type type;
    private Unit.Identifier identifier;
    private Integer floorNo;
    private Integer noOfBedrooms;
    private Integer noOfBathrooms;
    private Integer advanceInMonths;
    private Unit.Currency currency;
    private Integer rentPerMonth;
    private Integer securityPerMonth;
    private Integer garbagePerMonth;
    private String apartmentId;
}
