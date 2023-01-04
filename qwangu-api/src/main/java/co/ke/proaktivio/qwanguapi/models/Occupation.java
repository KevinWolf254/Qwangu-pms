package co.ke.proaktivio.qwanguapi.models;

import lombok.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Supplier;

@Data
@ToString(exclude = {"generateOccupationNumber"})
@NoArgsConstructor
@Document(value = "OCCUPATION")
public class Occupation {
    @Id
    private String id;
    private Status status;
    @Indexed(unique = true)
    private String number;
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
        PENDING_OCCUPATION("PENDING_OCCUPATION"),
        CURRENT("CURRENT"),
        PENDING_VACATING("PENDING_VACATING"),
        VACATED("VACATED");

        private final String state;
    }

    @Transient
    protected Supplier<String> generateOccupationNumber = () -> "O" + RandomStringUtils.randomAlphanumeric(5).toUpperCase();

    @NoArgsConstructor
    public static class OccupationBuilder {
        private Status status;
        private LocalDate startDate;
        private String tenantId;
        private String unitId;

        public OccupationBuilder status(Status status) {
            this.status = status;
            return this;
        }

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
            occupation.setNumber(occupation.generateOccupationNumber.get());
            occupation.setStatus(this.status);
            occupation.setStartDate(this.startDate);
            occupation.setTenantId(this.tenantId);
            occupation.setUnitId(this.unitId);
            return occupation;
        }
    }
}
