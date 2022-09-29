package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "NOTICE")
public class Notice {
    @Id
    private String id;
    private Boolean isActive;
    private LocalDateTime notifiedOn;
    private LocalDate vacatingOn;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;
    private String occupationId;

    @NoArgsConstructor
    public static class NoticeBuilder {
        private Boolean isActive;
        private LocalDateTime notifiedOn;
        private LocalDate vacatingOn;
        private String occupationId;

        public NoticeBuilder isActive(Boolean active) {
            isActive = active;
            return this;
        }

        public NoticeBuilder notifiedOn(LocalDateTime notifiedOn) {
            this.notifiedOn = notifiedOn;
            return this;
        }

        public NoticeBuilder vacatingOn(LocalDate vacatingOn) {
            this.vacatingOn = vacatingOn;
            return this;
        }

        public NoticeBuilder occupationId(String occupationId) {
            this.occupationId = occupationId;
            return this;
        }

        public Notice build() {
            var notice = new Notice();
            notice.setIsActive(this.isActive);
            notice.setNotifiedOn(this.notifiedOn);
            notice.setVacatingOn(this.vacatingOn);
            notice.setOccupationId(this.occupationId);
            return notice;
        }
    }
}
