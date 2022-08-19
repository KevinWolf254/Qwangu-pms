package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitDto {
    private Unit.Status status;
    private Unit.Type type;
    private Unit.Identifier identifier;
    private Integer floorNo;
    private Integer noOfBedrooms;
    private Integer noOfBathrooms;
    private Integer advanceInMonths;
    private Unit.Currency currency;
    private BigDecimal rentPerMonth;
    private BigDecimal securityPerMonth;
    private BigDecimal garbagePerMonth;
    private String apartmentId;
}
