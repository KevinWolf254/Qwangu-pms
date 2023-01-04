package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OccupationForNewTenantDto {
    private String tenantId;
    private TenantDto tenant;
    private OccupationDto occupation;
}
