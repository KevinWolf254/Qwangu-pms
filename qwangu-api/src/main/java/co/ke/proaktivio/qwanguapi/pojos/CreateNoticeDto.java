package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateNoticeDto extends NoticeDto{
        private String occupationId;

        public CreateNoticeDto(LocalDateTime notificationDate, LocalDateTime vacatingDate, String occupationId) {
                super(notificationDate, vacatingDate);
                this.occupationId = occupationId;
        }
}