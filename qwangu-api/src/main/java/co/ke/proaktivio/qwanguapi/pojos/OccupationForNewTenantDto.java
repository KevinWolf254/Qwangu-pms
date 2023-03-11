package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OccupationForNewTenantDto {
    private String tenantId;
    private TenantDto tenant;
    private OccupationDto occupation;
}
