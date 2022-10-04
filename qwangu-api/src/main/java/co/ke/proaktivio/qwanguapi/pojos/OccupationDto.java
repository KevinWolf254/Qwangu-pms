package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OccupationDto {
    private String occupationId;
    private Occupation.Status status;
    private LocalDateTime started;
    private LocalDateTime ended;
//    private String tenantId;
    private TenantDto tenant;
    private String unitId;
}
