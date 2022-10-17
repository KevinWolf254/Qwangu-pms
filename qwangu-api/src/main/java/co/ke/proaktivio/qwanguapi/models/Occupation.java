package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@Document(value = "OCCUPATION")
public class Occupation {
    @Id
    private String id;
    private Status status;
    @Indexed(unique = true)
    private String occupationNo;
    private LocalDate startDate;
    private LocalDate endDate;
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
        VACATED("VACATED");

        private final String state;
    }

    @NoArgsConstructor
    public static class OccupationBuilder {
        private LocalDate startDate;
        private String tenantId;
        private String unitId;

        public OccupationBuilder startDate(LocalDate startDate) {
            this.startDate = startDate;
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
            occupation.setStatus(Status.CURRENT);
            occupation.setStartDate(this.startDate);
            occupation.setTenantId(this.tenantId);
            occupation.setUnitId(this.unitId);
            return occupation;
        }
    }
}
