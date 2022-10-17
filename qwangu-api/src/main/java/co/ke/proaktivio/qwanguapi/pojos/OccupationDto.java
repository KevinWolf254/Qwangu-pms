package co.ke.proaktivio.qwanguapi.pojos;

import lombok.*;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OccupationDto {
    private LocalDate startDate;
    private String unitId;
    private String paymentId;
}