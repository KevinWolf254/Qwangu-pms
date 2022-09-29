package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "OCCUPATION")
public class Occupation {
    @Id
    private String id;
    private Status status;
    private LocalDateTime startedOn;
    private LocalDateTime endedOn;
    private String tenantId;
    private String unitId;
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
        BOOKED("BOOKED"),
        CURRENT("CURRENT"),
        PREVIOUS("PREVIOUS");

        private final String state;
    }

    @NoArgsConstructor
    public static class OccupationBuilder {
        private Status status;
        private LocalDateTime startedOn;
        private LocalDateTime endedOn;
        private String tenantId;
        private String unitId;

        public OccupationBuilder status(Status status) {
            this.status = status;
            return this;
        }

        public OccupationBuilder startedOn(LocalDateTime startedOn) {
            this.startedOn = startedOn;
            return this;
        }

        public OccupationBuilder endedOn(LocalDateTime endedOn) {
            this.endedOn = endedOn;
            return this;
        }

        public OccupationBuilder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public OccupationBuilder unitId(String unitId) {
            this.unitId = unitId;
            return this;
        }

        public Occupation build() {
            var occupation = new Occupation();
            occupation.setStatus(this.status);
            occupation.setStartedOn(this.startedOn);
            occupation.setEndedOn(this.endedOn);
            occupation.setTenantId(this.tenantId);
            occupation.setUnitId(this.unitId);
            return occupation;
        }
    }
}
