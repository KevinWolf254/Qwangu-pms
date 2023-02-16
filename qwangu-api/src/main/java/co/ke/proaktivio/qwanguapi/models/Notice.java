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
    private Status status;
    private LocalDate notificationDate;
    private LocalDate vacatingDate;
    private String occupationId;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    @Getter
    @RequiredArgsConstructor
    public enum Status {
        ACTIVE("ACTIVE"),
        FULFILLED("FULFILLED"),
        CANCELLED("CANCELLED");

        private final String state;
    }

    @NoArgsConstructor
    public static class NoticeBuilder {
        private Status status;
        //        private Boolean isActive;
        private LocalDate notificationDate;
        private LocalDate vacatingDate;
        private String occupationId;

        public NoticeBuilder status(Status status) {
            this.status = status;
            return this;
        }

//        public NoticeBuilder isActive(Boolean active) {
//            isActive = active;
//            return this;
//        }

        public NoticeBuilder notificationDate(LocalDate notifiedOn) {
            this.notificationDate = notifiedOn;
            return this;
        }

        public NoticeBuilder vacatingDate(LocalDate vacatingOn) {
            this.vacatingDate = vacatingOn;
            return this;
        }

        public NoticeBuilder occupationId(String occupationId) {
            this.occupationId = occupationId;
            return this;
        }

        public Notice build() {
            var notice = new Notice();
            notice.setStatus(this.status);
            notice.setNotificationDate(this.notificationDate);
            notice.setVacatingDate(this.vacatingDate);
            notice.setOccupationId(this.occupationId);
            return notice;
        }
    }
}
