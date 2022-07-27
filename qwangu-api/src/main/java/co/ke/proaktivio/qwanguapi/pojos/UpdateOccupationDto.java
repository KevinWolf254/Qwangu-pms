package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Occupation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateOccupationDto extends OccupationDto{
    private Occupation.Status status;

    public UpdateOccupationDto(Occupation.Status status, LocalDateTime started, LocalDateTime ended) {
        super(started, ended);
        this.status = status;
    }
}
