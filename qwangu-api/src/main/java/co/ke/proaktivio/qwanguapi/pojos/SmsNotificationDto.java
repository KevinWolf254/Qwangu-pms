package co.ke.proaktivio.qwanguapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SmsNotificationDto {
    private String phoneNumber;
    private String message;
}