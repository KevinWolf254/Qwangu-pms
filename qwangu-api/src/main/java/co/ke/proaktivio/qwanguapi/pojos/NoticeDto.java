package co.ke.proaktivio.qwanguapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class NoticeDto {
    private LocalDate notificationDate;
    private LocalDate vacatingDate;
}
