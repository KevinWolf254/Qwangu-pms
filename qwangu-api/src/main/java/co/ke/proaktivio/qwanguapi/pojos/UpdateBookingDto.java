package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Booking;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UpdateBookingDto extends BookingDto {
    private Booking.Status status;

    public UpdateBookingDto(Booking.Status status, LocalDate occupation) {
        super(occupation);
        this.status = status;
    }
}
