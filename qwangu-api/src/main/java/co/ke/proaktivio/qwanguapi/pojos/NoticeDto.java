package co.ke.proaktivio.qwanguapi.pojos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class NoticeDto {
    private Boolean active;
    private LocalDateTime notificationDate;
    private LocalDateTime vacatingDate;
}
