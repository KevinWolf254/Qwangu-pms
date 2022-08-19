package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.RentAdvance;
import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRentAdvanceDto {
    private RentAdvance.Status status;
    private String returnDetails;
    private LocalDate returnedOn;
}
