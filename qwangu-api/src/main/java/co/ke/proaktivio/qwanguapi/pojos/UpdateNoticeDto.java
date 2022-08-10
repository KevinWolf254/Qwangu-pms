package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class UpdateNoticeDto extends NoticeDto {
    private Boolean isActive;

    public UpdateNoticeDto(Boolean isActive, LocalDateTime notifiedOn, LocalDate vacatingOn) {
        super(notifiedOn, vacatingOn);
        this.isActive = isActive;
    }
}
