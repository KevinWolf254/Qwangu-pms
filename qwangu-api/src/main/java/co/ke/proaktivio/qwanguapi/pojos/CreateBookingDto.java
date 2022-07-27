package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateBookingDto extends BookingDto {
    private String paymentId;
    private String unitId;

    public CreateBookingDto(LocalDateTime occupation, String paymentId, String unitId) {
        super(occupation);
        this.paymentId = paymentId;
        this.unitId = unitId;
    }
}
