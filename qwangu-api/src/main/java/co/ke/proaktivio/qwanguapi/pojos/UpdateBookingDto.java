package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateBookingDto extends BookingDto {
    public UpdateBookingDto(LocalDate occupation) {
        super(occupation);
    }
}
