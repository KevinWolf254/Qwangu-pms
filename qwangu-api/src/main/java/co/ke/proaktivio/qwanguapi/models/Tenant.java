package co.ke.proaktivio.qwanguapi.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "TENANT")
public class Tenant {
    @Id
    private String id;
    private String firstName;
    private String middleName;
    private String surname;
    @Indexed(unique = true)
    private String mobileNumber;
    @Indexed(unique = true)
    private String emailAddress;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String updatedBy;

    @NoArgsConstructor
    public static class TenantBuilder {
        private String firstName;
        private String middleName;
        private String surname;
        private String mobileNumber;
        private String emailAddress;

        public TenantBuilder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public TenantBuilder middleName(String middleName) {
            this.middleName = middleName;
            return this;
        }

        public TenantBuilder surname(String surname) {
            this.surname = surname;
            return this;
        }

        public TenantBuilder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public TenantBuilder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public Tenant build() {
            var tenant = new Tenant();
            tenant.setFirstName(this.firstName);
            tenant.setMiddleName(this.middleName);
            tenant.setSurname(this.surname);
            tenant.setMobileNumber(this.mobileNumber);
            tenant.setEmailAddress(this.emailAddress);
            return tenant;
        }
    }
}
