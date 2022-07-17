package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateOccupationDto extends OccupationDto{
    public UpdateOccupationDto(Boolean active, LocalDateTime started, LocalDateTime ended) {
        super(active, started, ended);
    }
}
