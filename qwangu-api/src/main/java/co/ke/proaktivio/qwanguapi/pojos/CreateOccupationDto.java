package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateOccupationDto extends OccupationDto{
    private String tenantId;
    private String unitId;

    public CreateOccupationDto(LocalDateTime started, LocalDateTime ended, String tenantId, String unitId) {
        super(started, ended);
        this.tenantId = tenantId;
        this.unitId = unitId;
    }
}
