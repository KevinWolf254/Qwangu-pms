package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateNoticeDto extends NoticeDto {
    private Boolean active;

    public UpdateNoticeDto(Boolean active, LocalDateTime notificationDate, LocalDateTime vacatingDate) {
        super(notificationDate, vacatingDate);
        this.active = active;
    }
}
