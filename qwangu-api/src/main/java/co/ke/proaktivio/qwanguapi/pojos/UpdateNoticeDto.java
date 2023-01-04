package co.ke.proaktivio.qwanguapi.pojos;

import co.ke.proaktivio.qwanguapi.models.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateNoticeDto extends NoticeDto {
    private Notice.Status status;

    public UpdateNoticeDto(LocalDate notificationDate, LocalDate vacatingDate, Notice.Status status) {
        super(notificationDate, vacatingDate);
        this.status = status;
    }
}
