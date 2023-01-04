package co.ke.proaktivio.qwanguapi.pojos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CreateNoticeDto extends NoticeDto {
        private String occupationId;

        public CreateNoticeDto(LocalDate notifiedOn, LocalDate vacatingOn, String occupationId) {
                super(notifiedOn, vacatingOn);
                this.occupationId = occupationId;
        }
}
