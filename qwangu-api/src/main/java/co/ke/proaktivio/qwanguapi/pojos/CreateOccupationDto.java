package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateOccupationDto extends OccupationDto{

    public CreateOccupationDto(Boolean active, LocalDateTime started, LocalDateTime ended, String tenantId, String unitId) {
        super(active, started, ended);
        this.tenantId = tenantId;
        this.unitId = unitId;
    }

    private String tenantId;
    private String unitId;
}
