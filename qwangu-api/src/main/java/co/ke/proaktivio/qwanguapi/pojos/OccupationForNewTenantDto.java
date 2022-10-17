package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class OccupationForNewTenantDto extends OccupationDto {
    private TenantDto tenant;

    public OccupationForNewTenantDto(LocalDate startDate, String unitId, String paymentId, TenantDto tenant) {
        super(startDate, unitId, paymentId);
        this.tenant = tenant;
    }
}
