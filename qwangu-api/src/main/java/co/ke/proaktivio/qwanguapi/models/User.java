package co.ke.proaktivio.qwanguapi.models;

import co.ke.proaktivio.qwanguapi.pojos.Person;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(value = "USER")
public class User {
    @Id
    private String id;
    private Person person;
    @Indexed(unique = true)
    private String emailAddress;
    private String roleId;
    private String password;
    private Boolean isAccountExpired;
    private Boolean isCredentialsExpired;
    private Boolean isAccountLocked;
    private Boolean isEnabled;
    @CreatedDate
    private LocalDateTime createdOn;
    @CreatedBy
    private String createdBy;
    @LastModifiedDate
    private LocalDateTime modifiedOn;
    @LastModifiedBy
    private String modifiedBy;

    public User(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    @NoArgsConstructor
    public static class UserBuilder {
        private Person person;
        private String emailAddress;
        private String roleId;

        public UserBuilder person(Person person) {
            this.person = person;
            return this;
        }

        public UserBuilder emailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        public UserBuilder roleId(String roleId) {
            this.roleId = roleId;
            return this;
        }

        public User build() {
            var user = new User();
            user.setPerson(this.person);
            user.setEmailAddress(this.emailAddress);
            user.setRoleId(this.roleId);
            user.setIsAccountExpired(false);
            user.setIsCredentialsExpired(false);
            user.setIsAccountLocked(false);
            user.setIsEnabled(false);
            return user;
        }
    }
}
